package com.fooddelivery.communication.payloads;

import java.io.Serializable;

public class RemoveProductRequestPayload implements Serializable {
    private static final long serialVersionUID = 1L;

    private String storeName;
    private String productName;

    // Constructor
    public RemoveProductRequestPayload(String storeName, String productName) {
        this.storeName = storeName;
        this.productName = productName;
    }

    // Getters
    public String getStoreName() { return storeName; }
    public String getProductName() { return productName; }

    // Setters
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public void setProductName(String productName) { this.productName = productName; }
}
