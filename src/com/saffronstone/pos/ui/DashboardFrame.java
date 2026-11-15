package com.saffronstone.pos.ui;

import javax.swing.*;
import com.saffronstone.pos.DBConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.awt.*;

public class DashboardFrame extends JFrame {

    private JPanel contentPanel;

    public DashboardFrame() {

        setTitle(
                "Saffron & Stone - Restaurant POS"
        );

        setSize(
                1200,
                750
        );

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        setLocationRelativeTo(null);

        setLayout(
                new BorderLayout()
        );

        add(
                createHeader(),
                BorderLayout.NORTH
        );

        add(
                createSidebar(),
                BorderLayout.WEST
        );

        contentPanel =
                new JPanel(
                        new BorderLayout()
                );

        contentPanel.add(
                new DashboardHome(),
                BorderLayout.CENTER
        );

        add(
                contentPanel,
                BorderLayout.CENTER
        );
    }

    private JPanel createHeader() {

        JPanel header =
                new JPanel(
                        new BorderLayout()
                );

        header.setBackground(
                Color.WHITE
        );

        header.setBorder(
                BorderFactory.createEmptyBorder(
                        15,
                        20,
                        15,
                        20
                )
        );

        JLabel title =
                new JLabel(
                        "Saffron & Stone"
                );

        title.setFont(
                new Font(
                        "Serif",
                        Font.BOLD,
                        24
                )
        );

        JLabel status;
        try (Connection ignored = DBConnection.getConnection()) {
            status = new JLabel("● SQLite Database Connected");
            status.setForeground(new Color(49, 91, 66));
        } catch (SQLException error) {
            status = new JLabel("● SQLite Database Disconnected");
            status.setForeground(new Color(170, 45, 45));
            status.setToolTipText(error.getMessage());
        }

        header.add(
                title,
                BorderLayout.WEST
        );

        header.add(
                status,
                BorderLayout.EAST
        );

        return header;
    }

    private JPanel createSidebar() {

        JPanel sidebar =
                new JPanel();

        sidebar.setPreferredSize(
                new Dimension(
                        220,
                        0
                )
        );

        sidebar.setBackground(
                new Color(
                        49,
                        91,
                        66
                )
        );

        sidebar.setLayout(
                new GridLayout(
                        10,
                        1,
                        5,
                        5
                )
        );

        addButton(
                sidebar,
                "Overview",
                () ->
                        showPanel(
                                new DashboardHome()
                        )
        );

        addButton(
                sidebar,
                "Point of Sale",
                () ->
                        showPanel(
                                new POSPanel()
                        )
        );

        addButton(
                sidebar,
                "Orders",
                () ->
                        showPanel(
                                new OrdersPanel()
                        )
        );

        addButton(
                sidebar,
                "Tables",
                () ->
                        showPanel(
                                new TablesPanel()
                        )
        );

        addButton(
                sidebar,
                "Menu",
                () ->
                        showPanel(
                                new MenuPanel()
                        )
        );

        addButton(
                sidebar,
                "Customers",
                () ->
                        showPanel(
                                new CustomersPanel()
                        )
        );

        addButton(
                sidebar,
                "Inventory",
                () ->
                        showPanel(
                                new InventoryPanel()
                        )
        );

        addButton(
                sidebar,
                "Kitchen",
                () ->
                        showPanel(
                                new KitchenPanel()
                        )
        );

        addButton(
                sidebar,
                "Reports",
                () ->
                        showPanel(
                                new ReportsPanel()
                        )
        );

        return sidebar;
    }

    private void addButton(
            JPanel panel,
            String text,
            Runnable action
    ) {

        JButton button =
                new JButton(text);

        button.setBackground(
                new Color(
                        49,
                        91,
                        66
                )
        );

        button.setForeground(
                Color.WHITE
        );

        button.setFocusPainted(
                false
        );

        button.addActionListener(
                e -> action.run()
        );

        panel.add(button);
    }

    private void showPanel(
            JPanel panel
    ) {

        contentPanel.removeAll();

        contentPanel.add(
                panel,
                BorderLayout.CENTER
        );

        contentPanel.revalidate();

        contentPanel.repaint();
    }
}
