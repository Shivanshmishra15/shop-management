package com.shopapp.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;

public class FinanceDAO {
    private static final Logger logger = LoggerFactory.getLogger(FinanceDAO.class);
    
    public double getTotalSales() throws SQLException {
        return getSum("SELECT SUM(total_amount) FROM invoices");
    }

    public double getTotalPurchaseCosts() throws SQLException {
        return getSum("SELECT SUM(total_amount) FROM purchase_orders");
    }

    public double getTotalExpenses() throws SQLException {
        return getSum("SELECT SUM(amount) FROM expenses");
    }

    private double getSum(String sql) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                double sum = rs.getDouble(1);
                logger.debug("Sum result for query '{}': {}", sql, sum);
                return sum;
            }
        } catch (SQLException e) {
            logger.error("Error executing sum query '{}': {}", sql, e.getMessage(), e);
            throw e;
        }
        return 0.0;
    }
}
