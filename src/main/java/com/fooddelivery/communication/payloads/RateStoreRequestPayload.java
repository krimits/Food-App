package com.fooddelivery.communication.payloads;

import java.io.Serializable;

public class RateStoreRequestPayload implements Serializable {
    private static final long serialVersionUID = 1L;
    // storeName will be sent as part of the routing key (e.g., "RATE_STORE_REQUEST:MyStore")
    // Can also be included here.
    private String storeName;
    private int stars; // 1-5

    public RateStoreRequestPayload(String storeName, int stars) {
        this.storeName = storeName;
        this.stars = stars;
    }

    // Getters
    public String getStoreName() { return storeName; }
    public int getStars() { return stars; }
    // Setters
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public void setStars(int stars) { this.stars = stars; }
}
