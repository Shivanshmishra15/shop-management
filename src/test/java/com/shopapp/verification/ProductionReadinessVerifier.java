package com.shopapp.verification;

import com.shopapp.database.DatabaseManager;
import com.shopapp.database.UserDAO;
import com.shopapp.models.User;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;
import java.util.Optional;

public class ProductionReadinessVerifier {

    public static void main(String[] args) {
        System.out.println("=== Starting Production Readiness Verification ===");
        
        try {
            // 1. Setup - Ensure database is initialized
            DatabaseManager.init();
            
            // 2. Validate Password Migration
            validatePasswordMigration();
            
            // 3. Validate Secure Authentication
            validateAuthentication();
            
            System.out.println("\n=== Verification Successful! ===");
        } catch (Exception e) {
            System.err.println("\n!!! Verification Failed !!!");
            e.printStackTrace();
        } finally {
            DatabaseManager.shutdown();
        }
    }

    private static void validatePasswordMigration() throws SQLException {
        System.out.println("\n[1] Validating Password Migration...");
        String sql = "SELECT username, password FROM users";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String username = rs.getString("username");
                String password = rs.getString("password");
                
                if (password.startsWith("$2a$")) {
                    System.out.println("SUCCESS: User '" + username + "' has a hashed password.");
                } else {
                    throw new RuntimeException("FAILURE: User '" + username + "' still has a plaintext password: " + password);
                }
            }
        }
    }

    private static void validateAuthentication() {
        System.out.println("\n[2] Validating Secure Authentication...");
        UserDAO userDAO = new UserDAO();
        
        // Test valid login (assuming admin/admin123 was migrated)
        System.out.print("Testing 'admin/admin123' authentication... ");
        Optional<User> admin = userDAO.authenticate("admin", "admin123");
        if (admin.isPresent()) {
            System.out.println("SUCCESS");
        } else {
            System.out.println("FAILED");
            throw new RuntimeException("Authentication failed for valid credentials (admin/admin123)");
        }
        
        // Test invalid password
        System.out.print("Testing 'admin/wrongpass' authentication (should fail)... ");
        Optional<User> wrongPass = userDAO.authenticate("admin", "wrongpass");
        if (wrongPass.isEmpty()) {
            System.out.println("SUCCESS (failed as expected)");
        } else {
            System.out.println("FAILED (authenticated with wrong password)");
            throw new RuntimeException("Security vulnerability: Authenticated with wrong password!");
        }
    }
}
