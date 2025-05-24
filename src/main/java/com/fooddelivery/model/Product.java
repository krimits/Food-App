package com.fooddelivery.model;

public class Product {
    private String productName;
    private String productType;
    private int availableAmount;
    private double price;
    private boolean isAvailableForCustomer; // New field for soft delete

    // Constructor
    public Product(String productName, String productType, int availableAmount, double price) {
        this.productName = productName;
        this.productType = productType;
        this.availableAmount = availableAmount;
        this.price = price;
        this.isAvailableForCustomer = true; // Default to true
    }

    // Getters
    public String getProductName() { return productName; }
    public String getProductType() { return productType; }
    public int getAvailableAmount() { return availableAmount; }
    public double getPrice() { return price; }
    public boolean isAvailableForCustomer() { return isAvailableForCustomer; }

    // Setters
    public void setProductName(String productName) { this.productName = productName; }
    public void setProductType(String productType) { this.productType = productType; }
    public void setAvailableAmount(int availableAmount) { this.availableAmount = availableAmount; }
    public void setPrice(double price) { this.price = price; }
    public void setAvailableForCustomer(boolean availableForCustomer) { this.isAvailableForCustomer = availableForCustomer; }

    @Override
    public String toString() {
        return "Product{" +
               "productName='" + productName + '\'' +
               ", productType='" + productType + '\'' +
               ", availableAmount=" + availableAmount +
               ", price=" + price +
               ", isAvailableForCustomer=" + isAvailableForCustomer +
               '}';
    }
}
