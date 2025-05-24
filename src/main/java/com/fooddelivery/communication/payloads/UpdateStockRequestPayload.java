package com.fooddelivery.communication.payloads;

import java.io.Serializable;

public class UpdateStockRequestPayload implements Serializable {
    private static final long serialVersionUID = 1L;

    private String storeName;
    private String productName;
    private int quantityChange; // Positive to add stock, negative to decrease

    // Constructor
    public UpdateStockRequestPayload(String storeName, String productName, int quantityChange) {
        this.storeName = storeName;
        this.productName = productName;
        this.quantityChange = quantityChange;
    }

    // Getters
    public String getStoreName() { return storeName; }
    public String getProductName() { return productName; }
    public int getQuantityChange() { return quantityChange; }

    // Setters
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setQuantityChange(int quantityChange) { this.quantityChange = quantityChange; }
}
