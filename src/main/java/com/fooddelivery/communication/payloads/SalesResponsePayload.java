package com.fooddelivery.communication.payloads;

import java.io.Serializable;
import java.util.Map;
import java.util.List; // Using List of SalesDataEntry for ordered results / flexibility

public class SalesResponsePayload implements Serializable {
    private static final long serialVersionUID = 1L;

    private String queryType; // e.g., "BY_PRODUCT_IN_STORE", "BY_STORE_TYPE", "BY_PRODUCT_CATEGORY"
    private String queryContext; // e.g., storeName, foodCategory, productType
    private List<SalesDataEntry> entries; // List of sales data entries
    private double grandTotalRevenue; // Grand total for the queried context

    // Constructor
    public SalesResponsePayload(String queryType, String queryContext, List<SalesDataEntry> entries, double grandTotalRevenue) {
        this.queryType = queryType;
        this.queryContext = queryContext;
        this.entries = entries;
        this.grandTotalRevenue = grandTotalRevenue;
    }

    // Getters
    public String getQueryType() { return queryType; }
    public String getQueryContext() { return queryContext; }
    public List<SalesDataEntry> getEntries() { return entries; }
    public double getGrandTotalRevenue() { return grandTotalRevenue; }

    // Setters
    public void setQueryType(String queryType) { this.queryType = queryType; }
    public void setQueryContext(String queryContext) { this.queryContext = queryContext; }
    public void setEntries(List<SalesDataEntry> entries) { this.entries = entries; }
    public void setGrandTotalRevenue(double grandTotalRevenue) { this.grandTotalRevenue = grandTotalRevenue; }
}
