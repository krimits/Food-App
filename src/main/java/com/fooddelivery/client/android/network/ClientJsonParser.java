package com.fooddelivery.client.android.network; // Or a common util package if shared with Master

import com.fooddelivery.communication.payloads.StoreInfoForClient;
import com.fooddelivery.communication.payloads.SearchStoresResponsePayload;
import com.fooddelivery.communication.payloads.SearchStoresRequestPayload; // Added import
import com.fooddelivery.util.StoreJsonParser; // For reusing basic value extractors

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientJsonParser {

    // Parses a JSON string representing SearchStoresResponsePayload
    public static SearchStoresResponsePayload parseSearchStoresResponse(String jsonResponse) throws StoreJsonParser.JsonParseException {
        if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
            throw new StoreJsonParser.JsonParseException("SearchStoresResponse JSON is null or empty.");
        }

        List<StoreInfoForClient> results = new ArrayList<>();
        // Regex to find the "results" array: "results"\s*:\s*\[(.*?)\]
        // The (.*?) part should capture everything inside the brackets.
        // Need to be careful with nested structures if any, but StoreInfoForClient is flat.
        Pattern resultsArrayPattern = Pattern.compile("\"results\"\\s*:\\s*\\[([\\s\\S]*?)\\]");
        Matcher resultsArrayMatcher = resultsArrayPattern.matcher(jsonResponse);

        if (resultsArrayMatcher.find()) {
            String resultsJson = resultsArrayMatcher.group(1).trim();
            if (!resultsJson.isEmpty()) {
                // Split individual StoreInfoForClient objects.
                // This assumes objects are enclosed in {} and separated by commas.
                // A simple split by "}," might work if we then add back "}" and handle the last element.
                String[] storeJsonObjects = resultsJson.split("\\}\\s*,\\s*\\{"); 
                for (int i = 0; i < storeJsonObjects.length; i++) {
                    String storeJson = storeJsonObjects[i];
                    if (storeJsonObjects.length > 1) { // If multiple objects
                        if (i == 0 && !storeJson.startsWith("{")) storeJson = "{" + storeJson; 
                        if (i == storeJsonObjects.length - 1 && !storeJson.endsWith("}")) storeJson = storeJson + "}";
                        else if (i > 0 && i < storeJsonObjects.length -1 ) storeJson = "{" + storeJson + "}";
                        // if it's a middle element, it needs both { and }
                        else if (i > 0 && !storeJson.startsWith("{")) storeJson = "{" + storeJson;
                        else if (i < storeJsonObjects.length -1 && !storeJson.endsWith("}")) storeJson = storeJson + "}";


                    } else if (!storeJson.startsWith("{") && !storeJson.endsWith("}")) { // Single object, ensure it's wrapped
                         storeJson = "{" + storeJson + "}";
                    }
                     
                    // Ensure braces if they were stripped by split
                    if (!storeJson.startsWith("{") && storeJson.contains(":")) storeJson = "{" + storeJson;
                    if (!storeJson.endsWith("}") && storeJson.contains(":")) storeJson = storeJson + "}";


                    if (storeJson.trim().equals("{}") || storeJson.trim().isEmpty() || !storeJson.contains(":")) continue;
                    
                    results.add(parseStoreInfoForClient(storeJson));
                }
            }
        } else {
            // This could mean an empty results array "[]" or malformed JSON.
            if (!jsonResponse.contains("\"results\"\\s*:\\s*\\[")) {
                 // System.err.println("ClientJsonParser: 'results' array structure not found in SearchStoresResponse.");
            }
        }
        return new SearchStoresResponsePayload(results);
    }

    // Parses a JSON string representing a single StoreInfoForClient object
    private static StoreInfoForClient parseStoreInfoForClient(String storeJson) throws StoreJsonParser.JsonParseException {
        // Reusing extractors from StoreJsonParser (they are static)
        // Ensure these keys match exactly what the Worker sends.
        String storeName = StoreJsonParser.extractStringValue(storeJson, "storeName");
        String foodCategory = StoreJsonParser.extractStringValue(storeJson, "foodCategory");
        int stars = StoreJsonParser.extractIntValue(storeJson, "stars");
        String priceCategory = StoreJsonParser.extractStringValue(storeJson, "priceCategory");
        double distanceKm = StoreJsonParser.extractDoubleValue(storeJson, "distanceKm");
        String storeLogoPath = StoreJsonParser.extractStringValue(storeJson, "storeLogoPath");
        double latitude = StoreJsonParser.extractDoubleValue(storeJson, "latitude");
        double longitude = StoreJsonParser.extractDoubleValue(storeJson, "longitude");

        if (storeName == null) {
            // System.err.println("ClientJsonParser: storeName is null in storeJson: " + storeJson);
            throw new StoreJsonParser.JsonParseException("parseStoreInfoForClient: storeName is missing. JSON: " + storeJson);
        }
        
        return new StoreInfoForClient(storeName, foodCategory, stars, priceCategory, distanceKm, storeLogoPath, latitude, longitude);
    }

    // Method to parse SearchStoresRequestPayload (needed by Worker)
    public static SearchStoresRequestPayload parseSearchStoresRequest(String jsonRequest) throws StoreJsonParser.JsonParseException {
        if (jsonRequest == null || jsonRequest.trim().isEmpty()) {
            throw new StoreJsonParser.JsonParseException("SearchStoresRequest JSON is null or empty.");
        }
        double lat = StoreJsonParser.extractDoubleValue(jsonRequest, "clientLatitude");
        double lon = StoreJsonParser.extractDoubleValue(jsonRequest, "clientLongitude");
        String foodCat = StoreJsonParser.extractStringValue(jsonRequest, "foodCategoryFilter");
        int minStars = StoreJsonParser.extractIntValue(jsonRequest, "minStarsFilter");
        String priceRange = StoreJsonParser.extractStringValue(jsonRequest, "priceRangeFilter");

        return new SearchStoresRequestPayload(lat, lon, foodCat, minStars, priceRange);
    }

    public static com.fooddelivery.communication.payloads.RateStoreRequestPayload parseRateStoreRequestPayload(String jsonRequest) throws StoreJsonParser.JsonParseException {
        if (jsonRequest == null || jsonRequest.trim().isEmpty()) {
            throw new StoreJsonParser.JsonParseException("RateStoreRequest JSON is null or empty.");
        }
        // storeName might be parsed here if not relying solely on routing key, but for worker, routing key is primary.
        String storeName = StoreJsonParser.extractStringValue(jsonRequest, "storeName"); // Optional here if routing key is used
        int stars = StoreJsonParser.extractIntValue(jsonRequest, "stars");
        if (stars < 1 || stars > 5) {
            throw new StoreJsonParser.JsonParseException("Stars must be between 1 and 5. Received: " + stars);
        }
        return new com.fooddelivery.communication.payloads.RateStoreRequestPayload(storeName, stars);
    }

    public static MapTaskRequestPayload parseMapTaskRequestPayload(String json) throws StoreJsonParser.JsonParseException {
        if (json == null || json.trim().isEmpty()) throw new StoreJsonParser.JsonParseException("MapTaskRequestPayload JSON is null or empty.");
        String taskType = StoreJsonParser.extractStringValue(json, "taskTypeIdentifier");
        String criteria = StoreJsonParser.extractStringValue(json, "targetCriteria");
        if (taskType == null || criteria == null) throw new StoreJsonParser.JsonParseException("taskTypeIdentifier or targetCriteria missing in MapTaskRequestPayload.");
        return new MapTaskRequestPayload(taskType, criteria);
    }

    public static MapTaskResponsePayload parseMapTaskResponsePayload(String json) throws StoreJsonParser.JsonParseException {
        if (json == null || json.trim().isEmpty()) throw new StoreJsonParser.JsonParseException("MapTaskResponsePayload JSON is null or empty.");
        List<SalesDataEntry> results = new ArrayList<>();
        Pattern resultsArrayPattern = Pattern.compile("\"mappedResults\"\\s*:\\s*\\[([\\s\\S]*?)\\]");
        Matcher resultsArrayMatcher = resultsArrayPattern.matcher(json);

        if (resultsArrayMatcher.find()) {
            String resultsJson = resultsArrayMatcher.group(1).trim();
            if (!resultsJson.isEmpty()) {
                // This splitting logic is very basic and fragile for complex JSON arrays.
                String[] entryJsonObjects = resultsJson.split("\\}\\s*,\\s*\\{"); 
                for (int i = 0; i < entryJsonObjects.length; i++) {
                    String entryJson = entryJsonObjects[i];
                    if (entryJsonObjects.length > 1) {
                        if (i == 0 && !entryJson.startsWith("{")) entryJson = "{" + entryJson + "}";
                        else if (i == entryJsonObjects.length - 1 && !entryJson.endsWith("}")) entryJson = entryJson + "}";
                        else if (i > 0 && i < entryJsonObjects.length -1) entryJson = "{" + entryJson + "}";
                         else if (entryJsonObjects.length == 1 && !entryJson.startsWith("{")) entryJson = "{" + entryJson + "}";

                    } else if (!entryJson.startsWith("{") && !entryJson.endsWith("}")) {
                         entryJson = "{" + entryJson + "}";
                    }
                    if (!entryJson.startsWith("{") && entryJson.contains(":")) entryJson = "{" + entryJson;
                    if (!entryJson.endsWith("}") && entryJson.contains(":")) entryJson = "{" + entryJson + "}";
                    
                    if (entryJson.trim().equals("{}") || entryJson.trim().isEmpty() || !entryJson.contains(":")) continue;
                    
                    // Assuming SalesDataEntry has fields: itemName, totalQuantity, totalRevenue
                    String itemName = StoreJsonParser.extractStringValue(entryJson, "itemName");
                    int quantity = StoreJsonParser.extractIntValue(entryJson, "totalQuantity"); // May not be relevant here
                    double revenue = StoreJsonParser.extractDoubleValue(entryJson, "totalRevenue");
                    if(itemName != null) results.add(new SalesDataEntry(itemName, quantity, revenue));
                }
            }
        }
        return new MapTaskResponsePayload(results);
    }
}
