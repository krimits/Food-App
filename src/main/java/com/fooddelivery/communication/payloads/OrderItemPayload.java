package com.fooddelivery.communication.payloads;

import java.io.Serializable;

public class OrderItemPayload implements Serializable {
    private static final long serialVersionUID = 1L;
    private String productName;
    private int quantity;

    public OrderItemPayload(String productName, int quantity) {
        this.productName = productName;
        this.quantity = quantity;
    }

    // Getters
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    // Setters
    public void setProductName(String productName) { this.productName = productName; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
