package com.fooddelivery.communication.payloads;

import java.io.Serializable;

public class SalesRequestPayload implements Serializable {
    private static final long serialVersionUID = 1L;

    private String storeName;       // For sales by product in a specific store
    private String foodCategory;    // For sales by store type (foodCategory)
    private String productType;     // For sales by product category (productType)
    // Add other relevant fields if needed, e.g., date ranges in a more advanced system

    // Constructors for different request types
    public SalesRequestPayload() {} // Default constructor

    public static SalesRequestPayload forStore(String storeName) {
        SalesRequestPayload p = new SalesRequestPayload();
        p.setStoreName(storeName);
        return p;
    }

    public static SalesRequestPayload forFoodCategory(String foodCategory) {
        SalesRequestPayload p = new SalesRequestPayload();
        p.setFoodCategory(foodCategory);
        return p;
    }

    public static SalesRequestPayload forProductType(String productType) {
        SalesRequestPayload p = new SalesRequestPayload();
        p.setProductType(productType);
        return p;
    }

    // Getters
    public String getStoreName() { return storeName; }
    public String getFoodCategory() { return foodCategory; }
    public String getProductType() { return productType; }

    // Setters
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public void setFoodCategory(String foodCategory) { this.foodCategory = foodCategory; }
    public void setProductType(String productType) { this.productType = productType; }
}
