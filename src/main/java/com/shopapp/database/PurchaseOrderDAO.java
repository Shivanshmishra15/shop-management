package com.shopapp.database;

import com.shopapp.models.PurchaseOrder;
import com.shopapp.models.PurchaseOrderItem;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderDAO {

    public int createPurchaseOrder(PurchaseOrder po) throws SQLException {
        String sql = "INSERT INTO purchase_orders (supplier_id, total_amount, status) VALUES (?, ?, ?)";
        String sqlItem = "INSERT INTO purchase_order_items (po_id, product_id, quantity, cost_price) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int poId = -1;
                try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setInt(1, po.getSupplierId());
                    pstmt.setDouble(2, po.getTotalAmount());
                    pstmt.setString(3, po.getStatus());
                    pstmt.executeUpdate();
                    
                    ResultSet rs = pstmt.getGeneratedKeys();
                    if (rs.next()) {
                        poId = rs.getInt(1);
                    } else {
                        throw new SQLException("Failed to create purchase order, no ID obtained.");
                    }
                }

                try (PreparedStatement itemPstmt = conn.prepareStatement(sqlItem)) {
                    for (PurchaseOrderItem item : po.getItems()) {
                        itemPstmt.setInt(1, poId);
                        itemPstmt.setInt(2, item.getProductId());
                        itemPstmt.setInt(3, item.getQuantity());
                        itemPstmt.setDouble(4, item.getCostPrice());
                        itemPstmt.addBatch();
                    }
                    itemPstmt.executeBatch();
                }

                conn.commit();
                return poId;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // Removed standalone createPOItem as it's now integrated into the transaction

    // Basic List Method (Could be enhanced with JOINs)
    public List<PurchaseOrder> getAllOrders() throws SQLException {
        List<PurchaseOrder> list = new ArrayList<>();
        // Joins would be better, but keeping it simple for now
        String sql = "SELECT po.*, s.name as supplier_name FROM purchase_orders po " +
                     "JOIN suppliers s ON po.supplier_id = s.id " +
                     "ORDER BY po.order_date DESC";
                     
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             
            while (rs.next()) {
                // Parse date (SQLite default format usually yyyy-MM-dd HH:mm:ss)
                // Using simple parse or fallback
                LocalDateTime date;
                try {
                     date = LocalDateTime.parse(rs.getString("order_date").replace(" ", "T"));
                } catch (Exception e) { date = LocalDateTime.now(); }

                PurchaseOrder po = new PurchaseOrder(
                    rs.getInt("id"),
                    rs.getInt("supplier_id"),
                    date,
                    rs.getString("status"),
                    rs.getDouble("total_amount")
                );
                po.setSupplierName(rs.getString("supplier_name"));
                list.add(po);
            }
        }
        return list;
    }
}
