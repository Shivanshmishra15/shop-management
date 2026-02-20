package com.shopapp.database;

import com.shopapp.models.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO {
    private static final Logger logger = LoggerFactory.getLogger(SupplierDAO.class);

    public void addSupplier(Supplier s) throws SQLException {
        String sql = "INSERT INTO suppliers (name, contact_person, phone, email, address) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, s.getName());
            pstmt.setString(2, s.getContactPerson());
            pstmt.setString(3, s.getPhone());
            pstmt.setString(4, s.getEmail());
            pstmt.setString(5, s.getAddress());
            pstmt.executeUpdate();
            logger.info("New supplier added: {}", s.getName());
        } catch (SQLException e) {
            logger.error("Error adding supplier {}: {}", s.getName(), e.getMessage(), e);
            throw e;
        }
    }

    public List<Supplier> getAllSuppliers() throws SQLException {
        List<Supplier> list = new ArrayList<>();
        String sql = "SELECT * FROM suppliers";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Supplier(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("contact_person"),
                    rs.getString("phone"),
                    rs.getString("email"),
                    rs.getString("address")
                ));
            }
        }
        return list;
    }

    public void updateSupplier(Supplier s) throws SQLException {
        String sql = "UPDATE suppliers SET name=?, contact_person=?, phone=?, email=?, address=? WHERE id=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, s.getName());
            pstmt.setString(2, s.getContactPerson());
            pstmt.setString(3, s.getPhone());
            pstmt.setString(4, s.getEmail());
            pstmt.setString(5, s.getAddress());
            pstmt.setInt(6, s.getId());
            pstmt.executeUpdate();
            logger.info("Supplier updated: {}", s.getName());
        } catch (SQLException e) {
            logger.error("Error updating supplier {}: {}", s.getName(), e.getMessage(), e);
            throw e;
        }
    }

    public void deleteSupplier(int id) throws SQLException {
        String sql = "DELETE FROM suppliers WHERE id=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            logger.info("Supplier ID {} deleted.", id);
        } catch (SQLException e) {
            logger.error("Error deleting supplier ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }
}
