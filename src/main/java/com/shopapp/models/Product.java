package com.shopapp.models;

public class Product {
    private int id;
    private String name;
    private String barcode;
    private String hsnCode;
    private double pricePerUnit;
    private int currentStock;
    private int minStockAlert;
    private String description;

    public Product() {}

    public Product(int id, String name, String barcode, String hsnCode, double pricePerUnit, int currentStock, int minStockAlert, String description) {
        this.id = id;
        this.name = name;
        this.barcode = barcode;
        this.hsnCode = hsnCode;
        this.pricePerUnit = pricePerUnit;
        this.currentStock = currentStock;
        this.minStockAlert = minStockAlert;
        this.description = description;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getHsnCode() { return hsnCode; }
    public void setHsnCode(String hsnCode) { this.hsnCode = hsnCode; }

    public double getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(double pricePerUnit) { this.pricePerUnit = pricePerUnit; }

    public int getCurrentStock() { return currentStock; }
    public void setCurrentStock(int currentStock) { this.currentStock = currentStock; }

    public int getMinStockAlert() { return minStockAlert; }
    public void setMinStockAlert(int minStockAlert) { this.minStockAlert = minStockAlert; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
