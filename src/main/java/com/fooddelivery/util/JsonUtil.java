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
}
