package com.saffronstone.pos.ui;

import com.saffronstone.pos.OrderRepository;
import com.saffronstone.pos.OrderStore;
import com.saffronstone.pos.RestaurantRepository;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class POSPanel extends JPanel {
    private static final Color GREEN = new Color(49, 91, 66);
    private static final Color BACKGROUND = new Color(248, 247, 244);
    private final OrderStore orderStore = new OrderRepository();
    private final Map<String, Integer> cart = new LinkedHashMap<>();
    private final Map<String, Integer> menuPrices = new LinkedHashMap<>();
    private final JPanel menuGrid = new JPanel(new GridLayout(0, 2, 12, 12));
    private final JPanel cartPanel = new JPanel();
    private JLabel subtotalLabel, taxLabel, totalLabel;
    private JTextField customerField;
    private JComboBox<String> orderTypeBox, tableBox, paymentBox;
    private JLabel menuMessage;

    public POSPanel() {
        setLayout(new BorderLayout(15, 15)); setBackground(BACKGROUND);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        add(createMenuSection(), BorderLayout.CENTER);
        add(createOrderSection(), BorderLayout.EAST);
        loadMenu();
    }

    private JPanel createMenuSection() {
        JPanel main = new JPanel(new BorderLayout(10, 10)); main.setOpaque(false);
        JPanel heading = new JPanel(new BorderLayout()); heading.setOpaque(false);
        JLabel title = new JLabel("Point of Sale"); title.setFont(new Font("SansSerif", Font.BOLD, 26));
        heading.add(title, BorderLayout.WEST);
        JButton refresh = new JButton("Refresh Menu"); refresh.addActionListener(e -> loadMenu()); heading.add(refresh, BorderLayout.EAST);
        main.add(heading, BorderLayout.NORTH);
        menuGrid.setOpaque(false);
        main.add(new JScrollPane(menuGrid), BorderLayout.CENTER);
        menuMessage = new JLabel(" "); main.add(menuMessage, BorderLayout.SOUTH);

        return main;
    }

    private void loadMenu() {
        try {
            java.util.List<Object[]> items = RestaurantRepository.getMenuItems(true);
            menuGrid.removeAll();
            menuPrices.clear();
            for (Object[] item : items) {
                String name = (String) item[0]; int price = ((Number) item[2]).intValue();
                menuPrices.put(name, price); addItem(name, price);
            }
            menuMessage.setText(menuPrices.isEmpty() ? "No available menu items. Add items in Menu Management." : "Menu loaded from SQLite");
            menuMessage.setForeground(new Color(70, 90, 70));
        } catch (SQLException error) {
            menuMessage.setText("Could not load menu: " + error.getMessage()); menuMessage.setForeground(new Color(170, 45, 45));
        }
        menuGrid.revalidate(); menuGrid.repaint(); refreshCart();
    }

    private void addItem(String name, int price) {
        JPanel card = new JPanel(new BorderLayout(5, 5)); card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(12, 12, 12, 12));
        JLabel itemName = new JLabel(name); itemName.setFont(new Font("SansSerif", Font.BOLD, 14));
        JLabel amount = new JLabel("₹" + price); amount.setForeground(GREEN);
        JButton add = new JButton("+ Add");
        add.addActionListener(e -> { cart.put(name, cart.getOrDefault(name, 0) + 1); refreshCart(); });
        card.add(itemName, BorderLayout.NORTH); card.add(amount, BorderLayout.CENTER); card.add(add, BorderLayout.SOUTH);
        menuGrid.add(card);
    }

    private JPanel createOrderSection() {
        JPanel order = new JPanel(new BorderLayout(10, 10)); order.setPreferredSize(new Dimension(350, 0));
        order.setBackground(Color.WHITE); order.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel title = new JLabel("Current Order"); title.setFont(new Font("SansSerif", Font.BOLD, 20)); order.add(title, BorderLayout.NORTH);
        cartPanel.setLayout(new BoxLayout(cartPanel, BoxLayout.Y_AXIS)); cartPanel.setBackground(Color.WHITE);
        order.add(new JScrollPane(cartPanel), BorderLayout.CENTER);
        JPanel bottom = new JPanel(); bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS)); bottom.setBackground(Color.WHITE);
        JPanel details = new JPanel(new GridLayout(0, 2, 5, 5)); details.setBackground(Color.WHITE);
        details.add(new JLabel("Customer")); customerField = new JTextField("Walk-in Guest"); details.add(customerField);
        details.add(new JLabel("Order Type")); orderTypeBox = new JComboBox<>(new String[]{"Dine-in", "Takeaway", "Delivery"}); details.add(orderTypeBox);
        details.add(new JLabel("Table")); tableBox = new JComboBox<>(); for (int i=1;i<=12;i++) tableBox.addItem(String.valueOf(i)); details.add(tableBox);
        details.add(new JLabel("Payment")); paymentBox = new JComboBox<>(new String[]{"Cash", "Card", "UPI"}); details.add(paymentBox);
        bottom.add(details); bottom.add(Box.createVerticalStrut(10));
        subtotalLabel = new JLabel("Subtotal: ₹0"); taxLabel = new JLabel("Tax: ₹0");
        totalLabel = new JLabel("Total: ₹0"); totalLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        bottom.add(subtotalLabel); bottom.add(taxLabel); bottom.add(totalLabel); bottom.add(Box.createVerticalStrut(10));
        JButton checkout = new JButton("Checkout"); checkout.setBackground(GREEN); checkout.setForeground(Color.WHITE);
        checkout.addActionListener(e -> checkout()); bottom.add(checkout);
        order.add(bottom, BorderLayout.SOUTH); refreshCart(); return order;
    }

    private void refreshCart() {
        cartPanel.removeAll(); double subtotal = 0;
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            int price = menuPrices.getOrDefault(entry.getKey(), 0); subtotal += (double) price * entry.getValue();
            JPanel row = new JPanel(new BorderLayout(8, 0)); row.setBackground(Color.WHITE);
            JButton remove = new JButton("−");
            remove.setToolTipText("Remove one " + entry.getKey());
            remove.addActionListener(e -> {
                int quantity = cart.getOrDefault(entry.getKey(), 0);
                if (quantity <= 1) cart.remove(entry.getKey());
                else cart.put(entry.getKey(), quantity - 1);
                refreshCart();
            });
            row.add(new JLabel(entry.getKey() + " × " + entry.getValue()), BorderLayout.CENTER);
            row.add(remove, BorderLayout.WEST);
            row.add(new JLabel("₹" + price * entry.getValue()), BorderLayout.EAST);
            cartPanel.add(row); cartPanel.add(new JSeparator());
        }
        double tax = subtotal * 0.05; double total = subtotal + tax;
        subtotalLabel.setText(String.format("Subtotal: ₹%.2f", subtotal));
        taxLabel.setText(String.format("Tax (5%%): ₹%.2f", tax));
        totalLabel.setText(String.format("Total: ₹%.2f", total));
        cartPanel.revalidate(); cartPanel.repaint();
    }

    private void checkout() {
        if (cart.isEmpty()) { JOptionPane.showMessageDialog(this, "Please select food items first."); return; }
        String orderType = (String) orderTypeBox.getSelectedItem();
        Integer table = null;
        if ("Dine-in".equals(orderType)) {
            try { table = Integer.valueOf((String) tableBox.getSelectedItem()); }
            catch (NumberFormatException | NullPointerException error) {
                JOptionPane.showMessageDialog(this, "Choose a valid table before checkout.", "Invalid Order", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        Map<String, Integer> prices = new LinkedHashMap<>();
        for (String item : cart.keySet()) prices.put(item, menuPrices.getOrDefault(item, 0));
        try {
            long id = orderStore.saveOrder(customerField.getText(), orderType, table,
                    (String) paymentBox.getSelectedItem(), new LinkedHashMap<>(cart), prices);
            JOptionPane.showMessageDialog(this, "Order saved successfully!\nOrder #: " + id,
                    "Checkout Complete", JOptionPane.INFORMATION_MESSAGE);
            cart.clear(); refreshCart();
        } catch (IllegalArgumentException error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Invalid Order", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException error) {
            JOptionPane.showMessageDialog(this, "Could not save the order.\n" + error.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}


