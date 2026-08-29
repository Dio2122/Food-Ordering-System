package com.ordering.system;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;

public class OrderDetailsPage extends JFrame {

    private final User currentUser;
    private final int orderId;
    private final DefaultListModel<String> detailListModel = new DefaultListModel<>();
    private JLabel totalLabel;
    private JLabel paymentLabel;

    public OrderDetailsPage(User user, int orderId) {
        this.currentUser = user;
        this.orderId = orderId;

        setTitle("Order Details - ID " + orderId);
        setSize(700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel wrapper = new JPanel(new BorderLayout(10, 10));
        wrapper.setBorder(new EmptyBorder(15, 20, 15, 20));
        add(wrapper, BorderLayout.CENTER);

        JLabel header = new JLabel("Order #" + orderId, SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        wrapper.add(header, BorderLayout.NORTH);

        JPanel headerPanel = new JPanel(new GridLayout(1, 1));
        JLabel colHeader = new JLabel(String.format("%-25s %-10s %-10s", "Item Name", "Qty", "Price"), JLabel.LEFT);
        colHeader.setFont(new Font("Monospaced", Font.BOLD, 14));
        headerPanel.add(colHeader);
        wrapper.add(headerPanel, BorderLayout.BEFORE_FIRST_LINE);

        JList<String> itemList = new JList<>(detailListModel);
        itemList.setFont(new Font("Monospaced", Font.PLAIN, 14));
        wrapper.add(new JScrollPane(itemList), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));

        JButton backBtn = new JButton("Back to Dashboard");
        backBtn.addActionListener(e -> {
            dispose();
            new Dashboard(currentUser).setVisible(true);
        });
        bottomPanel.add(backBtn, BorderLayout.WEST);

        paymentLabel = new JLabel("Payment Method: ");
        paymentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        bottomPanel.add(paymentLabel, BorderLayout.CENTER);

        totalLabel = new JLabel("Total: RM0.00", SwingConstants.RIGHT);
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        bottomPanel.add(totalLabel, BorderLayout.EAST);

        wrapper.add(bottomPanel, BorderLayout.SOUTH);

        loadOrderDetails();
    }

    private void loadOrderDetails() {
        BigDecimal total = BigDecimal.ZERO;
        String paymentMethod = "";

        String sqlDetails = """
            SELECT f.Name, od.Quantity, od.PricePerItem, o.PaymentMethod
            FROM OrderDetails od
            JOIN FoodItems f ON f.ItemID = od.ItemID
            JOIN Orders o ON o.OrderID = od.OrderID
            WHERE od.OrderID = ?
        """;

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlDetails)) {

            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("Name");
                    int qty = rs.getInt("Quantity");
                    BigDecimal price = rs.getBigDecimal("PricePerItem");
                    BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(qty));
                    paymentMethod = rs.getString("PaymentMethod");

                    total = total.add(itemTotal);
                    detailListModel.addElement(String.format("%-25s %-10d RM%.2f", name, qty, itemTotal));
                }
            }

            totalLabel.setText(String.format("Total: RM%.2f", total));
            paymentLabel.setText("Payment Method: " + paymentMethod);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load order details: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
