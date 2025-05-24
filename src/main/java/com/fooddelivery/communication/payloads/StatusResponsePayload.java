package com.fooddelivery.communication.payloads;

import java.io.Serializable;

public class StatusResponsePayload implements Serializable {
    private static final long serialVersionUID = 1L;

    private String storeName; // Optional, context-dependent
    private String status; // e.g., "SUCCESS", "FAILURE", "PENDING"
    private String message; // Detailed message

    public StatusResponsePayload(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public StatusResponsePayload(String storeName, String status, String message) {
        this.storeName = storeName;
        this.status = status;
        this.message = message;
    }
    
    // Getters and Setters
    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
