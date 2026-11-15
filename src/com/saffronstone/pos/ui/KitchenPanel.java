package com.saffronstone.pos.ui;

import com.saffronstone.pos.RestaurantRepository;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class KitchenPanel extends JPanel {
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Order", "Type / Table", "Items", "Status", "Placed At"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JLabel message = new JLabel(" ");

    public KitchenPanel() {
        setLayout(new BorderLayout(10, 10)); setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel title = new JLabel("Kitchen Display"); title.setFont(new Font("SansSerif", Font.BOLD, 26)); add(title, BorderLayout.NORTH);
        table.setFillsViewportHeight(true); add(new JScrollPane(table), BorderLayout.CENTER);
        JButton update = new JButton("Update Selected Order"); update.addActionListener(e -> updateStatus());
        JButton refresh = new JButton("Refresh"); refresh.addActionListener(e -> refresh());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.add(update); actions.add(refresh); actions.add(message);
        add(actions, BorderLayout.SOUTH); refresh();
    }

    private void refresh() {
        try {
            List<Object[]> rows = RestaurantRepository.getKitchenOrders();
            model.setRowCount(0);
            for (Object[] row : rows) model.addRow(row);
            message.setForeground(new Color(90, 90, 90));
            message.setText(rows.isEmpty() ? "No active kitchen orders." : "Kitchen orders loaded from SQLite.");
        } catch (SQLException error) {
            message.setForeground(new Color(170, 45, 45));
            message.setText("Could not load kitchen orders: " + error.getMessage());
        }
    }

    private void updateStatus() {
        int viewRow = table.getSelectedRow(); if (viewRow < 0) { JOptionPane.showMessageDialog(this, "Select an order first."); return; }
        int row = table.convertRowIndexToModel(viewRow);
        final long id;
        try {
            id = Long.parseLong(model.getValueAt(row, 0).toString());
        } catch (NumberFormatException | IndexOutOfBoundsException error) {
            JOptionPane.showMessageDialog(this, "Could not identify the selected order. Refresh the kitchen list and try again.",
                    "Kitchen Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String[] statuses = {"New", "Preparing", "Ready", "Completed", "Cancelled"};
        Object selected = JOptionPane.showInputDialog(this, "Choose order status", "Order " + id,
                JOptionPane.PLAIN_MESSAGE, null, statuses, model.getValueAt(row, 3));
        if (selected == null) return;
        try { RestaurantRepository.updateOrderStatus(id, selected.toString()); refresh(); }
        catch (IllegalArgumentException error) { JOptionPane.showMessageDialog(this, error.getMessage(), "Invalid Order Status", JOptionPane.ERROR_MESSAGE); }
        catch (SQLException error) { JOptionPane.showMessageDialog(this, "Could not update order.\n" + error.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE); }
    }
}
