package com.ordering.system;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.sql.*;
import java.util.Map;

public class BrowseMenu extends JFrame {

    private final User currentUser;
    private final DefaultListModel<FoodItem> menuListModel = new DefaultListModel<>();
    private final Map<FoodItem, Integer> cartItems;
    private JPanel itemsPanel;

    public BrowseMenu(User user, Map<FoodItem, Integer> cartItems) {
        this.currentUser = user;
        this.cartItems = cartItems;

        setTitle("Browse Menu");
        setSize(900, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new BorderLayout());
        JButton backButton = new JButton("← Back");
        JLabel title = new JLabel("Browse Menu", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        topPanel.add(backButton, BorderLayout.WEST);
        topPanel.add(title, BorderLayout.CENTER);
        backButton.addActionListener(e -> {
            dispose();
            new Dashboard(currentUser).setVisible(true);
        });
        add(topPanel, BorderLayout.NORTH);

        itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(itemsPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton viewCartButton = new JButton("View Cart");
        viewCartButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        viewCartButton.setBackground(new Color(220, 220, 220));
        viewCartButton.setForeground(Color.BLACK);
        viewCartButton.setPreferredSize(new Dimension(140, 40));
        viewCartButton.setFocusPainted(false);
        viewCartButton.addActionListener(e -> {
            dispose();
            new ViewCartDetails(currentUser, cartItems).setVisible(true);
        });

        bottomPanel.add(viewCartButton);
        add(bottomPanel, BorderLayout.SOUTH);

        loadMenuItems();
    }

    private void loadMenuItems() {
        menuListModel.clear();
        itemsPanel.removeAll();

        String sql = "SELECT ItemID, Name, Description, Price, IsAvailable, ImagePath FROM FoodItems ORDER BY Name";
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                FoodItem item = new FoodItem(
                        rs.getInt("ItemID"),
                        rs.getString("Name"),
                        rs.getString("Description"),
                        rs.getBigDecimal("Price"),
                        rs.getBoolean("IsAvailable"),
                        rs.getString("ImagePath")
                );

                itemsPanel.add(createItemPanel(item));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load menu: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        itemsPanel.revalidate();
        itemsPanel.repaint();
    }

    private JPanel createItemPanel(FoodItem item) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.setPreferredSize(new Dimension(850, 120));

        // Grey out if item unavailable
        if (!item.isAvailable()) {
            panel.setBackground(new Color(230, 230, 230));
        }

        // Food item image
        JLabel imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(120, 100));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        String imagePath = item.getImagePath();

        if (imagePath == null || imagePath.trim().isEmpty()) {
            imageLabel.setText("No Image");
        } else {
            try {
                URL imageUrl = new URL(imagePath);
                ImageIcon rawIcon = new ImageIcon(imageUrl);
                Image scaledImage = rawIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaledImage));
            } catch (Exception e) {
                e.printStackTrace(); 
                imageLabel.setText("No Image");
            }
        }

        panel.add(imageLabel, BorderLayout.WEST);

        // Item information
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(item.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JTextArea descArea = new JTextArea(item.getDescription());
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setOpaque(false);

        JLabel priceLabel = new JLabel("RM " + item.getPrice());
        priceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        priceLabel.setForeground(Color.BLACK);

        // Unavailable label
        JLabel unavailableLabel = new JLabel(" (Unavailable)");
        unavailableLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        unavailableLabel.setForeground(Color.RED);

        JPanel bottomText = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bottomText.setOpaque(false);
        bottomText.add(priceLabel);
        if (!item.isAvailable()) {
            bottomText.add(unavailableLabel);
        }

        textPanel.add(nameLabel, BorderLayout.NORTH);
        textPanel.add(descArea, BorderLayout.CENTER);
        textPanel.add(bottomText, BorderLayout.SOUTH);
        panel.add(textPanel, BorderLayout.CENTER);

        // Add Button
        JButton addButton = new JButton("+");
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 20));
        addButton.setPreferredSize(new Dimension(50, 50));
        addButton.setFocusPainted(false);
        addButton.setEnabled(item.isAvailable());

        if (item.isAvailable()) {
            addButton.addActionListener(e -> {
                cartItems.put(item, cartItems.getOrDefault(item, 0) + 1);
                JOptionPane.showMessageDialog(this, item.getName() + " added to cart!", "Added", JOptionPane.INFORMATION_MESSAGE);
            });
        }

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.add(addButton);
        panel.add(buttonPanel, BorderLayout.EAST);

        return panel;
    }
}
