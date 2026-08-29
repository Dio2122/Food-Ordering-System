package com.ordering.system;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Map;

public class ViewCartDetails extends JFrame {

    private final User currentUser;
    private final Map<FoodItem, Integer> cartItems;
    private final JPanel cartPanel = new JPanel();
    private JLabel totalLabel;
    private JComboBox<String> paymentCombo;

    public ViewCartDetails(User user, Map<FoodItem, Integer> cartItems) {
        this.currentUser = user;
        this.cartItems = cartItems;

        setTitle("Your Cart");
        setSize(850, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel contentWrapper = new JPanel(new BorderLayout(10, 10));
        contentWrapper.setBorder(new EmptyBorder(15, 20, 15, 20));
        add(contentWrapper, BorderLayout.CENTER);

        JLabel title = new JLabel("Your Cart", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        contentWrapper.add(title, BorderLayout.NORTH);

        cartPanel.setLayout(new BoxLayout(cartPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(cartPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        contentWrapper.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(new EmptyBorder(10, 5, 10, 5));
        contentWrapper.add(bottomPanel, BorderLayout.SOUTH);

        JLabel payLabel = new JLabel("Payment Method:");
        payLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        payLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        paymentCombo = new JComboBox<>(new String[]{"Cash"});
        paymentCombo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        paymentCombo.setMaximumSize(new Dimension(200, 40));
        paymentCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        bottomPanel.add(payLabel);
        bottomPanel.add(paymentCombo);
        bottomPanel.add(Box.createVerticalStrut(10));

        totalLabel = new JLabel("Total: RM0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        totalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottomPanel.add(totalLabel);
        bottomPanel.add(Box.createVerticalStrut(10));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton backButton = new JButton("Back");
        JButton placeOrderBtn = new JButton("Place Order");

        placeOrderBtn.setBackground(new Color(76, 175, 80));
        placeOrderBtn.setForeground(Color.BLACK);
        placeOrderBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));

        backButton.addActionListener(e -> {
            dispose();
            new BrowseMenu(currentUser, cartItems).setVisible(true);
        });

        placeOrderBtn.addActionListener(e -> placeOrder());

        buttonPanel.add(backButton);
        buttonPanel.add(placeOrderBtn);
        bottomPanel.add(buttonPanel);

        updateCartDisplay();
    }

    private void updateCartDisplay() {
        cartPanel.removeAll();
        BigDecimal total = BigDecimal.ZERO;

        if (cartItems.isEmpty()) {
            JLabel emptyLabel = new JLabel("Cart is empty.");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 18));
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            cartPanel.setLayout(new BorderLayout());
            cartPanel.add(emptyLabel, BorderLayout.CENTER);
            cartPanel.revalidate();
            cartPanel.repaint();
            return;
        }

        cartPanel.setLayout(new BoxLayout(cartPanel, BoxLayout.Y_AXIS)); // Reset

        for (Map.Entry<FoodItem, Integer> entry : cartItems.entrySet()) {
            FoodItem item = entry.getKey();
            int qty = entry.getValue();
            BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(qty));
            total = total.add(itemTotal);

            JPanel itemPanel = new JPanel(new BorderLayout(10, 10));
            itemPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
            itemPanel.setPreferredSize(new Dimension(800, 100));

            JLabel imageLabel = new JLabel();
            imageLabel.setPreferredSize(new Dimension(80, 80));
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            try {
                imageLabel.setIcon(new ImageIcon(new URL(item.getImagePath())));
            } catch (Exception e) {
                imageLabel.setText("No Image");
            }
            itemPanel.add(imageLabel, BorderLayout.WEST);

            JPanel centerPanel = new JPanel();
            centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
            JLabel nameLabel = new JLabel(item.getName());
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            JButton minusBtn = new JButton("−");
            JLabel qtyLabel = new JLabel(String.valueOf(qty));
            JButton plusBtn = new JButton("+");
            JButton removeBtn = new JButton("Remove");

            qtyLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            minusBtn.setPreferredSize(new Dimension(45, 30));
            plusBtn.setPreferredSize(new Dimension(45, 30));
            removeBtn.setPreferredSize(new Dimension(90, 30));

            minusBtn.addActionListener(e -> {
                if (cartItems.get(item) > 1) {
                    cartItems.put(item, cartItems.get(item) - 1);
                } else {
                    cartItems.remove(item);
                }
                updateCartDisplay();
            });

            plusBtn.addActionListener(e -> {
                cartItems.put(item, cartItems.get(item) + 1);
                updateCartDisplay();
            });

            removeBtn.addActionListener(e -> {
                cartItems.remove(item);
                updateCartDisplay();
            });

            qtyPanel.add(minusBtn);
            qtyPanel.add(qtyLabel);
            qtyPanel.add(plusBtn);
            qtyPanel.add(removeBtn);

            centerPanel.add(nameLabel);
            centerPanel.add(qtyPanel);

            itemPanel.add(centerPanel, BorderLayout.CENTER);

            JLabel itemTotalLabel = new JLabel(String.format("RM%.2f", itemTotal));
            itemTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            itemTotalLabel.setBorder(new EmptyBorder(0, 10, 0, 20));
            itemTotalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            itemPanel.add(itemTotalLabel, BorderLayout.EAST);

            cartPanel.add(itemPanel);
            cartPanel.add(Box.createVerticalStrut(8));
        }

        totalLabel.setText(String.format("Total: RM%.2f", total));
        cartPanel.revalidate();
        cartPanel.repaint();
    }

    private void placeOrder() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String paymentMethod = (String) paymentCombo.getSelectedItem();

        try (Connection conn = DatabaseConnector.getConnection()) {
            conn.setAutoCommit(false);

            String insertOrder = "INSERT INTO Orders (UserID, TotalAmount, PaymentMethod) VALUES (?, ?, ?)";
            BigDecimal totalAmount = new BigDecimal(totalLabel.getText().replace("Total: RM", ""));

            int orderId;
            try (PreparedStatement orderStmt = conn.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS)) {
                orderStmt.setInt(1, currentUser.getUserId());
                orderStmt.setBigDecimal(2, totalAmount);
                orderStmt.setString(3, paymentMethod);
                orderStmt.executeUpdate();

                try (ResultSet keys = orderStmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        orderId = keys.getInt(1);
                    } else {
                        throw new SQLException("Failed to get order ID.");
                    }
                }
            }

            String insertDetail = "INSERT INTO OrderDetails (OrderID, ItemID, Quantity, PricePerItem) VALUES (?, ?, ?, ?)";
            try (PreparedStatement detailStmt = conn.prepareStatement(insertDetail)) {
                for (Map.Entry<FoodItem, Integer> entry : cartItems.entrySet()) {
                    detailStmt.setInt(1, orderId);
                    detailStmt.setInt(2, entry.getKey().getItemId());
                    detailStmt.setInt(3, entry.getValue());
                    detailStmt.setBigDecimal(4, entry.getKey().getPrice());
                    detailStmt.addBatch();
                }
                detailStmt.executeBatch();
            }

            conn.commit();
            cartItems.clear();
            JOptionPane.showMessageDialog(this, "Order placed successfully. Order ID: " + orderId, "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new OrderDetailsPage(currentUser, orderId).setVisible(true);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to place order: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
