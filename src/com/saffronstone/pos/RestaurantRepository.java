package com.saffronstone.pos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Read and write operations for the restaurant POS screens. */
public final class RestaurantRepository {
    private RestaurantRepository() { }

    public static List<Object[]> getMenuItems(boolean availableOnly) throws SQLException {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT name,category,price,available FROM menu_items "
                + (availableOnly ? "WHERE available=1 " : "") + "ORDER BY category,name";
        try (Connection c = DBConnection.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            while (r.next()) rows.add(new Object[]{r.getString("name"), r.getString("category"),
                    r.getInt("price"), r.getInt("available") == 1 ? "Available" : "Unavailable"});
        }
        return rows;
    }

    public static void addMenuItem(String name, String category, int price) throws SQLException {
        name = InputValidation.requiredText(name, "Item name");
        category = InputValidation.requiredText(category, "Category");
        InputValidation.positiveInt(price, "Price");
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(
                "INSERT INTO menu_items(name,category,price,available) VALUES(?,?,?,1)")) {
            p.setString(1, name.trim()); p.setString(2, category.trim()); p.setInt(3, price); p.executeUpdate();
        }
    }

    public static void setMenuAvailability(String name, boolean available) throws SQLException {
        name = InputValidation.requiredText(name, "Item name");
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(
                "UPDATE menu_items SET available=? WHERE name=?")) {
            p.setInt(1, available ? 1 : 0); p.setString(2, name);
            if (p.executeUpdate() == 0) throw new SQLException("Menu item was not found. Refresh the menu and try again.");
        }
    }

    public static List<Object[]> getCustomers() throws SQLException {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT c.name,COALESCE(c.phone,''),COALESCE(c.email,''),COUNT(o.id) AS order_count "
                + "FROM customers c LEFT JOIN orders o ON lower(o.customer_name)=lower(c.name) "
                + "GROUP BY c.id ORDER BY c.name";
        try (Connection c = DBConnection.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            while (r.next()) rows.add(new Object[]{r.getString(1), r.getString(2), r.getString(3), r.getInt(4)});
        }
        return rows;
    }

    public static void addCustomer(String name, String phone, String email) throws SQLException {
        name = InputValidation.requiredText(name, "Customer name");
        phone = InputValidation.optionalPhone(phone);
        email = InputValidation.optionalEmail(email);
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(
                "INSERT INTO customers(name,phone,email) VALUES(?,?,?)")) {
            p.setString(1, name.trim()); p.setString(2, phone); p.setString(3, email); p.executeUpdate();
        }
    }

    public static List<Object[]> getInventory() throws SQLException {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT id,name,quantity,unit,low_stock_at FROM inventory_items ORDER BY name";
        try (Connection c = DBConnection.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            while (r.next()) {
                double quantity = r.getDouble("quantity");
                rows.add(new Object[]{r.getInt("id"), r.getString("name"), quantity, r.getString("unit"),
                        quantity <= r.getDouble("low_stock_at") ? "Low Stock" : "In Stock"});
            }
        }
        return rows;
    }

    public static void addInventoryItem(String name, double quantity, String unit, double lowStockAt) throws SQLException {
        name = InputValidation.requiredText(name, "Ingredient name");
        unit = InputValidation.requiredText(unit, "Unit");
        InputValidation.nonNegativeFinite(quantity, "Quantity");
        InputValidation.nonNegativeFinite(lowStockAt, "Low stock threshold");
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(
                "INSERT INTO inventory_items(name,quantity,unit,low_stock_at) VALUES(?,?,?,?)")) {
            p.setString(1, name.trim()); p.setDouble(2, quantity); p.setString(3, unit.trim());
            p.setDouble(4, lowStockAt); p.executeUpdate();
        }
    }

    public static void updateInventoryQuantity(int id, double quantity) throws SQLException {
        if (id <= 0) throw new IllegalArgumentException("Ingredient ID must be greater than zero.");
        InputValidation.nonNegativeFinite(quantity, "Quantity");
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(
                "UPDATE inventory_items SET quantity=? WHERE id=?")) {
            p.setDouble(1, quantity); p.setInt(2, id);
            if (p.executeUpdate() == 0) throw new SQLException("Ingredient was not found. Refresh inventory and try again.");
        }
    }

    public static List<Object[]> getTables() throws SQLException {
        List<Object[]> rows = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT table_number,status FROM restaurant_tables ORDER BY table_number")) {
            while (r.next()) rows.add(new Object[]{r.getInt(1), r.getString(2)});
        }
        return rows;
    }

    public static void setTableStatus(int tableNumber, String status) throws SQLException {
        if (tableNumber <= 0) throw new IllegalArgumentException("Table number must be greater than zero.");
        if (status == null || !List.of("Available", "Occupied", "Reserved").contains(status)) {
            throw new IllegalArgumentException("Choose a valid table status.");
        }
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(
                "UPDATE restaurant_tables SET status=? WHERE table_number=?")) {
            p.setString(1, status); p.setInt(2, tableNumber);
            if (p.executeUpdate() == 0) throw new SQLException("Table " + tableNumber + " was not found.");
        }
    }

    public static List<Object[]> getKitchenOrders() throws SQLException {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT o.id,o.order_type,o.table_number,o.status,o.created_at,"
                + "COALESCE(group_concat(i.item_name || ' × ' || i.quantity, ', '),'') AS items "
                + "FROM orders o LEFT JOIN order_items i ON i.order_id=o.id "
                + "WHERE o.status NOT IN ('Completed','Cancelled') "
                + "GROUP BY o.id ORDER BY o.id DESC";
        try (Connection c = DBConnection.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            while (r.next()) {
                String table = r.getObject("table_number") == null ? "" : " / Table " + r.getInt("table_number");
                rows.add(new Object[]{r.getLong("id"), r.getString("order_type") + table,
                        r.getString("items"), r.getString("status"), r.getString("created_at")});
            }
        }
        return rows;
    }

    public static void updateOrderStatus(long orderId, String status) throws SQLException {
        if (orderId <= 0) throw new IllegalArgumentException("Order number must be greater than zero.");
        if (status == null || !List.of("New", "Preparing", "Ready", "Completed", "Cancelled").contains(status)) {
            throw new IllegalArgumentException("Choose a valid order status.");
        }
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement update = c.prepareStatement("UPDATE orders SET status=? WHERE id=?")) {
                update.setString(1, status); update.setLong(2, orderId);
                if (update.executeUpdate() == 0) throw new SQLException("Order was not found.");
                if ("Completed".equals(status)) {
                    try (PreparedStatement table = c.prepareStatement(
                            "UPDATE restaurant_tables SET status='Available' WHERE table_number="
                                    + "(SELECT table_number FROM orders WHERE id=?)")) {
                        table.setLong(1, orderId); table.executeUpdate();
                    }
                }
                c.commit();
            } catch (SQLException error) {
                try {
                    c.rollback();
                } catch (SQLException rollbackError) {
                    error.addSuppressed(rollbackError);
                }
                throw error;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public static Object[] getSummary() throws SQLException {
        String sql = "SELECT COALESCE(SUM(CASE WHEN status<>'Cancelled' THEN total ELSE 0 END),0),"
                + "SUM(CASE WHEN status<>'Cancelled' THEN 1 ELSE 0 END),"
                + "(SELECT COUNT(*) FROM restaurant_tables WHERE status='Occupied'),"
                + "COALESCE((SELECT i.item_name FROM order_items i JOIN orders o ON o.id=i.order_id "
                + "WHERE o.status<>'Cancelled' GROUP BY i.item_name "
                + "ORDER BY SUM(quantity) DESC LIMIT 1),'—'),"
                + "COALESCE(SUM(CASE WHEN date(created_at,'localtime')=date('now','localtime') "
                + "AND status<>'Cancelled' THEN total ELSE 0 END),0),"
                + "SUM(CASE WHEN date(created_at,'localtime')=date('now','localtime') "
                + "AND status<>'Cancelled' THEN 1 ELSE 0 END) FROM orders";
        try (Connection c = DBConnection.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            r.next();
            double sales = r.getDouble(1);
            int count = r.getInt(2);
            return new Object[]{sales, count, count == 0 ? 0 : sales / count, r.getInt(3), r.getString(4),
                    r.getDouble(5), r.getInt(6)};
        }
    }

    /** Returns today's saved orders for the daily sales report. */
    public static List<Object[]> getTodayOrders() throws SQLException {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT id,customer_name,order_type,table_number,total,status,created_at FROM orders "
                + "WHERE date(created_at,'localtime')=date('now','localtime') "
                + "ORDER BY id DESC";
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                Object table = r.getObject("table_number");
                rows.add(new Object[]{r.getLong("id"), r.getString("customer_name"), r.getString("order_type"),
                        table == null ? "—" : table.toString(), "₹" + r.getBigDecimal("total"),
                        r.getString("status"), r.getString("created_at")});
            }
        }
        return rows;
    }
}

