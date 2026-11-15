package com.saffronstone.pos.ui;

import com.saffronstone.pos.RestaurantRepository;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class TablesPanel extends JPanel {
    private final JPanel tableGrid = new JPanel(new GridLayout(3, 4, 15, 15));
    private final JLabel message = new JLabel(" ");

    public TablesPanel() {
        setLayout(new BorderLayout(10, 10)); setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel title = new JLabel("Floor & Tables"); title.setFont(new Font("SansSerif", Font.BOLD, 26)); add(title, BorderLayout.NORTH);
        add(tableGrid, BorderLayout.CENTER);
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refresh = new JButton("Refresh"); refresh.addActionListener(e -> refresh()); footer.add(refresh); footer.add(message);
        add(footer, BorderLayout.SOUTH); refresh();
    }

    private void refresh() {
        try {
            List<Object[]> rows = RestaurantRepository.getTables();
            tableGrid.removeAll();
            for (Object[] row : rows) {
                int number = (Integer) row[0]; String status = (String) row[1];
                JButton button = new JButton("<html>Table " + number + "<br>" + status + "</html>");
                button.setBackground("Available".equals(status) ? new Color(226, 242, 229)
                        : "Occupied".equals(status) ? new Color(255, 226, 220) : new Color(255, 244, 205));
                button.addActionListener(e -> chooseStatus(number, status)); tableGrid.add(button);
            }
            message.setForeground(new Color(49, 91, 66));
            message.setText("Table status saved in SQLite");
        } catch (SQLException error) {
            message.setForeground(new Color(170, 45, 45));
            message.setText("Could not load tables: " + error.getMessage());
        }
        tableGrid.revalidate(); tableGrid.repaint();
    }

    private void chooseStatus(int number, String current) {
        String[] statuses = {"Available", "Occupied", "Reserved"};
        Object selected = JOptionPane.showInputDialog(this, "Choose table status", "Table " + number,
                JOptionPane.PLAIN_MESSAGE, null, statuses, current);
        if (selected == null) return;
        try { RestaurantRepository.setTableStatus(number, selected.toString()); refresh(); }
        catch (IllegalArgumentException error) { JOptionPane.showMessageDialog(this, error.getMessage(), "Invalid Table Status", JOptionPane.ERROR_MESSAGE); }
        catch (SQLException error) { JOptionPane.showMessageDialog(this, "Could not update table.\n" + error.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE); }
    }
}
