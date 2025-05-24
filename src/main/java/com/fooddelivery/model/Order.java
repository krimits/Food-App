package com.fooddelivery.model;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

public class Order {
    private String orderId;
    private String storeName; // Or storeId
    private String userId; // Identifier for the customer
    private Map<String, Integer> purchasedProducts; // ProductName -> quantity
    private double totalAmount;
    private long timestamp;

    public Order(String storeName, String userId, Map<String, Integer> purchasedProducts, double totalAmount) {
        this.orderId = UUID.randomUUID().toString(); // Generate a unique order ID
        this.storeName = storeName;
        this.userId = userId;
        this.purchasedProducts = new HashMap<>(purchasedProducts);
        this.totalAmount = totalAmount;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters
    public String getOrderId() { return orderId; }
    public String getStoreName() { return storeName; }
    public String getUserId() { return userId; }
    public Map<String, Integer> getPurchasedProducts() { return new HashMap<>(purchasedProducts); } // Return a copy
    public double getTotalAmount() { return totalAmount; }
    public long getTimestamp() { return timestamp; }

    // No setters typically for an order once created, but could be added if needed for modifications.

    @Override
    public String toString() {
        return "Order{" +
               "orderId='" + orderId + '\'' +
               ", storeName='" + storeName + '\'' +
               ", userId='" + userId + '\'' +
               ", purchasedProducts=" + purchasedProducts +
               ", totalAmount=" + totalAmount +
               ", timestamp=" + timestamp +
               '}';
    }
}
