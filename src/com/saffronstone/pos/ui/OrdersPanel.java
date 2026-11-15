package com.saffronstone.pos.ui;

import com.saffronstone.pos.OrderRepository;
import com.saffronstone.pos.OrderStore;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class OrdersPanel extends JPanel {
    private final OrderStore orderStore = new OrderRepository();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Order ID", "Customer", "Type", "Table", "Total", "Status", "Placed At"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JLabel message = new JLabel(" ");

    public OrdersPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        JLabel title = new JLabel("Orders");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        heading.add(title, BorderLayout.WEST);
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(event -> loadOrders());
        heading.add(refresh, BorderLayout.EAST);
        add(heading, BorderLayout.NORTH);

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton details = new JButton("View Order Items");
        details.addActionListener(event -> showOrderItems(table));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(details);
        actions.add(message);

        message.setForeground(new Color(90, 90, 90));
        add(actions, BorderLayout.SOUTH);
        loadOrders();
    }

    private void showOrderItems(JTable table) {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Select an order first.", "No Order Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int row = table.convertRowIndexToModel(selected);
        long orderId = ((Number) model.getValueAt(row, 0)).longValue();
        try {
            List<Object[]> items = orderStore.getOrderItems(orderId);
            if (items.isEmpty()) {
                JOptionPane.showMessageDialog(this, "This order has no saved items.", "Order " + orderId, JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            DefaultTableModel itemModel = new DefaultTableModel(new String[]{"Item", "Unit Price", "Quantity", "Line Total"}, 0) {
                @Override public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }
            };
            for (Object[] item : items) {
                itemModel.addRow(new Object[]{item[0], "₹" + item[1], item[2], "₹" + item[3]});
            }
            JTable itemTable = new JTable(itemModel);
            itemTable.setFillsViewportHeight(true);
            JScrollPane scroll = new JScrollPane(itemTable);
            scroll.setPreferredSize(new Dimension(520, Math.min(280, 55 + items.size() * 32)));
            JOptionPane.showMessageDialog(this, scroll, "Saved items for order " + orderId, JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException error) {
            JOptionPane.showMessageDialog(this, "Could not load order items.\n" + error.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadOrders() {
        try {
            List<Object[]> orders = orderStore.getRecentOrders();
            model.setRowCount(0);
            for (Object[] order : orders) model.addRow(order);
            message.setForeground(new Color(90, 90, 90));
            if (orders.isEmpty()) {
                message.setText("No saved orders yet. Place an order in Point of Sale, then refresh.");
            } else {
                message.setText("Showing " + orders.size() + " saved order(s).");
            }
        } catch (SQLException error) {
            message.setForeground(new Color(170, 45, 45));
            message.setText("Could not load orders: " + error.getMessage());
        }
    }
}
