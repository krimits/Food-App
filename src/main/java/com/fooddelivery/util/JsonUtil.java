package com.fooddelivery.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonUtil {

    // Extracts StoreName from a JSON string. Very basic, assumes specific format.
    public static String extractStoreName(String jsonPayload) {
        if (jsonPayload == null) return null;
        // Regex to find "StoreName" : "value"
        // This is fragile and only works for simple, predictable JSON structures.
        Pattern pattern = Pattern.compile("\"StoreName\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(jsonPayload);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null; // Or throw an exception if StoreName is mandatory
    }

    // Basic method to create a JSON response string. Highly simplified.
    public static String createStatusResponseJson(String storeName, String status, String message) {
        // Manual and very basic JSON construction. Prone to errors for complex JSON.
        // Example: {"storeName":"MyStore","status":"SUCCESS","message":"Processed"}
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        if (storeName != null) {
            sb.append("\"storeName\":\"").append(escapeJsonString(storeName)).append("\",");
        }
        sb.append("\"status\":\"").append(escapeJsonString(status)).append("\",");
        sb.append("\"message\":\"").append(escapeJsonString(message)).append("\"");
        sb.append("}");
        return sb.toString();
    }
    
    private static String escapeJsonString(String value) {
        if (value == null) return "";
        // Basic escaping for quotes and backslashes. Not comprehensive.
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public static String createAddProductRequestJson(String productName, String productType, double price, int initialAvailableAmount) {
        // {"productName":"pepsi","productType":"drink","price":1.5,"initialAvailableAmount":100}
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"productName\":\"").append(escapeJsonString(productName)).append("\",");
        sb.append("\"productType\":\"").append(escapeJsonString(productType)).append("\",");
        sb.append("\"price\":").append(price).append(",");
        sb.append("\"initialAvailableAmount\":").append(initialAvailableAmount);
        sb.append("}");
        return sb.toString();
    }

    public static String createRemoveProductRequestJson(String productName) {
        // {"productName":"coke"}
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"productName\":\"").append(escapeJsonString(productName)).append("\"");
        sb.append("}");
        return sb.toString();
    }

    public static String createUpdateStockRequestJson(String productName, int quantityChange) {
        // {"productName":"fanta","quantityChange":20}
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"productName\":\"").append(escapeJsonString(productName)).append("\",");
        sb.append("\"quantityChange\":").append(quantityChange);
        sb.append("}");
        return sb.toString();
    }

    public static String createSalesResponseJson(String queryType, String queryContext, List<com.fooddelivery.communication.payloads.SalesDataEntry> entries, double grandTotal) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"queryType\":\"").append(escapeJsonString(queryType)).append("\",");
        sb.append("\"queryContext\":\"").append(escapeJsonString(queryContext)).append("\",");
        sb.append("\"grandTotalRevenue\":").append(grandTotal).append(",");
        sb.append("\"entries\":[");
        for (int i = 0; i < entries.size(); i++) {
            // SalesDataEntry.toString() is designed to produce a JSON object string.
            // This assumes SalesDataEntry.toString() correctly escapes its internal values.
            sb.append(entries.get(i).toString());
            if (i < entries.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }

    public static String createSalesRequestJson(String storeName, String foodCategory, String productType) {
        // Creates a JSON payload for SalesRequestPayload based on which field is present.
        // Example: {"storeName":"MyStore"} or {"foodCategory":"pizza"}
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean firstField = true;
        if (storeName != null) {
            sb.append("\"storeName\":\"").append(escapeJsonString(storeName)).append("\"");
            firstField = false;
        }
        if (foodCategory != null) {
            if (!firstField) sb.append(",");
            sb.append("\"foodCategory\":\"").append(escapeJsonString(foodCategory)).append("\"");
            firstField = false;
        }
        if (productType != null) {
            if (!firstField) sb.append(",");
            sb.append("\"productType\":\"").append(escapeJsonString(productType)).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }
}
