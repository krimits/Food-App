package com.fooddelivery.communication.payloads;

import java.io.Serializable;

public class AddProductRequestPayload implements Serializable {
    private static final long serialVersionUID = 1L;

    private String storeName;
    private String productName;
    private String productType;
    private double price;
    private int initialAvailableAmount;

    // Constructor
    public AddProductRequestPayload(String storeName, String productName, String productType, double price, int initialAvailableAmount) {
        this.storeName = storeName;
        this.productName = productName;
        this.productType = productType;
        this.price = price;
        this.initialAvailableAmount = initialAvailableAmount;
    }

    // Getters
    public String getStoreName() { return storeName; }
    public String getProductName() { return productName; }
    public String getProductType() { return productType; }
    public double getPrice() { return price; }
    public int getInitialAvailableAmount() { return initialAvailableAmount; }

    // Setters (optional, but good for consistency if other payloads have them)
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setProductType(String productType) { this.productType = productType; }
    public void setPrice(double price) { this.price = price; }
    public void setInitialAvailableAmount(int initialAvailableAmount) { this.initialAvailableAmount = initialAvailableAmount; }
}
