package com.sri.netbanking.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserStore {

    public static boolean register(String username, String password, String fullName, String email, String phoneNumber) {
        String query = "INSERT INTO users (username, password, full_name, email, phone_number) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username.trim().toLowerCase());
            pstmt.setString(2, password);
            pstmt.setString(3, fullName.trim());
            pstmt.setString(4, email.trim());
            pstmt.setString(5, phoneNumber.trim());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Database error during registration: " + e.getMessage());
            return false;
        }
    }

    public static boolean authenticate(String username, String password) {
        String query = "SELECT password FROM users WHERE LOWER(username) = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username.trim().toLowerCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    return storedPassword != null && storedPassword.equals(password);
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error during authentication: " + e.getMessage());
        }
        return false;
    }

    public static boolean exists(String username) {
        String query = "SELECT 1 FROM users WHERE LOWER(username) = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username.trim().toLowerCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Database error during existence check: " + e.getMessage());
            return false;
        }
    }
}
