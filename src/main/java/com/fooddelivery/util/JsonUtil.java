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

    public static String createSearchStoresRequestJson(double lat, double lon, String foodCategory, int minStars, String priceRange) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"clientLatitude\":").append(lat).append(",");
        sb.append("\"clientLongitude\":").append(lon);
        if (foodCategory != null && !foodCategory.isEmpty()) {
            sb.append(",\"foodCategoryFilter\":\"").append(escapeJsonString(foodCategory)).append("\"");
        }
        if (minStars > 0) { // Assuming 0 means not specified
            sb.append(",\"minStarsFilter\":").append(minStars);
        }
        if (priceRange != null && !priceRange.isEmpty()) {
            sb.append(",\"priceRangeFilter\":\"").append(escapeJsonString(priceRange)).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }

    // For SearchStoresResponsePayload (used by Worker and Master)
    public static String createSearchStoresResponseJson(List<com.fooddelivery.communication.payloads.StoreInfoForClient> results) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"results\":[");
        if (results != null) {
            for (int i = 0; i < results.size(); i++) {
                com.fooddelivery.communication.payloads.StoreInfoForClient store = results.get(i);
                // Manually construct JSON for each StoreInfoForClient
                sb.append("{");
                sb.append("\"storeName\":\"").append(escapeJsonString(store.getStoreName())).append("\",");
                sb.append("\"foodCategory\":\"").append(escapeJsonString(store.getFoodCategory())).append("\",");
                sb.append("\"stars\":").append(store.getStars()).append(",");
                sb.append("\"priceCategory\":\"").append(escapeJsonString(store.getPriceCategory())).append("\",");
                sb.append("\"distanceKm\":").append(String.format("%.2f", store.getDistanceKm())).append(","); // Format distance
                sb.append("\"storeLogoPath\":\"").append(escapeJsonString(store.getStoreLogoPath())).append("\",");
                sb.append("\"latitude\":").append(store.getLatitude()).append(",");
                sb.append("\"longitude\":").append(store.getLongitude());
                sb.append("}");
                if (i < results.size() - 1) {
                    sb.append(",");
                }
            }
        }
        sb.append("]}");
        return sb.toString();
    }

    public static String createRateStoreRequestJson(String storeName, int stars) {
        // {"storeName":"MyStore","stars":5}
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        // storeName is also sent as routing key, but including in payload for completeness/validation
        sb.append("\"storeName\":\"").append(escapeJsonString(storeName)).append("\",");
        sb.append("\"stars\":").append(stars);
        sb.append("}");
        return sb.toString();
    }

    public static String createMapTaskRequestJson(String taskTypeIdentifier, String targetCriteria) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"taskTypeIdentifier\":\"").append(escapeJsonString(taskTypeIdentifier)).append("\",");
        sb.append("\"targetCriteria\":\"").append(escapeJsonString(targetCriteria)).append("\"");
        sb.append("}");
        return sb.toString();
    }

    // To create JSON for MapTaskResponsePayload (sent by Worker)
    public static String createMapTaskResponseJson(List<com.fooddelivery.communication.payloads.SalesDataEntry> results) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"mappedResults\":[");
        if (results != null) {
            for (int i = 0; i < results.size(); i++) {
                // Assuming SalesDataEntry.toString() produces a valid JSON object string
                sb.append(results.get(i).toString()); 
                if (i < results.size() - 1) {
                    sb.append(",");
                }
            }
        }
        sb.append("]}");
        return sb.toString();
    }

    public static String createReplicatedStoreDataJson(com.fooddelivery.communication.payloads.ReplicatedStoreData data) {
        if (data == null) return "{}";
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"storeName\":\"").append(escapeJsonString(data.getStoreName())).append("\"");

        if (data.getUpdatedProductStocks() != null && !data.getUpdatedProductStocks().isEmpty()) {
            sb.append(",\"updatedProductStocks\":{");
            boolean firstStock = true;
            for (Map.Entry<String, Integer> entry : data.getUpdatedProductStocks().entrySet()) {
                if (!firstStock) sb.append(",");
                sb.append("\"").append(escapeJsonString(entry.getKey())).append("\":").append(entry.getValue());
                firstStock = false;
            }
            sb.append("}");
        }

        if (data.getNewSaleMade() != null) {
            // SalesDataEntry.toString() should produce a valid JSON object string
            sb.append(",\"newSaleMade\":").append(data.getNewSaleMade().toString());
        }

        if (data.getNewAverageStars() != null && data.getNewTotalVotes() != null) {
            sb.append(",\"newRatingInfo\":{"); // Nest rating info
            sb.append("\"newAverageStars\":").append(data.getNewAverageStars()).append(",");
            sb.append("\"newTotalVotes\":").append(data.getNewTotalVotes());
            sb.append("}");
        }
        sb.append("}");
        return sb.toString();
    }

    // For Worker creating PurchaseProcessingDetailsResponsePayload JSON
    public static String createPurchaseProcessingDetailsResponseJson(com.fooddelivery.communication.payloads.PurchaseProcessingDetailsResponsePayload payload) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"storeName\":\"").append(escapeJsonString(payload.getStoreName())).append("\",");
        sb.append("\"status\":\"").append(escapeJsonString(payload.getStatus())).append("\",");
        sb.append("\"message\":\"").append(escapeJsonString(payload.getMessage())).append("\"");

        if (payload.getUpdatedProductStocks() != null && !payload.getUpdatedProductStocks().isEmpty()) {
            sb.append(",\"updatedProductStocks\":{");
            boolean firstStock = true;
            for (Map.Entry<String, Integer> entry : payload.getUpdatedProductStocks().entrySet()) {
                if (!firstStock) sb.append(",");
                sb.append("\"").append(escapeJsonString(entry.getKey())).append("\":").append(entry.getValue());
                firstStock = false;
            }
            sb.append("}");
        }
        if (payload.getSaleRecorded() != null) {
            sb.append(",\"saleRecorded\":").append(payload.getSaleRecorded().toString());
        }
        sb.append("}");
        return sb.toString();
    }

    // For Worker creating RatingProcessingDetailsResponsePayload JSON
    public static String createRatingProcessingDetailsResponseJson(com.fooddelivery.communication.payloads.RatingProcessingDetailsResponsePayload payload) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"storeName\":\"").append(escapeJsonString(payload.getStoreName())).append("\",");
        sb.append("\"status\":\"").append(escapeJsonString(payload.getStatus())).append("\",");
        sb.append("\"message\":\"").append(escapeJsonString(payload.getMessage())).append("\",");
        sb.append("\"newAverageStars\":").append(payload.getNewAverageStars()).append(",");
        sb.append("\"newTotalVotes\":").append(payload.getNewTotalVotes());
        sb.append("}");
        return sb.toString();
    }
}
