package com.shopapp.database;

import com.shopapp.models.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public void addProduct(Product product) throws SQLException {
        String sql = "INSERT INTO products (name, barcode, hsn_code, price_per_unit, current_stock, min_stock_alert, description) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getBarcode());
            pstmt.setString(3, product.getHsnCode());
            pstmt.setDouble(4, product.getPricePerUnit());
            pstmt.setInt(5, product.getCurrentStock());
            pstmt.setInt(6, product.getMinStockAlert());
            pstmt.setString(7, product.getDescription());
            
            pstmt.executeUpdate();
        }
    }

    public void updateProduct(Product product) throws SQLException {
        String sql = "UPDATE products SET name=?, barcode=?, hsn_code=?, price_per_unit=?, current_stock=?, min_stock_alert=?, description=? WHERE id=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getBarcode());
            pstmt.setString(3, product.getHsnCode());
            pstmt.setDouble(4, product.getPricePerUnit());
            pstmt.setInt(5, product.getCurrentStock());
            pstmt.setInt(6, product.getMinStockAlert());
            pstmt.setString(7, product.getDescription());
            pstmt.setInt(8, product.getId());
            
            pstmt.executeUpdate();
        }
    }

    public void deleteProduct(int id) throws SQLException {
        String sql = "DELETE FROM products WHERE id=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public List<Product> getAllProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY name";
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Product p = new Product();
                p.setId(rs.getInt("id"));
                p.setName(rs.getString("name"));
                p.setBarcode(rs.getString("barcode"));
                p.setHsnCode(rs.getString("hsn_code"));
                p.setPricePerUnit(rs.getDouble("price_per_unit"));
                p.setCurrentStock(rs.getInt("current_stock"));
                p.setMinStockAlert(rs.getInt("min_stock_alert"));
                p.setDescription(rs.getString("description"));
                products.add(p);
            }
        }
        return products;
    }
    
    public Product getProductByBarcode(String barcode) throws SQLException {
        String sql = "SELECT * FROM products WHERE barcode = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, barcode);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Product p = new Product();
                    p.setId(rs.getInt("id"));
                    p.setName(rs.getString("name"));
                    p.setBarcode(rs.getString("barcode"));
                    p.setHsnCode(rs.getString("hsn_code"));
                    p.setPricePerUnit(rs.getDouble("price_per_unit"));
                    p.setCurrentStock(rs.getInt("current_stock"));
                    p.setMinStockAlert(rs.getInt("min_stock_alert"));
                    p.setDescription(rs.getString("description"));
                    return p;
                }
            }
        }
        return null;
    }
}
