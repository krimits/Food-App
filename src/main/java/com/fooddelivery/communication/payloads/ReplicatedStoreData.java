package com.fooddelivery.communication.payloads;

import java.io.Serializable;
import java.util.Map;
import java.util.List; // If replicating multiple sales entries at once, though unlikely for single purchase

// This payload is sent by the Master to Workers to replicate specific state changes
// that occurred on a primary worker (e.g., after a purchase or rating).
public class ReplicatedStoreData implements Serializable {
    private static final long serialVersionUID = 1L;

    private String storeName; // Mandatory to identify the store

    // For stock updates (productName -> new stock level)
    private Map<String, Integer> updatedProductStocks; 

    // For new sales (a single sale entry from a purchase)
    // Includes productName, quantitySold, pricePerItemAtTimeOfSale (important!)
    private SalesDataEntry newSaleMade; // Using existing SalesDataEntry

    // For rating updates
    private Integer newAverageStars; // Integer to match Store.stars
    private Integer newTotalVotes;

    // Constructor - build based on what needs to be replicated
    public ReplicatedStoreData(String storeName) {
        this.storeName = storeName;
    }

    // Getters
    public String getStoreName() { return storeName; }
    public Map<String, Integer> getUpdatedProductStocks() { return updatedProductStocks; }
    public SalesDataEntry getNewSaleMade() { return newSaleMade; }
    public Integer getNewAverageStars() { return newAverageStars; }
    public Integer getNewTotalVotes() { return newTotalVotes; }

    // Setters for building the object in Master
    public void setUpdatedProductStocks(Map<String, Integer> updatedProductStocks) {
        this.updatedProductStocks = updatedProductStocks;
    }
    public void setNewSaleMade(SalesDataEntry newSaleMade) {
        this.newSaleMade = newSaleMade;
    }
    public void setNewRating(int newAverageStars, int newTotalVotes) {
        this.newAverageStars = newAverageStars;
        this.newTotalVotes = newTotalVotes;
    }
}
