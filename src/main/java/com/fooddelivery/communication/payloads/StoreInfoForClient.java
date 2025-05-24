package com.fooddelivery.communication.payloads;

import java.io.Serializable;

public class StoreInfoForClient implements Serializable {
    private static final long serialVersionUID = 1L;
    private String storeName;
    private String foodCategory;
    private int stars;
    private String priceCategory; // $, $$, $$$
    private double distanceKm;
    private String storeLogoPath;
    private double latitude;
    private double longitude;
    // Could also include a short list of popular products or tags later

    public StoreInfoForClient(String storeName, String foodCategory, int stars, String priceCategory, double distanceKm, String storeLogoPath, double latitude, double longitude) {
        this.storeName = storeName;
        this.foodCategory = foodCategory;
        this.stars = stars;
        this.priceCategory = priceCategory;
        this.distanceKm = distanceKm;
        this.storeLogoPath = storeLogoPath;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters for all fields
    public String getStoreName() { return storeName; }
    public String getFoodCategory() { return foodCategory; }
    public int getStars() { return stars; }
    public String getPriceCategory() { return priceCategory; }
    public double getDistanceKm() { return distanceKm; }
    public String getStoreLogoPath() { return storeLogoPath; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    
    // toString for easy representation if manually building JSON for client display (debug)
    @Override
    public String toString() {
        // Basic JSON-like string for debugging. Not for actual protocol use.
        return String.format("{\"storeName\":\"%s\", \"foodCategory\":\"%s\", \"stars\":%d, \"priceCategory\":\"%s\", \"distanceKm\":%.2f, \"latitude\":%f, \"longitude\":%f}",
            storeName, foodCategory, stars, priceCategory, distanceKm, latitude, longitude);
    }
}
