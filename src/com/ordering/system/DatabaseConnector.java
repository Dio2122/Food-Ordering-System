package com.ordering.system;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


 // Handles the connection to the Microsoft SQL Server database using Windows Authentication.
 
public class DatabaseConnector {

    private static final String DB_URL = 
        "jdbc:sqlserver://BRANDON;databaseName=FoodOrderingSystemDB;integratedSecurity=true;encrypt=false;";

    // The setup requires the 'mssql-jdbc_auth' DLL file.
    // Ensure the -Djava.library.path VM argument is configured in Eclipse.
    
    private static Connection connection = null;

    // Private constructor to prevent instantiation of this utility class.
    private DatabaseConnector() {}

    /**
     * Establishes a connection to the database if one doesn't already exist.
     *
     * @return The active database Connection object.
     * @throws SQLException if a database access error occurs.
     */
    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // The driver class name is the same
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                
                System.out.println("Attempting to connect to URL: " + DB_URL);
                connection = DriverManager.getConnection(DB_URL);
                System.out.println("Database connection established successfully.");

            } catch (ClassNotFoundException e) {
                System.err.println("Error: MS SQL Server JDBC Driver not found.");
                e.printStackTrace();
                throw new SQLException("JDBC Driver not found.", e);

            } catch (SQLException e) {
                // --- ENHANCED ERROR LOGGING ---
                // This will give us the specific reason for the connection failure.
                System.err.println("Error: Failed to connect to the database. See details below.");
                System.err.println("---------------------------------------------------------------------------------");
                System.err.println("TROUBLESHOOTING CHECKLIST:");
                System.err.println("1. Is the server name in DB_URL *exactly* correct (e.g., 'localhost\\SQLEXPRESS')?");
                System.err.println("2. Is the SQL Server Browser service running?");
                System.err.println("3. Is the main SQL Server service running?");
                System.err.println("4. Is TCP/IP enabled for your SQL instance?");
                System.err.println("5. Did you set the '-Djava.library.path' VM argument correctly in Eclipse Run Configurations?");
                System.err.println("---------------------------------------------------------------------------------");
                
                // Print detailed SQL exception information
                System.err.println("SQLException: " + e.getMessage());
                System.err.println("SQLState: " + e.getSQLState());
                System.err.println("VendorError: " + e.getErrorCode());
                System.err.println("---------------------------------------------------------------------------------");
                e.printStackTrace();
                throw e;
            }
        }
        return connection;
    }

    /**
     * Closes the database connection if it is open.
     */
    public static synchronized void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("Database connection closed.");
                }
            } catch (SQLException e) {
                System.err.println("Error: Failed to close the database connection.");
                e.printStackTrace();
            } finally {
                connection = null;
            }
        }
    }
}
