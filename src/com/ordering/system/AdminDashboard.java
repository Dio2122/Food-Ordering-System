package com.ordering.system;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;


public class AdminDashboard extends JFrame {


    private static final int NAME_MAX_LEN = 100;
    private static final int DESC_MAX_LEN = 255;
    private static final int IMAGE_MAX_LEN = 512;

    private JTable foodItemsTable;
    private DefaultTableModel tableModel;
    private ArrayList<FoodItem> foodItemsList = new ArrayList<>();

    public AdminDashboard() {
        setTitle("Admin Dashboard");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        Color bgColor = new Color(255, 248, 231);
        getContentPane().setBackground(bgColor);

        // Outer header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(bgColor); // header bg

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topRow.setBackground(bgColor); // top row bg

        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomRow.setBackground(bgColor); // bottom row bg
        // Top row
        JButton logoutButton = new JButton(" Logout ");
        logoutButton.setFont(new Font("Arial", Font.BOLD, 15));
        logoutButton.setBackground(new Color(255, 200, 200)); // very light pink
        logoutButton.setOpaque(true);
        logoutButton.setBorder(BorderFactory.createLineBorder(new Color(255, 150, 150, 80), 1)); // softened border
        logoutButton.setFocusPainted(false); // remove focus border highlight
        logoutButton.addActionListener(e -> {
            dispose();
            new LoginWindow().setVisible(true);
        });
        topRow.add(logoutButton);

        JLabel title = new JLabel("Menu Management", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 30));
        bottomRow.add(title);

        // Add both rows to the header
        headerPanel.add(topRow, BorderLayout.NORTH);
        headerPanel.add(bottomRow, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH); // Add to main frame

        // Table panel
        add(createFoodTablePanel(), BorderLayout.CENTER);

        // Buttons
        add(createButtonPanel(), BorderLayout.SOUTH);

        loadFoodItems();
    }

    private JPanel createFoodTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        Color bgColor = new Color(255, 248, 231);

        String[] columnNames = {"ID", "Name", "Description", "Price (RM)", "Available", "Image URL"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        foodItemsTable = new JTable(tableModel);
        foodItemsTable.setRowHeight(25);
        foodItemsTable.getColumnModel().getColumn(0).setMaxWidth(40);  // ID
        foodItemsTable.getColumnModel().getColumn(3).setMaxWidth(80);  // Price
        foodItemsTable.getColumnModel().getColumn(4).setMaxWidth(80);  // Available

        // Table header styling
        foodItemsTable.getTableHeader().setBackground(new Color(255, 235, 205)); 
        foodItemsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        // Alternate row colors
        foodItemsTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(new Color(255, 250, 240)); 
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                } else {
                    c.setBackground(new Color(220, 240, 255));
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(foodItemsTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.setBackground(bgColor);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setPreferredSize(new Dimension(getWidth(), 150));
        buttonPanel.setBackground(new Color(255, 248, 231)); 

        // Buttons
        JButton addButton = new JButton("Add Item");
        JButton editButton = new JButton("Edit Item");
        JButton deleteButton = new JButton("Delete Item");

        Font buttonFont = new Font("Arial", Font.BOLD, 16);
        Dimension buttonSize = new Dimension(150, 40);

        for (JButton btn : new JButton[]{addButton, editButton, deleteButton}) {
            btn.setFont(buttonFont);
            btn.setPreferredSize(buttonSize);
            btn.setFocusPainted(false);
            btn.setOpaque(true);

            addButton.setBackground(new Color(220, 255, 220)); 
            addButton.setBorder(BorderFactory.createLineBorder(Color.GREEN, 2));

            editButton.setBackground(new Color(255, 255, 200));
            editButton.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 2));

            deleteButton.setBackground(new Color(255, 230, 230)); 
            deleteButton.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
        }

        JPanel innerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        innerPanel.setOpaque(false);
        innerPanel.add(addButton);
        innerPanel.add(editButton);
        innerPanel.add(deleteButton);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;

        buttonPanel.add(innerPanel, gbc);

        addButton.addActionListener(e -> showFoodItemDialog(null));
        editButton.addActionListener(e -> {
            int selectedRow = foodItemsTable.getSelectedRow();
            if (selectedRow >= 0) {
                showFoodItemDialog(foodItemsList.get(selectedRow));
            } else {
                JOptionPane.showMessageDialog(this, "Please select an item to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });
        deleteButton.addActionListener(e -> deleteSelectedItem());

        return buttonPanel;
    }

    private void loadFoodItems() {
        foodItemsList.clear();
        tableModel.setRowCount(0);

        String sql = "SELECT ItemID, Name, Description, Price, IsAvailable, ImagePath FROM FoodItems ORDER BY ItemID";
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
                        rs.getString("ImagePath") // Load the new field
                );
                foodItemsList.add(item);
                tableModel.addRow(new Object[]{
                        item.getItemId(), item.getName(), item.getDescription(),
                        String.format("%.2f", item.getPrice()),
                        item.isAvailable() ? "Yes" : "No",
                        item.getImagePath()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load food items: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void showFoodItemDialog(FoodItem item) {
        // Fields
        JTextField nameField = new JTextField(20);
        JTextField descriptionField = new JTextField(20);
        JTextField priceField = new JTextField(10);
        JTextField imagePathField = new JTextField(20);
        JCheckBox availableCheckBox = new JCheckBox("Is Available", true);

        boolean isEditMode = item != null;
        if (isEditMode) {
            nameField.setText(item.getName());
            descriptionField.setText(item.getDescription());
            priceField.setText(item.getPrice().toString());
            availableCheckBox.setSelected(item.isAvailable());
            imagePathField.setText(item.getImagePath());
        }

        // Layout
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        form.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; form.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        form.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; form.add(descriptionField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        form.add(new JLabel("Price (RM):"), gbc);
        gbc.gridx = 1; form.add(priceField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        form.add(new JLabel("Image URL:"), gbc);
        gbc.gridx = 1; form.add(imagePathField, gbc);

        gbc.gridx = 1; gbc.gridy++;
        form.add(availableCheckBox, gbc);

        // Custom dialog
        JDialog dialog = new JDialog(this, isEditMode ? "Edit Food Item" : "Add Food Item", true);
        dialog.setLayout(new BorderLayout());
        dialog.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        actions.add(saveBtn);
        actions.add(cancelBtn);
        dialog.add(actions, BorderLayout.SOUTH);

        // Save action with validation
        saveBtn.addActionListener(ev -> {
            String name = nameField.getText().trim();
            String description = descriptionField.getText().trim();
            String priceText = priceField.getText().trim();
            String imagePath = imagePathField.getText().trim();
            boolean isAvailable = availableCheckBox.isSelected();

            // Empty field checks
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Name cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (description.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Description cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (priceText.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Price cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (imagePath.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Image URL cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Invalid field checks
            if (name.length() > NAME_MAX_LEN) {
                JOptionPane.showMessageDialog(dialog, "Name is too long, please try again.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (description.length() > DESC_MAX_LEN) {
                JOptionPane.showMessageDialog(dialog, "Description is too long, please try again.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (imagePath.length() > IMAGE_MAX_LEN) {
                JOptionPane.showMessageDialog(dialog, "Image URL is too long, please try again.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Price checks
            BigDecimal price;
            try {
                price = new BigDecimal(priceText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid price format. Please enter a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(dialog, "Price must be greater than zero.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Save to DB if all good
            try {
                if (isEditMode) {
                    updateFoodItem(item.getItemId(), name, description, price, isAvailable, imagePath);
                } else {
                    addFoodItem(name, description, price, isAvailable, imagePath);
                }
                loadFoodItems();
                dialog.dispose(); 
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Unexpected error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        cancelBtn.addActionListener(ev -> dialog.dispose());

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true); 
    }

    private void addFoodItem(String name, String description, BigDecimal price, boolean isAvailable, String imagePath) {
        String sql = "INSERT INTO FoodItems (Name, Description, Price, IsAvailable, ImagePath) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setBigDecimal(3, price);
            pstmt.setBoolean(4, isAvailable);
            pstmt.setString(5, imagePath);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to add item: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void updateFoodItem(int itemId, String name, String description, BigDecimal price, boolean isAvailable, String imagePath) {
        String sql = "UPDATE FoodItems SET Name = ?, Description = ?, Price = ?, IsAvailable = ?, ImagePath = ? WHERE ItemID = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setBigDecimal(3, price);
            pstmt.setBoolean(4, isAvailable);
            pstmt.setString(5, imagePath);
            pstmt.setInt(6, itemId);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to update item: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }


    private void deleteSelectedItem() {
        int selectedRow = foodItemsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this item?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            FoodItem selectedItem = foodItemsList.get(selectedRow);
            String sql = "DELETE FROM FoodItems WHERE ItemID = ?";
            try (Connection conn = DatabaseConnector.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, selectedItem.getItemId());
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this, "Item deleted successfully.");
                    loadFoodItems();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete item.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting item: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
}
