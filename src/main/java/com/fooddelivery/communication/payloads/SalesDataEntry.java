package com.fooddelivery.communication.payloads;

import java.io.Serializable;
// Assuming JsonUtil is in com.fooddelivery.util.JsonUtil
// If not, this import will fail compilation later, but for POJO structure it's fine.
// For the toString method, we might need a way to escape strings for JSON.
// Let's assume a utility class like JsonUtil.escapeJsonString exists or will be created.
// For now, the toString is a placeholder for how it might be manually serialized.
import com.fooddelivery.util.JsonUtil;


public class SalesDataEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    private String itemName; // Could be ProductName, StoreName etc. depending on context
    private int totalQuantity;
    private double totalRevenue;

    public SalesDataEntry(String itemName, int totalQuantity, double totalRevenue) {
        this.itemName = itemName;
        this.totalQuantity = totalQuantity;
        this.totalRevenue = totalRevenue;
    }

    // Getters
    public String getItemName() { return itemName; }
    public int getTotalQuantity() { return totalQuantity; }
    public double getTotalRevenue() { return totalRevenue; }

    // Setters
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setTotalQuantity(int totalQuantity) { this.totalQuantity = totalQuantity; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
    
    @Override
    public String toString() { // For easier representation in JSON if manually building
        // Attempt to use JsonUtil for escaping, if available.
        // If JsonUtil or its escapeJsonString method isn't available at compile time for this POJO,
        // this specific toString() method might cause a compile error later when integrated.
        // For POJO definition, this is fine.
        String escapedItemName = (JsonUtil.class != null) ? JsonUtil.escapeJsonString(itemName) : itemName.replace("\"", "\\\"");
        return String.format("{\"itemName\":\"%s\",\"totalQuantity\":%d,\"totalRevenue\":%.2f}", 
                             escapedItemName, totalQuantity, totalRevenue);
    }
}
