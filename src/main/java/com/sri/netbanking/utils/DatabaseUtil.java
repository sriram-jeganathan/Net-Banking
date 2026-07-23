package com.sri.netbanking.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseUtil {
    private static final String HOST = "localhost";
    private static final String PORT = "3306";
    private static final String DB_NAME = "netbanking";
    private static final String DB_USER = "sri";
    private static final String DB_PASSWORD = "root";

    private static final String BASE_URL = "jdbc:mariadb://" + HOST + ":" + PORT + "/";
    private static final String DB_URL = BASE_URL + DB_NAME;

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MariaDB JDBC Driver not found", e);
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static void initializeDatabase() {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            
            // Connect to server (without DB name) to create database if not exists
            try (Connection conn = DriverManager.getConnection(BASE_URL, DB_USER, DB_PASSWORD);
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            }
            
            // Connect to the database to initialize tables
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {
                String createTableSQL = "CREATE TABLE IF NOT EXISTS users ("
                        + "id INT AUTO_INCREMENT PRIMARY KEY,"
                        + "username VARCHAR(50) NOT NULL UNIQUE,"
                        + "password VARCHAR(255) NOT NULL,"
                        + "full_name VARCHAR(100) NOT NULL,"
                        + "email VARCHAR(100) NOT NULL,"
                        + "phone_number VARCHAR(20) NOT NULL,"
                        + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                        + ")";
                stmt.executeUpdate(createTableSQL);
            }
        } catch (Exception e) {
            System.err.println(ConsoleUtil.ANSI_RED + "Database initialization failed: " + e.getMessage() + ConsoleUtil.ANSI_RESET);
            System.err.println(ConsoleUtil.ANSI_YELLOW + "Make sure MariaDB is running on localhost:3306 with user 'sri' and password 'root'." + ConsoleUtil.ANSI_RESET);
        }
    }
}
