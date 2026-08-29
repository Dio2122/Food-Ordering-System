package com.ordering.system;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class LoginWindow extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton; 

    public LoginWindow() {
        setTitle("Food Ordering System - Login");
        setSize(400, 270);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(255, 248, 231));

        JPanel headerPanel = new JPanel();
        JLabel headerLabel = new JLabel("Log In", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 30));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 5, 0));
        headerPanel.setBackground(new Color(255, 248, 231));
        headerLabel.setBackground(new Color(255, 248, 231));
        headerPanel.add(headerLabel);
        add(headerPanel, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        fieldsPanel.setBackground(new Color(255, 248, 231));


        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 16)); 
        fieldsPanel.add(usernameLabel);

        usernameField = new JTextField();
        usernameField.setFont(new Font("Arial", Font.PLAIN, 16)); 
        fieldsPanel.add(usernameField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        fieldsPanel.add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Arial", Font.PLAIN, 16));
        fieldsPanel.add(passwordField);

        add(fieldsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); 
        buttonPanel.setBackground(new Color(255, 248, 231));
        loginButton = new JButton("Login");
        registerButton = new JButton("Register"); 
        
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton); 
        add(buttonPanel, BorderLayout.SOUTH);

        loginButton.addActionListener(e -> performLogin());
        
        registerButton.addActionListener(e -> openRegisterWindow());

        passwordField.addActionListener(e -> performLogin());
    }
    
    // Opens the registration window
    private void openRegisterWindow() {
        RegisterWindow registerDialog = new RegisterWindow(this);
        registerDialog.setVisible(true);
    }

    // Handles the login logic when the login button is clicked.
    private void performLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() && password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password cannot be empty.", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        else if(username.isEmpty() || password.isEmpty()) {
        	if(username.isEmpty()) {
        		JOptionPane.showMessageDialog(this, "Please enter a valid username.", "Login Error", JOptionPane.ERROR_MESSAGE);
                return;
        	}
        	else {
        		JOptionPane.showMessageDialog(this, "Password field is empty.", "Login Error", JOptionPane.ERROR_MESSAGE);
                return;
        	}
        	
        }

        User user = authenticate(username, password);

        if (user != null) {
            // Open the appropriate dashboard
            if ("Admin".equalsIgnoreCase(user.getRole())) {
                SwingUtilities.invokeLater(() -> new AdminDashboard().setVisible(true));
            } else {
                SwingUtilities.invokeLater(() -> new Dashboard(user).setVisible(true));
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }


    private User authenticate(String username, String password) {
        String sql = "SELECT UserID, PasswordHash, Role FROM Users WHERE Username = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // User found, now verify password
                String storedHash = rs.getString("PasswordHash");
                if (PasswordUtils.verifyPassword(password, storedHash)) {
                    // Password is correct, create and return User object
                    int userId = rs.getInt("UserID");
                    String role = rs.getString("Role");
                    return new User(userId, username, role);
                }
            }
            // return if user was not found or password was incorrect
            return null;

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error during login. Please try again later.", "Database Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
}
