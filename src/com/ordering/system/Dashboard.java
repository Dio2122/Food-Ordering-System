package com.ordering.system;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Dashboard extends JFrame {
    private final User currentUser;
    private final Map<FoodItem, Integer> cartItems;

    public Dashboard(User user) {
        this.currentUser = user;
        this.cartItems = new HashMap<>();

        setTitle("Customer Dashboard");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        Color backgroundColor = new Color(255, 248, 231); 
        getContentPane().setBackground(backgroundColor);

        JPanel outerTopPanel = new JPanel(new BorderLayout());
        outerTopPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 0, 20));
        outerTopPanel.setBackground(backgroundColor);

        JPanel logoutPanel = new JPanel(new BorderLayout());
        logoutPanel.setOpaque(false);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setPreferredSize(new Dimension(100, 30));
        logoutBtn.setBackground(new Color(255, 200, 200)); 
        logoutBtn.setBorder(BorderFactory.createLineBorder(new Color(255, 150, 150, 80), 1)); 
        logoutBtn.setFocusPainted(false); 
        logoutBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginWindow().setVisible(true); 
        });
        logoutPanel.add(logoutBtn, BorderLayout.EAST);

        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(backgroundColor);

        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titlePanel.add(title);

        outerTopPanel.add(logoutPanel, BorderLayout.NORTH);
        outerTopPanel.add(titlePanel, BorderLayout.SOUTH);

        add(outerTopPanel, BorderLayout.NORTH);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(backgroundColor);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 1, 15, 15));
        buttonPanel.setPreferredSize(new Dimension(250, 130)); 
        buttonPanel.setBackground(backgroundColor);

        JButton browseMenu = new JButton("Browse Menu");
        JButton orderHistory = new JButton("Order History");

        browseMenu.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        orderHistory.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        browseMenu.setPreferredSize(new Dimension(250, 50));
        orderHistory.setPreferredSize(new Dimension(250, 50));

        browseMenu.addActionListener(e -> {
            dispose();
            new BrowseMenu(currentUser, cartItems).setVisible(true);
        });

        orderHistory.addActionListener(e -> {
            dispose();
            new OrderHistory(currentUser).setVisible(true);
        });

        buttonPanel.add(browseMenu);
        buttonPanel.add(orderHistory);

        centerWrapper.add(buttonPanel);
        add(centerWrapper, BorderLayout.CENTER);
    }
}
