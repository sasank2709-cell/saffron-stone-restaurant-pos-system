package com.saffronstone.pos.ui;

import com.saffronstone.pos.RestaurantRepository;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class InventoryPanel extends JPanel {
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Ingredient", "Stock", "Unit", "Status"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JLabel message = new JLabel(" ");
    private final JTable table = new JTable(model);

    public InventoryPanel() {
        setLayout(new BorderLayout(10, 10)); setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel title = new JLabel("Inventory"); title.setFont(new Font("SansSerif", Font.BOLD, 26)); add(title, BorderLayout.NORTH);
        table.setFillsViewportHeight(true); add(new JScrollPane(table), BorderLayout.CENTER);
        JButton add = new JButton("Add Ingredient"); add.addActionListener(e -> addItem());
        JButton update = new JButton("Update Stock"); update.addActionListener(e -> updateStock());
        JButton refresh = new JButton("Refresh"); refresh.addActionListener(e -> refresh());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.add(add); actions.add(update); actions.add(refresh); actions.add(message);
        add(actions, BorderLayout.SOUTH); table.removeColumn(table.getColumnModel().getColumn(0)); refresh();
    }

    private void refresh() {
        try {
            java.util.List<Object[]> rows = RestaurantRepository.getInventory();
            model.setRowCount(0);
            for (Object[] row : rows) model.addRow(row);
            message.setText("Inventory saved in SQLite");
            message.setForeground(new Color(49, 91, 66));
        } catch (SQLException error) {
            message.setText("Could not load inventory: " + error.getMessage());
            message.setForeground(new Color(170, 45, 45));
        }
    }

    private void addItem() {
        JTextField name = new JTextField(); JTextField quantity = new JTextField("0");
        JTextField unit = new JTextField("kg"); JTextField lowAt = new JTextField("5");
        JPanel form = new JPanel(new GridLayout(4, 2, 5, 5));
        form.add(new JLabel("Ingredient")); form.add(name); form.add(new JLabel("Quantity")); form.add(quantity);
        form.add(new JLabel("Unit")); form.add(unit); form.add(new JLabel("Low stock threshold")); form.add(lowAt);
        if (JOptionPane.showConfirmDialog(this, form, "Add Ingredient", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try {
            if (name.getText().isBlank() || unit.getText().isBlank()) throw new IllegalArgumentException();
            double q;
            double threshold;
            try {
                q = Double.parseDouble(quantity.getText().trim());
                threshold = Double.parseDouble(lowAt.getText().trim());
            } catch (NumberFormatException error) {
                JOptionPane.showMessageDialog(this, "Quantity and threshold must be numbers.", "Invalid Stock", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!Double.isFinite(q) || !Double.isFinite(threshold) || q < 0 || threshold < 0) {
                throw new IllegalArgumentException("Quantity and threshold must be finite, non-negative numbers.");
            }
            RestaurantRepository.addInventoryItem(name.getText(), q, unit.getText(), threshold); refresh();
        } catch (IllegalArgumentException error) {
            JOptionPane.showMessageDialog(this, "Enter a name, unit, and non-negative numbers.", "Invalid Stock", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException error) { JOptionPane.showMessageDialog(this, "Could not save ingredient. The name may already exist.\n" + error.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE); }
    }

    private void updateStock() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) { JOptionPane.showMessageDialog(this, "Select an ingredient first."); return; }
        int row = table.convertRowIndexToModel(viewRow);
        final int id;
        try {
            id = Integer.parseInt(model.getValueAt(row, 0).toString());
        } catch (NumberFormatException | IndexOutOfBoundsException error) {
            JOptionPane.showMessageDialog(this, "Could not identify the selected ingredient. Refresh the inventory and try again.",
                    "Inventory Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String amount = JOptionPane.showInputDialog(this, "Enter the new stock quantity:", model.getValueAt(row, 2));
        if (amount == null) return;
        try {
            double q;
            try { q = Double.parseDouble(amount.trim()); }
            catch (NumberFormatException error) {
                JOptionPane.showMessageDialog(this, "Quantity must be a number.", "Invalid Stock", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!Double.isFinite(q) || q < 0) {
                throw new IllegalArgumentException("Quantity must be a finite, non-negative number.");
            }
            RestaurantRepository.updateInventoryQuantity(id, q); refresh();
        }
        catch (IllegalArgumentException error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Invalid Stock", JOptionPane.ERROR_MESSAGE);
        }
        catch (SQLException error) { JOptionPane.showMessageDialog(this, "Could not update stock.\n" + error.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE); }
    }
}
