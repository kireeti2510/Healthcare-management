package com.healthcare.db;

import java.sql.*;

/**
 * SINGLETON PATTERN (Creational)
 * Member: Kireeti Reddy P (PES1UG23CS307)
 * Ensures only one DB connection instance exists in the JVM.
 */
public class DatabaseConnection {
    private static volatile DatabaseConnection instance;
    private Connection connection;

    // H2 in-memory database for clean startup each run.
    private static final String DB_URL = "jdbc:h2:mem:healthcare;DB_CLOSE_DELAY=-1";

    private DatabaseConnection() {
        try {
            Class.forName("org.h2.Driver");
            this.connection = DriverManager.getConnection(DB_URL);
            this.connection.setAutoCommit(true);
            System.out.println("[DB] Connected to H2: " + DB_URL);
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) instance = new DatabaseConnection();
            }
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB reconnect failed", e);
        }
        return connection;
    }

    public void close() {
        try { if (connection != null) connection.close(); }
        catch (SQLException e) { e.printStackTrace(); }
    }
}
