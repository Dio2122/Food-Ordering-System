package com.ordering.system;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderHistory extends JFrame {

    private static final long serialVersionUID = 1L;
    private final User currentUser;
    private final DefaultListModel<String> historyListModel = new DefaultListModel<>();
    private final List<Integer> orderIds = new ArrayList<>();

    public OrderHistory(User user) {
        this.currentUser = user;

        setTitle("Order History");
        setSize(700, 500);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.WHITE); 

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(new Color(255, 248, 231)); 

        JButton backBtn = new JButton("← Back");
        backBtn.addActionListener(e -> {
            dispose();
            new Dashboard(currentUser).setVisible(true);
        });
        topPanel.add(backBtn, BorderLayout.WEST);

        JLabel title = new JLabel("Order History", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        topPanel.add(title, BorderLayout.CENTER);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBackground(new Color(255, 248, 231));

        JTextField yearField = new JTextField(4);
        JTextField monthField = new JTextField(2);
        JTextField dayField = new JTextField(2);

        ((AbstractDocument) yearField.getDocument()).setDocumentFilter(new NumberLimitFilter(4, 9999));
        ((AbstractDocument) monthField.getDocument()).setDocumentFilter(new NumberLimitFilter(2, 12));
        ((AbstractDocument) dayField.getDocument()).setDocumentFilter(new NumberLimitFilter(2, 31));

        filterPanel.add(new JLabel("Year:"));
        filterPanel.add(yearField);
        filterPanel.add(new JLabel("Month:"));
        filterPanel.add(monthField);
        filterPanel.add(new JLabel("Day:"));
        filterPanel.add(dayField);

        JButton filterButton = new JButton("Filter");
        filterPanel.add(filterButton);

        topPanel.add(filterPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        JList<String> orderList = new JList<>(historyListModel);
        orderList.setFont(new Font("Monospaced", Font.PLAIN, 14));
        orderList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        orderList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && orderList.getSelectedIndex() >= 0) {
                int orderId = orderIds.get(orderList.getSelectedIndex());
                new OrderDetailsPage(currentUser, orderId).setVisible(true);
                dispose();
            }
        });
        add(new JScrollPane(orderList), BorderLayout.CENTER);

        filterButton.addActionListener(e -> {
            String yyyy = yearField.getText().trim();
            String mm = monthField.getText().trim();
            String dd = dayField.getText().trim();

            String fullDate = (!yyyy.isEmpty() && !mm.isEmpty() && !dd.isEmpty())
                    ? yyyy + "-" + String.format("%02d", Integer.parseInt(mm)) + "-" + String.format("%02d", Integer.parseInt(dd))
                    : null;

            loadOrderHistory(fullDate);
        });

        // Initial Load (All Orders)
        loadOrderHistory(null);
    }

    public OrderHistory() {
        this.currentUser = new User();
    }

    private void loadOrderHistory(String dateFilter) {
        historyListModel.clear();
        orderIds.clear();

        String sql = """
            SELECT OrderID, OrderDate, TotalAmount
            FROM Orders
            WHERE UserID = ?
            ORDER BY OrderDate DESC
        """;

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentUser.getUserId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int orderId = rs.getInt("OrderID");
                    Date orderDate = rs.getDate("OrderDate");
                    String formattedDate = orderDate.toString();
                    String total = String.format("RM%.2f", rs.getBigDecimal("TotalAmount"));

                    if (dateFilter == null || dateFilter.isBlank() || formattedDate.equals(dateFilter)) {
                        orderIds.add(orderId);
                        historyListModel.addElement(String.format("ID #%04d  |  %s  |  %s", orderId, formattedDate, total));
                    }
                }
            }

            if (historyListModel.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No past orders found.", "Info", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                new Dashboard(currentUser).setVisible(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load order history: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private static class NumberLimitFilter extends DocumentFilter {
        private final int maxLength;
        private final int maxValue;

        public NumberLimitFilter(int maxLength, int maxValue) {
            this.maxLength = maxLength;
            this.maxValue = maxValue;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (string != null && string.matches("\\d+")) {
                StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
                sb.insert(offset, string);
                if (isValid(sb.toString())) {
                    super.insertString(fb, offset, string, attr);
                }
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text != null && text.matches("\\d+")) {
                StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
                sb.replace(offset, offset + length, text);
                if (isValid(sb.toString())) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        }

        private boolean isValid(String input) {
            if (input.length() > maxLength) return false;
            try {
                int val = Integer.parseInt(input);
                return val >= 0 && val <= maxValue;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }
}
