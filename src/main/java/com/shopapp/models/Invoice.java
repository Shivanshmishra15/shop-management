package com.shopapp.models;

import java.time.LocalDateTime;

public class Invoice {
    private int id;
    private String customerName;
    private String customerContact;
    private String gstNumber;
    private double totalAmount;
    private double gstTotal;
    private double finalAmount;
    private LocalDateTime createdAt;

    public Invoice(int id, String customerName, String customerContact, String gstNumber, double totalAmount, double gstTotal, double finalAmount, LocalDateTime createdAt) {
        this.id = id;
        this.customerName = customerName;
        this.customerContact = customerContact;
        this.gstNumber = gstNumber;
        this.totalAmount = totalAmount; // Pre-tax
        this.gstTotal = gstTotal;
        this.finalAmount = finalAmount; // Post-tax
        this.createdAt = createdAt;
    }

    // Getters
    public int getId() { return id; }
    public String getCustomerName() { return customerName; }
    public String getCustomerContact() { return customerContact; } // Can be null
    public double getFinalAmount() { return finalAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
