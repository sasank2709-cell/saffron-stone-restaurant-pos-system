package com.saffronstone.pos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/** Saves POS orders and reads them back for the order screens. */
public final class OrderRepository implements OrderStore {
    public OrderRepository() { }

    @Override
    public long saveOrder(String customer, String orderType, Integer tableNumber,
                                 String paymentMethod, Map<String, Integer> items,
                                 Map<String, Integer> prices) throws SQLException {
        if (items == null || items.isEmpty()) throw new IllegalArgumentException("Add at least one item to the order.");
        if (prices == null) throw new IllegalArgumentException("Menu prices are unavailable.");
        if (orderType == null || !List.of("Dine-in", "Takeaway", "Delivery").contains(orderType)) {
            throw new IllegalArgumentException("Choose a valid order type.");
        }
        if (paymentMethod == null || !List.of("Cash", "Card", "UPI").contains(paymentMethod)) {
            throw new IllegalArgumentException("Choose a valid payment method.");
        }
        if ("Dine-in".equals(orderType) && (tableNumber == null || tableNumber < 1 || tableNumber > 12)) {
            throw new IllegalArgumentException("Choose a valid table for dine-in orders.");
        }
        BigDecimal subtotal = BigDecimal.ZERO;
        for (Map.Entry<String, Integer> item : items.entrySet()) {
            Integer price = prices.get(item.getKey());
            if (item.getKey() == null || item.getKey().isBlank() || price == null || price <= 0
                    || item.getValue() == null || item.getValue() < 1) {
                throw new IllegalArgumentException("Order contains an invalid menu item or quantity.");
            }
            subtotal = subtotal.add(BigDecimal.valueOf((long) price * item.getValue()));
        }
        BigDecimal tax = subtotal.multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(tax);
        String customerName = customer == null || customer.isBlank() ? "Walk-in Guest" : customer.trim();

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement customerInsert = connection.prepareStatement(
                        "INSERT OR IGNORE INTO customers(name) VALUES(?)")) {
                    customerInsert.setString(1, customerName); customerInsert.executeUpdate();
                }
                long orderId;
                String orderSql = "INSERT INTO orders (customer_name,order_type,table_number,payment_method,subtotal,tax,total) "
                        + "VALUES (?,?,?,?,?,?,?)";
                try (PreparedStatement order = connection.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                    order.setString(1, customerName); order.setString(2, orderType);
                    if (tableNumber == null) order.setNull(3, java.sql.Types.INTEGER); else order.setInt(3, tableNumber);
                    order.setString(4, paymentMethod); order.setBigDecimal(5, subtotal);
                    order.setBigDecimal(6, tax); order.setBigDecimal(7, total); order.executeUpdate();
                    try (ResultSet keys = order.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("The database did not return an order number.");
                        orderId = keys.getLong(1);
                    }
                }
                String itemSql = "INSERT INTO order_items(order_id,item_name,unit_price,quantity,line_total) VALUES(?,?,?,?,?)";
                try (PreparedStatement line = connection.prepareStatement(itemSql)) {
                    for (Map.Entry<String, Integer> item : items.entrySet()) {
                        int price = prices.get(item.getKey());
                        line.setLong(1, orderId); line.setString(2, item.getKey());
                        line.setBigDecimal(3, BigDecimal.valueOf(price)); line.setInt(4, item.getValue());
                        line.setBigDecimal(5, BigDecimal.valueOf((long) price * item.getValue())); line.addBatch();
                    }
                    line.executeBatch();
                }
                if ("Dine-in".equals(orderType) && tableNumber != null) {
                    try (PreparedStatement table = connection.prepareStatement(
                            "UPDATE restaurant_tables SET status='Occupied' WHERE table_number=?")) {
                        table.setInt(1, tableNumber); table.executeUpdate();
                    }
                }
                connection.commit();
                return orderId;
            } catch (SQLException | RuntimeException error) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    error.addSuppressed(rollbackError);
                }
                throw error;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    @Override
    public List<Object[]> getRecentOrders() throws SQLException {
        List<Object[]> results = new java.util.ArrayList<>();
        String sql = "SELECT id,customer_name,order_type,table_number,total,status,created_at "
                + "FROM orders ORDER BY id DESC LIMIT 200";
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                Object table = r.getObject("table_number");
                BigDecimal total = r.getBigDecimal("total");
                results.add(new Object[]{r.getLong("id"), r.getString("customer_name"), r.getString("order_type"),
                        table == null ? "-" : table.toString(), "₹" + total.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                        r.getString("status"), r.getString("created_at")});
            }
        }
        return results;
    }

    /** Returns the saved line items for one order, including the price snapshot used at checkout. */
    @Override
    public List<Object[]> getOrderItems(long orderId) throws SQLException {
        if (orderId <= 0) throw new IllegalArgumentException("Order number must be greater than zero.");
        List<Object[]> results = new java.util.ArrayList<>();
        String sql = "SELECT item_name,unit_price,quantity,line_total FROM order_items "
                + "WHERE order_id=? ORDER BY id";
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, orderId);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    results.add(new Object[]{r.getString("item_name"), r.getBigDecimal("unit_price"),
                            r.getInt("quantity"), r.getBigDecimal("line_total")});
                }
            }
        }
        return results;
    }
}
