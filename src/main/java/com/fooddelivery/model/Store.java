package com.fooddelivery.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Store {
    private String storeName;
    private double latitude;
    private double longitude;
    private String foodCategory; // e.g., "pizzeria"
    private int stars; // 1-5
    private int noOfVotes;
    private String storeLogoPath;
    private List<Product> products;
    private String priceCategory; // $, $$, $$$ (calculated)
    
    // For sales tracking
    private Map<String, Integer> salesByProduct; // ProductName -> quantity sold
    private double totalRevenue;

    // Constructors
    public Store(String storeName, double latitude, double longitude, String foodCategory, 
                 int stars, int noOfVotes, String storeLogoPath, List<Product> products) {
        this.storeName = storeName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.foodCategory = foodCategory;
        this.stars = stars;
        this.noOfVotes = noOfVotes;
        this.storeLogoPath = storeLogoPath;
        this.products = products != null ? new ArrayList<>(products) : new ArrayList<>();
        this.salesByProduct = new HashMap<>();
        this.totalRevenue = 0.0;
        // Price category will be calculated and set separately
    }

    // Getters
    public String getStoreName() { return storeName; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getFoodCategory() { return foodCategory; }
    public int getStars() { return stars; }
    public int getNoOfVotes() { return noOfVotes; }
    public String getStoreLogoPath() { return storeLogoPath; }
    public List<Product> getProducts() { return new ArrayList<>(products); } // Return a copy
    public String getPriceCategory() { return priceCategory; }
    public Map<String, Integer> getSalesByProduct() { return new HashMap<>(salesByProduct); } // Return a copy
    public double getTotalRevenue() { return totalRevenue; }

    // Setters
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setFoodCategory(String foodCategory) { this.foodCategory = foodCategory; }
    public void setStars(int stars) { this.stars = stars; }
    public void setNoOfVotes(int noOfVotes) { this.noOfVotes = noOfVotes; }
    public void setStoreLogoPath(String storeLogoPath) { this.storeLogoPath = storeLogoPath; }
    public void setPriceCategory(String priceCategory) { this.priceCategory = priceCategory; }
    
    // Methods to manage products
    public void addProduct(Product product) {
        if (this.products == null) {
            this.products = new ArrayList<>();
        }
        this.products.add(product);
        // Potentially recalculate price category
    }

    public boolean removeProduct(String productName) {
        if (this.products == null) return false;
        return this.products.removeIf(p -> p.getProductName().equals(productName));
        // Potentially recalculate price category
    }
    
    public Product findProduct(String productName) {
        if (this.products == null) return null;
        for (Product p : this.products) {
            if (p.getProductName().equals(productName)) {
                return p;
            }
        }
        return null;
    }

    // Methods for sales and revenue (ensure thread safety if called concurrently later)
    public synchronized void recordSale(String productName, int quantity, double pricePerItem) {
        this.salesByProduct.put(productName, this.salesByProduct.getOrDefault(productName, 0) + quantity);
        this.totalRevenue += quantity * pricePerItem;
    }
    
    public synchronized void updateStock(String productName, int quantityChange) {
        Product product = findProduct(productName);
        if (product != null) {
            product.setAvailableAmount(product.getAvailableAmount() + quantityChange);
        }
    }
    
    // Method to calculate and set price category
    public void calculateAndSetPriceCategory() {
        if (products == null || products.isEmpty()) {
            this.priceCategory = "-"; // Or some default/unknown
            return;
        }
        double sumPrices = 0;
        for (Product product : products) {
            sumPrices += product.getPrice();
        }
        double avgPrice = sumPrices / products.size();
        if (avgPrice <= 5) {
            this.priceCategory = "$";
        } else if (avgPrice <= 15) {
            this.priceCategory = "$$";
        } else {
            this.priceCategory = "$$$";
        }
    }

    @Override
    public String toString() {
        return "Store{" +
               "storeName='" + storeName + '\'' +
               ", latitude=" + latitude +
               ", longitude=" + longitude +
               ", foodCategory='" + foodCategory + '\'' +
               ", stars=" + stars +
               ", noOfVotes=" + noOfVotes +
               ", priceCategory='" + priceCategory + '\'' +
               ", products=" + products.size() + " products" +
               ", totalRevenue=" + totalRevenue +
               '}';
    }
}
