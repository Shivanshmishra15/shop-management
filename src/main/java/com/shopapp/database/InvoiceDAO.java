package com.shopapp.database;

import com.shopapp.models.CartItem;
import com.shopapp.models.Invoice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.List;

public class InvoiceDAO {
    private static final Logger logger = LoggerFactory.getLogger(InvoiceDAO.class);

    public void createInvoice(String custName, String custPhone, String gst, List<CartItem> items, double totalAmount) throws SQLException {
        String sqlInv = "INSERT INTO invoices (customer_name, customer_contact, gst_number, total_amount, gst_total, final_amount) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlItem = "INSERT INTO invoice_items (invoice_id, product_id, product_name, quantity, rate, gst_percent, total) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlUpdateStock = "UPDATE products SET current_stock = current_stock - ? WHERE id = ? AND current_stock >= ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            logger.info("Starting invoice creation transaction for customer: {}", custName);
            
            try {
                // 1. Insert Invoice Header
                int invoiceId = 0;
                try (PreparedStatement pstmt = conn.prepareStatement(sqlInv, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, custName);
                    pstmt.setString(2, custPhone);
                    pstmt.setString(3, gst);
                    pstmt.setDouble(4, totalAmount);
                    pstmt.setDouble(5, 0); // TODO: Implement GST calculation
                    pstmt.setDouble(6, totalAmount);
                    pstmt.executeUpdate();
                    
                    ResultSet rs = pstmt.getGeneratedKeys();
                    if (rs.next()) {
                        invoiceId = rs.getInt(1);
                    } else {
                        throw new SQLException("Failed to retrieve generated invoice ID.");
                    }
                }

                // 2. Insert Invoice Items and Update Stock
                try (PreparedStatement itemPstmt = conn.prepareStatement(sqlItem);
                     PreparedStatement stockPstmt = conn.prepareStatement(sqlUpdateStock)) {
                     
                    for (CartItem item : items) {
                        // Add to Invoice Items
                        itemPstmt.setInt(1, invoiceId);
                        itemPstmt.setInt(2, item.getProduct().getId());
                        itemPstmt.setString(3, item.getProduct().getName());
                        itemPstmt.setInt(4, item.getQuantity());
                        itemPstmt.setDouble(5, item.getProduct().getPricePerUnit());
                        itemPstmt.setDouble(6, 0); 
                        itemPstmt.setDouble(7, item.getTotal());
                        itemPstmt.addBatch();

                        // Deduct Stock
                        stockPstmt.setInt(1, item.getQuantity());
                        stockPstmt.setInt(2, item.getProduct().getId());
                        stockPstmt.setInt(3, item.getQuantity()); // Ensure enough stock
                        int rows = stockPstmt.executeUpdate();
                        if (rows == 0) {
                            throw new SQLException("Insufficient stock for product: " + item.getProduct().getName());
                        }
                    }
                    itemPstmt.executeBatch();
                }

                conn.commit();
                logger.info("Invoice #{} created successfully with {} items.", invoiceId, items.size());
            } catch (SQLException e) {
                conn.rollback();
                logger.error("Transaction failed for customer '{}'. Rolled back. Error: {}", custName, e.getMessage());
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
