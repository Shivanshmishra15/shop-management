package com.shopapp.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:shop.db";
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static HikariDataSource dataSource;

    public static void init() {
        if (dataSource != null) return;

        logger.info("Initializing Database Connection Pool...");
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DB_URL);
        config.setDriverClassName("org.sqlite.JDBC");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(300000);
        config.setConnectionTimeout(10000);
        
        dataSource = new HikariDataSource(config);
        logger.info("HikariCP Connection Pool initialized.");
        
        // Add shutdown hook for extra safety
        Runtime.getRuntime().addShutdownHook(new Thread(DatabaseManager::shutdown));
        
        initializeSchema();
        migratePasswords();
    }

    private static void initializeSchema() {
        try (InputStream is = DatabaseManager.class.getResourceAsStream("/schema.sql")) {
            if (is == null) {
                logger.error("schema.sql not found!");
                return;
            }
            
            String sql = new BufferedReader(new InputStreamReader(is))
                .lines().collect(Collectors.joining("\n"));
            
            String[] statements = sql.split(";");
            
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                for (String s : statements) {
                    if (!s.trim().isEmpty()) {
                        stmt.execute(s);
                    }
                }
            }
            logger.info("Database schema initialized.");

        } catch (Exception e) {
            logger.error("Error reading/executing schema: {}", e.getMessage(), e);
        }
    }

    private static void migratePasswords() {
        logger.info("Checking for password migration...");
        String selectSql = "SELECT id, username, password FROM users";
        String updateSql = "UPDATE users SET password = ? WHERE id = ?";
        
        List<Object[]> usersToUpdate = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSql)) {
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String password = rs.getString("password");
                
                // If not starting with $2a$, it's likely plaintext
                if (!password.startsWith("$2a$")) {
                    logger.info("Migrating password for user: {}", username);
                    String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                    usersToUpdate.add(new Object[]{hashedPassword, id});
                }
            }
            
            if (!usersToUpdate.isEmpty()) {
                try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                    for (Object[] update : usersToUpdate) {
                        pstmt.setString(1, (String) update[0]);
                        pstmt.setInt(2, (Integer) update[1]);
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                    logger.info("Successfully migrated {} passwords.", usersToUpdate.size());
                }
            } else {
                logger.info("No passwords need migration.");
            }
            
        } catch (SQLException e) {
            logger.error("Error during password migration: {}", e.getMessage(), e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            init();
        }
        return dataSource.getConnection();
    }

    public static void shutdown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
