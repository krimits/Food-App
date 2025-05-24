package com.fooddelivery.util;

import com.fooddelivery.model.Product;
import com.fooddelivery.model.Store;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// WARNING: This is a very rudimentary and fragile manual JSON parser.
// It's provided to meet the "no external libraries" constraint but is not robust
// for complex or malformed JSON. A proper JSON library is always recommended.
public class StoreJsonParser {

    public static Store parseStoreJson(String json) throws JsonParseException {
        if (json == null || json.trim().isEmpty()) {
            throw new JsonParseException("JSON string is null or empty.");
        }

        // Simplify by removing whitespace between tokens, but not within string values
        // This is still very error prone.
        // json = json.replaceAll("\s+(?=(?:[^"]*"[^"]*")*[^"]*$)", "");


        String storeName = extractStringValue(json, "StoreName");
        double latitude = extractDoubleValue(json, "Latitude");
        double longitude = extractDoubleValue(json, "Longitude");
        String foodCategory = extractStringValue(json, "FoodCategory");
        int stars = extractIntValue(json, "Stars");
        int noOfVotes = extractIntValue(json, "NoOfVotes");
        String storeLogo = extractStringValue(json, "StoreLogo");
        
        List<Product> products = parseProductsArray(json);

        if (storeName == null) {
            throw new JsonParseException("StoreName is missing in JSON.");
        }

        Store store = new Store(storeName, latitude, longitude, foodCategory, stars, noOfVotes, storeLogo, products);
        // Price category will be calculated by store.calculateAndSetPriceCategory() later
        return store;
    }

    private static List<Product> parseProductsArray(String json) throws JsonParseException {
        List<Product> products = new ArrayList<>();
        Pattern productsPattern = Pattern.compile("\"Products\"\\s*:\\s*\\[([^\\]]*)\\]");
        Matcher productsMatcher = productsPattern.matcher(json);

        if (productsMatcher.find()) {
            String productsJsonArray = productsMatcher.group(1).trim();
            if (productsJsonArray.isEmpty()) {
                return products; // Empty products list
            }
            // Split individual product objects. This regex assumes objects are separated by commas
            // and correctly handles nested structures *only if they are simple*.
            String[] productJsonObjects = productsJsonArray.split("\\}\\s*,\\s*\\{"); 
            
            for (int i = 0; i < productJsonObjects.length; i++) {
                String productJson = productJsonObjects[i];
                if (i > 0) productJson = "{" + productJson; // Add back leading {
                if (i < productJsonObjects.length - 1) productJson = productJson + "}"; // Add back trailing }
                if (productJsonObjects.length == 1 && !productJson.startsWith("{") && !productJson.endsWith("}")) { // Single object case, ensure curly braces
                    productJson = "{" + productJson + "}";
                } else if (productJsonObjects.length > 1) { // Ensure braces for multiple objects if split removed them
                    if (!productJson.startsWith("{")) productJson = "{" + productJson;
                    if (!productJson.endsWith("}")) productJson = productJson + "}";
                }


                String productName = extractStringValue(productJson, "ProductName");
                String productType = extractStringValue(productJson, "ProductType");
                // Handle "Available Amount" with space
                int availableAmount = extractIntValue(productJson, "Available Amount");
                // The check below is redundant if extractIntValue handles the spaced key correctly.
                // if (availableAmount == 0 && productJson.contains("\"Available Amount\"")) { 
                //      availableAmount = extractIntValue(productJson, "Available Amount");
                // }
                double price = extractDoubleValue(productJson, "Price");

                if (productName == null) {
                    throw new JsonParseException("ProductName is missing in a product JSON object: " + productJson);
                }
                products.add(new Product(productName, productType, availableAmount, price));
            }
        } else {
            // Products array might be missing or empty, which can be valid
            System.out.println("StoreJsonParser: 'Products' array not found or empty in JSON. Assuming no products.");
        }
        return products;
    }

    private static String extractStringValue(String json, String key) throws JsonParseException {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        // Allow some fields to be optional or handle them as needed
        if (key.equals("StoreLogo") || key.equals("ProductType")) return ""; // Example: optional field
        // System.err.println("Warning: Key '" + key + "' not found or not a string in JSON snippet: " + json);
        return null; 
    }

    private static double extractDoubleValue(String json, String key) throws JsonParseException {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*([0-9]*\\.?[0-9]+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) {
                System.err.println("StoreJsonParser: NumberFormatException for key '" + key + "' - value: '" + matcher.group(1) + "' in JSON snippet."); // Log full JSON might be too verbose
                throw new JsonParseException("Invalid double value for key '" + key + "': '" + matcher.group(1) + "'", e);
            }
        }
        // If key not found, returning 0.0 for now. Could throw if key is mandatory.
        // System.err.println("StoreJsonParser: Key '" + key + "' not found for double. JSON: " + json);
        return 0.0; 
    }

    private static int extractIntValue(String json, String key) throws JsonParseException {
         Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*([0-9]+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                 System.err.println("StoreJsonParser: NumberFormatException for key '" + key + "' - value: '" + matcher.group(1) + "' in JSON snippet.");
                throw new JsonParseException("Invalid int value for key '" + key + "': '" + matcher.group(1) + "'", e);
            }
        }
        // If key not found, returning 0 for now.
        // System.err.println("StoreJsonParser: Key '" + key + "' not found for int. JSON: " + json);
        return 0; 
    }

    public static class JsonParseException extends Exception {
        public JsonParseException(String message) {
            super(message);
        }
        public JsonParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // New methods for parsing product management payloads
    public static com.fooddelivery.communication.payloads.AddProductRequestPayload parseAddProductRequestPayload(String json) throws JsonParseException {
        if (json == null || json.trim().isEmpty()) throw new JsonParseException("AddProductRequestPayload JSON is null or empty.");
        String productName = extractStringValue(json, "productName");
        String productType = extractStringValue(json, "productType"); // Can be null if not found by extractStringValue
        double price = extractDoubleValue(json, "price");
        int initialAvailableAmount = extractIntValue(json, "initialAvailableAmount");

        if (productName == null) throw new JsonParseException("productName missing in AddProductRequestPayload.");
        // storeName will be passed separately to worker methods.
        return new com.fooddelivery.communication.payloads.AddProductRequestPayload(null, productName, productType, price, initialAvailableAmount);
    }

    public static com.fooddelivery.communication.payloads.RemoveProductRequestPayload parseRemoveProductRequestPayload(String json) throws JsonParseException {
        if (json == null || json.trim().isEmpty()) throw new JsonParseException("RemoveProductRequestPayload JSON is null or empty.");
        String productName = extractStringValue(json, "productName");
        if (productName == null) throw new JsonParseException("productName missing in RemoveProductRequestPayload.");
        return new com.fooddelivery.communication.payloads.RemoveProductRequestPayload(null, productName);
    }

    public static com.fooddelivery.communication.payloads.UpdateStockRequestPayload parseUpdateStockRequestPayload(String json) throws JsonParseException {
        if (json == null || json.trim().isEmpty()) throw new JsonParseException("UpdateStockRequestPayload JSON is null or empty.");
        String productName = extractStringValue(json, "productName");
        int quantityChange = extractIntValue(json, "quantityChange");
        if (productName == null) throw new JsonParseException("productName missing in UpdateStockRequestPayload.");
        return new com.fooddelivery.communication.payloads.UpdateStockRequestPayload(null, productName, quantityChange);
    }
}
