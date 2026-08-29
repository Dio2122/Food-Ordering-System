package com.ordering.system;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


// A window for new users to register for an account.
public class RegisterWindow extends JDialog {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton registerButton;
    private JButton cancelButton;

    public RegisterWindow(Frame owner) {
        super(owner, "Register", true); 

        setSize(400, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(255, 248, 231));

        JPanel headerPanel = new JPanel();
        JLabel headerLabel = new JLabel("Register New Account", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        headerLabel.setBackground(new Color(255, 248, 231));
        headerPanel.setBackground(new Color(255, 248, 231));


        headerPanel.add(headerLabel);
        add(headerPanel, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        fieldsPanel.setBackground(new Color(255, 248, 231));

        Font fieldFont = new Font("Arial", Font.PLAIN, 16);

        // Username
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(fieldFont);
        fieldsPanel.add(usernameLabel);

        usernameField = new JTextField();
        usernameField.setFont(fieldFont);
        fieldsPanel.add(usernameField);

        // Password
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(fieldFont);
        fieldsPanel.add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setFont(fieldFont);
        fieldsPanel.add(passwordField);

        // Confirm Password
        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordLabel.setFont(fieldFont);
        fieldsPanel.add(confirmPasswordLabel);

        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setFont(fieldFont);
        fieldsPanel.add(confirmPasswordField);
        add(fieldsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        registerButton = new JButton("Register");
        cancelButton = new JButton("Cancel");
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);
        buttonPanel.setBackground(new Color(255, 248, 231));
        add(buttonPanel, BorderLayout.SOUTH);

        registerButton.addActionListener(e -> performRegistration());
        cancelButton.addActionListener(e -> dispose()); // Close the dialog
    }


    private void performRegistration() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // Input validation
        if (username.isEmpty() || password.isEmpty()) {
        	if(username.isEmpty() && password.isEmpty()) {
        		JOptionPane.showMessageDialog(this, "Username and password cannot be empty.", "Registration Error", JOptionPane.ERROR_MESSAGE);
                return;
        	}
        	else {
        		if(username.isEmpty()) {
        			JOptionPane.showMessageDialog(this, "Username cannot be empty.", "Registration Error", JOptionPane.ERROR_MESSAGE);
                    return;
        		}
        		else {
        			JOptionPane.showMessageDialog(this, "Password cannot be empty.", "Registration Error", JOptionPane.ERROR_MESSAGE);
                    return;
        		}
        	}
        }
        
        if (username.length() < 4) {
             JOptionPane.showMessageDialog(this, "Username must be at least 4 characters long.", "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (password.length() < 6) {
             JOptionPane.showMessageDialog(this, "Password must be at least 6 characters long.", "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Check if username already exists
            if (userExists(username)) {
                JOptionPane.showMessageDialog(this, "Username already in use. Please try again.", "Registration Error", JOptionPane.ERROR_MESSAGE);
            } else {
                // Add the new user to the database
                addNewUser(username, password);
                JOptionPane.showMessageDialog(this, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose(); // Close the registration window
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error during registration.", "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }


    private boolean userExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Users WHERE Username = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    // Adds a new user to the database with the 'User' role.
    private void addNewUser(String username, String password) throws SQLException {
        // New users are always given the 'User' role
        String role = "User"; 
        String hashedPassword = PasswordUtils.hashPassword(password);
        
        String sql = "INSERT INTO Users (Username, PasswordHash, Role) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, role);
            pstmt.executeUpdate();
        }
    }
}
