package com.saffronstone.pos.ui;

import javax.swing.*;
import com.saffronstone.pos.AuthRepository;
import com.saffronstone.pos.DBConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.awt.*;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame() {

        setTitle("Saffron & Stone - Restaurant POS");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(new Color(248, 247, 244));

        JLabel title = new JLabel(
                "SAFFRON & STONE",
                SwingConstants.CENTER
        );

        title.setFont(new Font("Serif", Font.BOLD, 28));
        title.setForeground(new Color(49, 91, 66));

        mainPanel.add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(5, 1, 10, 10));
        formPanel.setBorder(
                BorderFactory.createEmptyBorder(40, 70, 40, 70)
        );
        formPanel.setBackground(Color.WHITE);

        JLabel usernameLabel = new JLabel("Username");

        usernameField = new JTextField();

        JLabel passwordLabel = new JLabel("Password");

        passwordField = new JPasswordField();

        JButton loginButton = new JButton("Login");

        loginButton.setBackground(new Color(49, 91, 66));
        loginButton.setForeground(Color.WHITE);

        formPanel.add(usernameLabel);
        formPanel.add(usernameField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);
        formPanel.add(loginButton);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        JLabel databaseStatus = new JLabel("Connecting to SQLite...", SwingConstants.CENTER);
        try (Connection ignored = DBConnection.getConnection()) {
            databaseStatus.setText("SQLite database connected  |  Demo login: admin / 1234");
            databaseStatus.setForeground(new Color(49, 91, 66));
        } catch (SQLException error) {
            databaseStatus.setText("Database connection failed: " + error.getMessage());
            databaseStatus.setForeground(new Color(170, 45, 45));
            loginButton.setEnabled(false);
        }
        mainPanel.add(databaseStatus, BorderLayout.SOUTH);

        loginButton.addActionListener(e -> login());

        add(mainPanel);
    }

    private void login() {
        String username = usernameField.getText().trim();
        char[] password = passwordField.getPassword();
        try {
            if (AuthRepository.authenticate(username, password)) {
                JOptionPane.showMessageDialog(this, "Login successful!");
                dispose();
                new DashboardFrame().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password");
            }
        } catch (SQLException error) {
            JOptionPane.showMessageDialog(this, "Could not check login with the database.\n" + error.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            java.util.Arrays.fill(password, '\0');
            passwordField.setText("");
        }
    }
}

