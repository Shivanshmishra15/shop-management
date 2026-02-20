package com.shopapp.models;

public class PurchaseOrderItem {
    private int id;
    private int poId;
    private int productId;
    private String productName; // For display
    private int quantity;
    private double costPrice;

    public PurchaseOrderItem(int id, int poId, int productId, int quantity, double costPrice) {
        this.id = id;
        this.poId = poId;
        this.productId = productId;
        this.quantity = quantity;
        this.costPrice = costPrice;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPoId() { return poId; }
    public void setPoId(int poId) { this.poId = poId; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getCostPrice() { return costPrice; }
    public void setCostPrice(double costPrice) { this.costPrice = costPrice; }
    
    public double getTotal() { return quantity * costPrice; }
}
