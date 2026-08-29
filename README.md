# Food Ordering & Management System (Java Swing + MS SQL Server)

A Java Swing desktop application featuring role-based authentication, real-time menu management, dynamic shopping cart workflows, transaction-managed order placement, and order history tracking backed by a Microsoft SQL Server database.

---

## System Architecture & Features

### 1. Authentication & Security
- **Role-Based Access Control (RBAC):** Distinct dashboards and capabilities for **Admin** and **Customer (User)** accounts.
- **Password Hashing:** Passwords are encrypted using **SHA-256** before database storage and authentication.
- **Automatic Seed Verification:** On startup, default administrative and standard user credentials are automatically verified and seeded into the database.

### 2. Customer Workflow
- **Live Menu Browsing:** View available food items with descriptions, real-time pricing, and loaded image previews.
- **Interactive Cart Management:** Dynamically increment, decrement, or remove items from the active basket with automatic real-time total calculations.
- **Atomic Order Placement:** Processes multi-item orders using SQL transactions (`commit`/`rollback`) across `Orders` and `OrderDetails` tables.
- **Order History & Inspection:** Filter past orders by date (Year, Month, Day) and drill down into itemised order receipts.

### 3. Administrator Workflow
- **Menu Management Dashboard:** Comprehensive CRUD operations to add new dishes, modify pricing/availability/descriptions, and remove menu entries with tabular oversight.

---

## Database Schema Overview

The system operates on a Microsoft SQL Server database (`FoodOrderingSystemDB`) with the following core entities:
- `Users` (`UserID`, `Username`, `PasswordHash`, `Role`)
- `FoodItems` (`ItemID`, `Name`, `Description`, `Price`, `IsAvailable`, `ImagePath`)
- `Orders` (`OrderID`, `UserID`, `OrderDate`, `TotalAmount`, `PaymentMethod`)
- `OrderDetails` (`OrderDetailID`, `OrderID`, `ItemID`, `Quantity`, `PricePerItem`)

---

## Getting Started

### Prerequisites

- **Java Development Kit (JDK):** Version 11 or higher.
- **Database:** Microsoft SQL Server with an active database named `FoodOrderingSystemDB`.
- **JDBC Driver:** Microsoft JDBC Driver for SQL Server (`mssql-jdbc.jar`).

### Database Configuration

1. Update the connection string in `src/com/ordering/system/DatabaseConnector.java` with your SQL Server instance name:
   ```java
   private static final String DB_URL = 
       "jdbc:sqlserver://<YOUR_SERVER_NAME>;databaseName=FoodOrderingSystemDB;integratedSecurity=true;encrypt=false;";
