package com.fooddelivery.communication.payloads;

import java.io.Serializable;

// Sent by Primary Worker to Master after successfully processing a rating.
public class RatingProcessingDetailsResponsePayload implements Serializable {
    private static final long serialVersionUID = 1L;
    private String storeName;
    private String status; // SUCCESS or FAILURE
    private String message;
    private int newAverageStars;
    private int newTotalVotes;

    public RatingProcessingDetailsResponsePayload(String storeName, String status, String message, int newAverageStars, int newTotalVotes) {
        this.storeName = storeName;
        this.status = status;
        this.message = message;
        this.newAverageStars = newAverageStars;
        this.newTotalVotes = newTotalVotes;
    }

    // Getters
    public String getStoreName() { return storeName; }
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public int getNewAverageStars() { return newAverageStars; }
    public int getNewTotalVotes() { return newTotalVotes; }
}
