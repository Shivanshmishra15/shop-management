package com.shopapp.database;

import com.shopapp.models.User;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.Optional;

public class UserDAO {

    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    public Optional<User> authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                
                // Secure Authentication: Only allow BCrypt hashes
                if (storedPassword != null && storedPassword.startsWith("$2a$")) {
                    if (BCrypt.checkpw(password, storedPassword)) {
                        User user = new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            storedPassword,
                            rs.getString("role"),
                            rs.getString("fullname")
                        );
                        logger.info("User '{}' authenticated successfully.", username);
                        return Optional.of(user);
                    } else {
                        logger.warn("Invalid password attempt for user: {}", username);
                    }
                } else {
                    logger.error("Authentication failed for user '{}': Password not hashed.", username);
                }
            } else {
                logger.warn("Authentication attempt for non-existent user: {}", username);
            }
        } catch (SQLException e) {
            logger.error("Database error during authentication: {}", e.getMessage(), e);
        }
        return Optional.empty();
    }
}
