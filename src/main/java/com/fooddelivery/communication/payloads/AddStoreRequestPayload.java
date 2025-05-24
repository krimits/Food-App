package com.fooddelivery.communication.payloads; // Store payloads in a sub-package

import com.fooddelivery.model.Product; // Assuming Product is Serializable or we handle its conversion
import java.io.Serializable;
import java.util.List;

// This class represents the structure of the JSON payload for adding a store.
// The actual Store object from com.fooddelivery.model.Store will be serialized into JSON
// and that JSON string will be the payload for the Message class when type is ADD_STORE_REQUEST.
// Alternatively, if the manager console sends the full JSON string directly, that can be used.
// For now, let's define this to mirror the expected JSON structure.
// If we use a JSON library, these classes can be used directly for serialization/deserialization.
// Without a library, we'll be dealing with JSON strings manually.

public class AddStoreRequestPayload implements Serializable {
    private static final long serialVersionUID = 1L;

    private String StoreName;
    private double Latitude;
    private double Longitude;
    private String FoodCategory;
    private int Stars;
    private int NoOfVotes;
    private String StoreLogo; // Path
    private List<ProductDetailsPayload> Products; // Using a nested payload for products

    // Getters and Setters for all fields (required for JSON libraries, good practice anyway)

    public String getStoreName() { return StoreName; }
    public void setStoreName(String storeName) { StoreName = storeName; }
    public double getLatitude() { return Latitude; }
    public void setLatitude(double latitude) { Latitude = latitude; }
    public double getLongitude() { return Longitude; }
    public void setLongitude(double longitude) { Longitude = longitude; }
    public String getFoodCategory() { return FoodCategory; }
    public void setFoodCategory(String foodCategory) { FoodCategory = foodCategory; }
    public int getStars() { return Stars; }
    public void setStars(int stars) { Stars = stars; }
    public int getNoOfVotes() { return NoOfVotes; }
    public void setNoOfVotes(int noOfVotes) { NoOfVotes = noOfVotes; }
    public String getStoreLogo() { return StoreLogo; }
    public void setStoreLogo(String storeLogo) { StoreLogo = storeLogo; }
    public List<ProductDetailsPayload> getProducts() { return Products; }
    public void setProducts(List<ProductDetailsPayload> products) { Products = products; }

    public static class ProductDetailsPayload implements Serializable {
        private static final long serialVersionUID = 1L;
        private String ProductName;
        private String ProductType;
        private int AvailableAmount; // Field name from JSON
        private double Price;

        // Getters and Setters
        public String getProductName() { return ProductName; }
        public void setProductName(String productName) { ProductName = productName; }
        public String getProductType() { return ProductType; }
        public void setProductType(String productType) { ProductType = productType; }
        public int getAvailableAmount() { return AvailableAmount; }
        public void setAvailableAmount(int availableAmount) { AvailableAmount = availableAmount; }
        public double getPrice() { return Price; }
        public void setPrice(double price) { Price = price; }
    }
}
