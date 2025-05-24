package com.fooddelivery.client.android.network;

import com.fooddelivery.communication.payloads.*; // Using the shared payload classes
import com.fooddelivery.client.android.model.CartItem; // Though CartItem is client model, PurchaseRequest uses OrderItemPayload

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientJsonUtil {

    // --- Private Helper Methods (similar to backend JsonUtil/StoreJsonParser but may be repeated/adapted for client context) ---

    private static String escapeJsonString(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\""); // Basic escaping
    }

    // Basic value extractors - these are very simplified and assume keys are unique at their expected level
    private static String extractStringValue(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static int extractIntValue(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*([0-9]+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) { /* Log or handle */ }
        }
        return 0; // Default or throw
    }
    
    private static double extractDoubleValue(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*([0-9]*\\.?[0-9]+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) { /* Log or handle */ }
        }
        return 0.0; // Default or throw
    }

    // --- Serialization Methods (POJO to JSON String) ---

    public static String toJson(SearchStoresRequestPayload payload) {
        if (payload == null) return "{}";
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"clientLatitude\":").append(payload.getClientLatitude()).append(",");
        sb.append("\"clientLongitude\":").append(payload.getClientLongitude());
        if (payload.getFoodCategoryFilter() != null && !payload.getFoodCategoryFilter().isEmpty()) {
            sb.append(",\"foodCategoryFilter\":\"").append(escapeJsonString(payload.getFoodCategoryFilter())).append("\"");
        }
        if (payload.getMinStarsFilter() > 0) {
            sb.append(",\"minStarsFilter\":").append(payload.getMinStarsFilter());
        }
        if (payload.getPriceRangeFilter() != null && !payload.getPriceRangeFilter().isEmpty()) {
            sb.append(",\"priceRangeFilter\":\"").append(escapeJsonString(payload.getPriceRangeFilter())).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }

    public static String toJson(PurchaseRequestPayload payload) {
        if (payload == null) return "{}";
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        // storeName is usually part of routing key, but payload includes it too
        sb.append("\"storeName\":\"").append(escapeJsonString(payload.getStoreName())).append("\","); 
        sb.append("\"items\":[");
        if (payload.getItems() != null) {
            for (int i = 0; i < payload.getItems().size(); i++) {
                OrderItemPayload item = payload.getItems().get(i);
                sb.append("{");
                sb.append("\"productName\":\"").append(escapeJsonString(item.getProductName())).append("\",");
                sb.append("\"quantity\":").append(item.getQuantity());
                sb.append("}");
                if (i < payload.getItems().size() - 1) {
                    sb.append(",");
                }
            }
        }
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }

    public static String toJson(RateStoreRequestPayload payload) {
        if (payload == null) return "{}";
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        // storeName is usually part of routing key, but payload includes it too
        sb.append("\"storeName\":\"").append(escapeJsonString(payload.getStoreName())).append("\",");
        sb.append("\"stars\":").append(payload.getStars());
        sb.append("}");
        return sb.toString();
    }

    // --- Deserialization Methods (JSON String to POJO) ---

    public static SearchStoresResponsePayload fromSearchStoresResponseJson(String json) {
        if (json == null || json.trim().isEmpty()) return new SearchStoresResponsePayload(new ArrayList<>());
        
        List<StoreInfoForClient> results = new ArrayList<>();
        // This parsing logic is extremely basic and fragile. Reuses simplified logic from ClientJsonParser.
        Pattern resultsArrayPattern = Pattern.compile("\"results\"\\s*:\\s*\\[([\\s\\S]*?)\\]");
        Matcher resultsArrayMatcher = resultsArrayPattern.matcher(json);

        if (resultsArrayMatcher.find()) {
            String resultsJson = resultsArrayMatcher.group(1).trim();
            if (!resultsJson.isEmpty()) {
                String[] storeJsonObjects = resultsJson.split("\\}\\s*,\\s*\\{"); 
                for (int i = 0; i < storeJsonObjects.length; i++) {
                    String storeJson = storeJsonObjects[i];
                    if (storeJsonObjects.length > 1) {
                         if (i == 0 && !storeJson.startsWith("{")) storeJson = "{" + storeJson + "}";
                         else if (i == storeJsonObjects.length - 1 && !storeJson.endsWith("}")) storeJson = storeJson + "}";
                         else if (i > 0 && i < storeJsonObjects.length -1 ) storeJson = "{" + storeJson + "}";
                         else if (storeJsonObjects.length == 1 && !storeJson.startsWith("{") && !storeJson.endsWith("}")) storeJson = "{" + storeJson + "}";

                    }  else if (!storeJson.startsWith("{") && !storeJson.endsWith("}")) {
                         storeJson = "{" + storeJson + "}";
                    }
                    if (!storeJson.startsWith("{") && storeJson.contains(":")) storeJson = "{" + storeJson;
                    if (!storeJson.endsWith("}") && storeJson.contains(":")) storeJson = "{" + storeJson + "}";

                    if (storeJson.trim().equals("{}") || storeJson.trim().isEmpty() || !storeJson.contains(":")) continue;

                    String name = extractStringValue(storeJson, "storeName");
                    if (name != null) { // Basic check
                         results.add(new StoreInfoForClient(
                            name,
                            extractStringValue(storeJson, "foodCategory"),
                            extractIntValue(storeJson, "stars"),
                            extractStringValue(storeJson, "priceCategory"),
                            extractDoubleValue(storeJson, "distanceKm"),
                            extractStringValue(storeJson, "storeLogoPath"),
                            extractDoubleValue(storeJson, "latitude"),
                            extractDoubleValue(storeJson, "longitude")
                        ));
                    }
                }
            }
        }
        return new SearchStoresResponsePayload(results);
    }

    public static StatusResponsePayload fromStatusResponseJson(String json) {
        if (json == null || json.trim().isEmpty()) return new StatusResponsePayload("FAILURE", "Empty or null server response");
        
        String storeName = extractStringValue(json, "storeName"); // Optional
        String status = extractStringValue(json, "status");
        String message = extractStringValue(json, "message");

        if (status == null) status = "FAILURE"; // Default if parsing fails
        if (message == null) message = "Could not parse server status response.";

        return new StatusResponsePayload(storeName, status, message);
    }
    
    // Exception class for parsing errors (optional, can just return null or throw RuntimeException)
    public static class ClientJsonParseException extends Exception {
        public ClientJsonParseException(String message) {
            super(message);
        }
    }
}
