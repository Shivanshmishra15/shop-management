package com.shopapp.models;

public class CartItem {
    private Product product;
    private int quantity;
    private double rate;
    private double gstPercent;
    
    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.rate = product.getPricePerUnit();
        // Assuming inclusive tax or tax logic here. For simplicity, let's say GST is separate or included.
        // The prompt says "Generate invoices including... GST %".
        // Let's assume a default GST for now or add it to product. 
        // For this demo, we'll hardcode 18% or add a field later. 
        // Using 18% generic for now.
        this.gstPercent = 18.0; 
    }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public String getDescription() { return product.getName(); }
    public String getHsn() { return product.getHsnCode(); }
    public double getRate() { return rate; }
    public double getGst() { return gstPercent; }
    
    public double getTotal() {
        // Rate * Qty
        return rate * quantity;
    }
}
