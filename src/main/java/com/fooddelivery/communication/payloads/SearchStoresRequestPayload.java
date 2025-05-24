package com.fooddelivery.communication.payloads;

import java.io.Serializable;

public class SearchStoresRequestPayload implements Serializable {
    private static final long serialVersionUID = 1L;
    private double clientLatitude;
    private double clientLongitude;
    private String foodCategoryFilter; // Optional
    private int minStarsFilter;        // Optional (0 or -1 if not set)
    private String priceRangeFilter;   // Optional (e.g., "$", "$$", "$$$")

    public SearchStoresRequestPayload(double clientLatitude, double clientLongitude, String foodCategoryFilter, int minStarsFilter, String priceRangeFilter) {
        this.clientLatitude = clientLatitude;
        this.clientLongitude = clientLongitude;
        this.foodCategoryFilter = foodCategoryFilter;
        this.minStarsFilter = minStarsFilter;
        this.priceRangeFilter = priceRangeFilter;
    }
    
    // Getters
    public double getClientLatitude() { return clientLatitude; }
    public double getClientLongitude() { return clientLongitude; }
    public String getFoodCategoryFilter() { return foodCategoryFilter; }
    public int getMinStarsFilter() { return minStarsFilter; }
    public String getPriceRangeFilter() { return priceRangeFilter; }

    // Setters (optional, but good for builder patterns or if used by frameworks)
    public void setClientLatitude(double clientLatitude) { this.clientLatitude = clientLatitude; }
    public void setClientLongitude(double clientLongitude) { this.clientLongitude = clientLongitude; }
    public void setFoodCategoryFilter(String foodCategoryFilter) { this.foodCategoryFilter = foodCategoryFilter; }
    public void setMinStarsFilter(int minStarsFilter) { this.minStarsFilter = minStarsFilter; }
    public void setPriceRangeFilter(String priceRangeFilter) { this.priceRangeFilter = priceRangeFilter; }
}
