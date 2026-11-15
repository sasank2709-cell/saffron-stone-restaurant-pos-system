package com.saffronstone.pos.ui;

import com.saffronstone.pos.RestaurantRepository;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class MenuPanel extends JPanel {
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Item", "Category", "Price", "Availability"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JLabel message = new JLabel(" ");

    public MenuPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel title = new JLabel("Menu Management"); title.setFont(new Font("SansSerif", Font.BOLD, 26));
        add(title, BorderLayout.NORTH);
        JTable table = new JTable(model); table.setFillsViewportHeight(true);
        add(new JScrollPane(table), BorderLayout.CENTER);
        JButton add = new JButton("Add Menu Item");
        add.addActionListener(e -> addNewItem());
        JButton availability = new JButton("Toggle Availability");
        availability.addActionListener(e -> toggleAvailability(table));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(add); actions.add(availability); actions.add(message);
        add(actions, BorderLayout.SOUTH);
        refresh();
    }

    private void refresh() {
        try {
            java.util.List<Object[]> rows = RestaurantRepository.getMenuItems(false);
            model.setRowCount(0);
            for (Object[] row : rows) {
                model.addRow(new Object[]{row[0], row[1], "₹" + row[2], row[3]});
            }
            message.setText("Menu saved in SQLite"); message.setForeground(new Color(49, 91, 66));
        } catch (SQLException error) { showError(error); }
    }

    private void addNewItem() {
        JTextField name = new JTextField(); JTextField category = new JTextField(); JTextField price = new JTextField();
        JPanel form = new JPanel(new GridLayout(3, 2, 5, 5));
        form.add(new JLabel("Name")); form.add(name); form.add(new JLabel("Category")); form.add(category);
        form.add(new JLabel("Price (₹)")); form.add(price);
        if (JOptionPane.showConfirmDialog(this, form, "Add Menu Item", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try {
            int amount;
            try { amount = Integer.parseInt(price.getText().trim()); }
            catch (NumberFormatException error) {
                JOptionPane.showMessageDialog(this, "Price must be a whole number.", "Invalid Menu Item", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (name.getText().isBlank() || category.getText().isBlank() || amount <= 0) throw new IllegalArgumentException();
            RestaurantRepository.addMenuItem(name.getText(), category.getText(), amount); refresh();
        } catch (IllegalArgumentException error) {
            String detail = error.getMessage() == null ? "Enter an item name, category, and positive whole-number price." : error.getMessage();
            JOptionPane.showMessageDialog(this, detail, "Invalid Menu Item", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException error) { showError(error); }
    }

    private void toggleAvailability(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a menu item first."); return; }
        String name = model.getValueAt(row, 0).toString();
        boolean available = "Unavailable".equals(model.getValueAt(row, 3));
        try { RestaurantRepository.setMenuAvailability(name, available); refresh(); }
        catch (SQLException error) { showError(error); }
    }

    private void showError(SQLException error) {
        message.setText("Database error: " + error.getMessage()); message.setForeground(new Color(170, 45, 45));
    }
}

