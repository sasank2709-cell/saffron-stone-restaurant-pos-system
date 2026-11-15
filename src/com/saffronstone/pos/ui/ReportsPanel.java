package com.saffronstone.pos.ui;

import com.saffronstone.pos.RestaurantRepository;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.List;

public class ReportsPanel extends JPanel {
    private final JLabel salesValue = new JLabel("—");
    private final JLabel ordersValue = new JLabel("—");
    private final JLabel topItemValue = new JLabel("—");
    private final JLabel todaySalesValue = new JLabel("—");
    private final JLabel message = new JLabel(" ");
    private final DefaultTableModel todayModel = new DefaultTableModel(
            new String[]{"Order ID", "Customer", "Type", "Table", "Total", "Status", "Placed At"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    public ReportsPanel() {
        setLayout(new BorderLayout(15, 15)); setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel title = new JLabel("Reports"); title.setFont(new Font("SansSerif", Font.BOLD, 26)); add(title, BorderLayout.NORTH);
        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 12));
        cards.add(createCard("All-time Sales", salesValue));
        cards.add(createCard("Today's Sales", todaySalesValue));
        cards.add(createCard("All Orders", ordersValue));
        cards.add(createCard("Top Item", topItemValue));
        JTable todayTable = new JTable(todayModel);
        todayTable.setFillsViewportHeight(true);
        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.add(cards, BorderLayout.NORTH);
        center.add(new JScrollPane(todayTable), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT)); JButton refresh = new JButton("Refresh"); refresh.addActionListener(e -> load()); footer.add(refresh); footer.add(message); add(footer, BorderLayout.SOUTH);
        load();
    }

    private JPanel createCard(String title, JLabel value) {
        JPanel card = new JPanel(new GridLayout(2, 1)); card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        card.add(new JLabel(title)); value.setFont(new Font("SansSerif", Font.BOLD, 22)); card.add(value); return card;
    }

    private void load() {
        try {
            Object[] summary = RestaurantRepository.getSummary();
            NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
            salesValue.setText(currency.format((Double) summary[0]));
            todaySalesValue.setText(currency.format((Double) summary[5]));
            ordersValue.setText(summary[1].toString()); topItemValue.setText(summary[4].toString());
            List<Object[]> todayOrders = RestaurantRepository.getTodayOrders();
            todayModel.setRowCount(0);
            for (Object[] row : todayOrders) todayModel.addRow(row);
            message.setForeground(new Color(49, 91, 66));
            message.setText(summary[6] + " order(s) today · " + todayOrders.size() + " order detail row(s) loaded from SQLite.");
        } catch (SQLException error) {
            message.setForeground(new Color(170, 45, 45));
            message.setText("Could not load report: " + error.getMessage());
        }
    }
}
