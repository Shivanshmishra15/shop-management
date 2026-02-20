package com.shopapp.database;

import com.shopapp.models.Expense;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExpenseDAO {
    private static final Logger logger = LoggerFactory.getLogger(ExpenseDAO.class);

    public void addExpense(Expense exc) throws SQLException {
        String sql = "INSERT INTO expenses (category, description, amount) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, exc.getCategory());
            pstmt.setString(2, exc.getDescription());
            pstmt.setDouble(3, exc.getAmount());
            pstmt.executeUpdate();
            logger.info("New expense added: {} ({})", exc.getAmount(), exc.getCategory());
        } catch (SQLException e) {
            logger.error("Error adding expense: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<Expense> getAllExpenses() throws SQLException {
        List<Expense> list = new ArrayList<>();
        String sql = "SELECT * FROM expenses ORDER BY expense_date DESC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                LocalDateTime date;
                try { date = LocalDateTime.parse(rs.getString("expense_date").replace(" ", "T")); }
                catch (Exception e) { date = LocalDateTime.now(); }

                list.add(new Expense(
                    rs.getInt("id"),
                    rs.getString("category"),
                    rs.getString("description"),
                    rs.getDouble("amount"),
                    date
                ));
            }
        }
        return list;
    }

    public void deleteExpense(int id) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM expenses WHERE id=?")) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            logger.info("Expense ID {} deleted.", id);
        } catch (SQLException e) {
            logger.error("Error deleting expense ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }
}
