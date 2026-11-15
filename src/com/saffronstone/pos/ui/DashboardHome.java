package com.saffronstone.pos.ui;

import com.saffronstone.pos.RestaurantRepository;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;

public class DashboardHome extends JPanel {
    private final JLabel sales = new JLabel("—");
    private final JLabel orders = new JLabel("—");
    private final JLabel average = new JLabel("—");
    private final JLabel tables = new JLabel("—");
    private final JLabel message = new JLabel(" ");

    public DashboardHome() {
        setLayout(new BorderLayout()); setBackground(new Color(248, 247, 244));
        JLabel heading = new JLabel("Saffron & Stone POS"); heading.setFont(new Font("Serif", Font.BOLD, 28));
        JPanel top = new JPanel(new BorderLayout()); top.setBackground(new Color(248, 247, 244));
        top.setBorder(BorderFactory.createEmptyBorder(25, 25, 15, 25)); top.add(heading, BorderLayout.WEST);
        JButton refresh = new JButton("Refresh"); refresh.addActionListener(e -> load()); top.add(refresh, BorderLayout.EAST); add(top, BorderLayout.NORTH);
        JPanel cards = new JPanel(new GridLayout(2, 2, 15, 15)); cards.setBackground(new Color(248, 247, 244));
        cards.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        cards.add(createCard("Total Sales", sales)); cards.add(createCard("Orders", orders));
        cards.add(createCard("Average Order", average)); cards.add(createCard("Tables Occupied", tables));
        add(cards, BorderLayout.CENTER); message.setBorder(BorderFactory.createEmptyBorder(0,25,15,25)); add(message, BorderLayout.SOUTH); load();
    }

    private JPanel createCard(String title, JLabel value) {
        JPanel card = new JPanel(new GridLayout(2, 1)); card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        card.add(new JLabel(title)); value.setFont(new Font("SansSerif", Font.BOLD, 25));
        value.setForeground(new Color(49, 91, 66)); card.add(value); return card;
    }

    private void load() {
        try {
            Object[] summary = RestaurantRepository.getSummary();
            NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
            sales.setText(currency.format((Double) summary[0])); orders.setText(summary[1].toString());
            average.setText(currency.format((Double) summary[2])); tables.setText(summary[3] + " / 12");
            message.setForeground(new Color(49, 91, 66));
            message.setText("Dashboard values loaded from SQLite.");
        } catch (SQLException error) {
            message.setForeground(new Color(170, 45, 45));
            message.setText("Could not load dashboard: " + error.getMessage());
        }
    }
}
