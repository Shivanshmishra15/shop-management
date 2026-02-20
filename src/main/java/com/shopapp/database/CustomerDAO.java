package com.shopapp.database;

import com.shopapp.models.Customer;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerDAO {

    public void addCustomer(Customer c) throws SQLException {
        String sql = "INSERT INTO customers (name, phone, email, address, loyalty_points) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, c.getName());
            pstmt.setString(2, c.getPhone());
            pstmt.setString(3, c.getEmail());
            pstmt.setString(4, c.getAddress());
            pstmt.setInt(5, c.getLoyaltyPoints());
            pstmt.executeUpdate();
        }
    }

    public void updateCustomer(Customer c) throws SQLException {
        String sql = "UPDATE customers SET name=?, phone=?, email=?, address=?, loyalty_points=? WHERE id=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, c.getName());
            pstmt.setString(2, c.getPhone());
            pstmt.setString(3, c.getEmail());
            pstmt.setString(4, c.getAddress());
            pstmt.setInt(5, c.getLoyaltyPoints());
            pstmt.setInt(6, c.getId());
            pstmt.executeUpdate();
        }
    }

    public List<Customer> searchCustomers(String query) throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE name LIKE ? OR phone LIKE ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String q = "%" + query + "%";
            pstmt.setString(1, q);
            pstmt.setString(2, q);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        }
        return list;
    }
    
    public Optional<Customer> getCustomerByPhone(String phone) throws SQLException {
        String sql = "SELECT * FROM customers WHERE phone = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phone);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSet(rs));
            }
        }
        return Optional.empty();
    }

    public List<Customer> getAllCustomers() throws SQLException {
         List<Customer> list = new ArrayList<>();
         try (Connection conn = DatabaseManager.getConnection();
              Statement stmt = conn.createStatement();
              ResultSet rs = stmt.executeQuery("SELECT * FROM customers")) {
             while (rs.next()) {
                 list.add(mapResultSet(rs));
             }
         }
         return list;
    }

    private Customer mapResultSet(ResultSet rs) throws SQLException {
        return new Customer(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("phone"),
            rs.getString("email"),
            rs.getString("address"),
            rs.getInt("loyalty_points"),
            LocalDateTime.now() // formatting skip for brevity
        );
    }
}
