package com.saffronstone.pos.ui;

import com.saffronstone.pos.RestaurantRepository;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class CustomersPanel extends JPanel {
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Name", "Phone", "Email", "Orders"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JLabel message = new JLabel(" ");

    public CustomersPanel() {
        setLayout(new BorderLayout(10, 10)); setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel title = new JLabel("Customers"); title.setFont(new Font("SansSerif", Font.BOLD, 26)); add(title, BorderLayout.NORTH);
        JTable table = new JTable(model); table.setFillsViewportHeight(true); add(new JScrollPane(table), BorderLayout.CENTER);
        JButton add = new JButton("Add Customer"); add.addActionListener(e -> addCustomer());
        JButton refresh = new JButton("Refresh"); refresh.addActionListener(e -> refresh());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.add(add); actions.add(refresh); actions.add(message);
        add(actions, BorderLayout.SOUTH); refresh();
    }

    private void refresh() {
        try {
            java.util.List<Object[]> rows = RestaurantRepository.getCustomers();
            model.setRowCount(0);
            for (Object[] row : rows) model.addRow(row);
            message.setForeground(new Color(49, 91, 66));
            message.setText("Customers saved in SQLite");
        } catch (SQLException error) {
            message.setForeground(new Color(170, 45, 45));
            message.setText("Could not load customers: " + error.getMessage());
        }
    }

    private void addCustomer() {
        JTextField name = new JTextField(); JTextField phone = new JTextField(); JTextField email = new JTextField();
        JPanel form = new JPanel(new GridLayout(3, 2, 5, 5));
        form.add(new JLabel("Name")); form.add(name); form.add(new JLabel("Phone")); form.add(phone); form.add(new JLabel("Email")); form.add(email);
        if (JOptionPane.showConfirmDialog(this, form, "Add Customer", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try { RestaurantRepository.addCustomer(name.getText(), phone.getText(), email.getText()); refresh(); }
        catch (IllegalArgumentException error) { JOptionPane.showMessageDialog(this, error.getMessage(), "Invalid Customer", JOptionPane.ERROR_MESSAGE); }
        catch (SQLException error) { JOptionPane.showMessageDialog(this, "Could not save customer. Check for a duplicate name.\n" + error.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE); }
    }
}
