package com.ordering.system;
 
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
 

public class Main {
 
    public static void main(String[] args) {
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set system look and feel.");
            e.printStackTrace();
        }
 
        // Sets up the initial user accounts if they don't exist.
        setupInitialData();
        
        
        SwingUtilities.invokeLater(() -> {
            LoginWindow loginWindow = new LoginWindow();
            loginWindow.setVisible(true);
            
        });
        
    }
 
     // Sets up initial data
     
    private static void setupInitialData() {
        // --- ADMIN PASSWORD UPDATED HERE ---
        String adminUsername = "admin";
        String adminPasswordHash = PasswordUtils.hashPassword("password1");
       
        // Checks if 'admin' exists. If not, it inserts the admin
        String sql = "MERGE Users AS target " +
                     "USING (SELECT '" + adminUsername + "' AS Username) AS source " +
                     "ON (target.Username = source.Username) " +
                     "WHEN MATCHED THEN " +
                     "    UPDATE SET PasswordHash = '" + adminPasswordHash + "' " +
                     "WHEN NOT MATCHED THEN " +
                     "    INSERT (Username, PasswordHash, Role) " +
                     "    VALUES ('" + adminUsername + "', '" + adminPasswordHash + "', 'Admin');";
                     
        String userUsername = "customer";
        String userPasswordHash = PasswordUtils.hashPassword("password2");
 
        String userSql = "MERGE Users AS target " +
                         "USING (SELECT '" + userUsername + "' AS Username) AS source " +
                         "ON (target.Username = source.Username) " +
                         "WHEN NOT MATCHED THEN " +
                         "    INSERT (Username, PasswordHash, Role) " +
                         "    VALUES ('" + userUsername + "', '" + userPasswordHash + "', 'User');";
 
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement()) {
             
            System.out.println("Checking for initial admin data...");
            stmt.executeUpdate(sql);
            System.out.println("Admin account check complete.");
 
            System.out.println("Checking for initial user data...");
            stmt.executeUpdate(userSql);
            System.out.println("Default user account check complete.");
 
        } catch (SQLException e) {
            System.err.println("Error during initial data setup. The application may not have an admin account.");
            e.printStackTrace();
        }
    }
}