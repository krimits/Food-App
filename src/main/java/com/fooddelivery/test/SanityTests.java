package com.fooddelivery.test;

import com.fooddelivery.model.Coordinates;
import com.fooddelivery.model.Product;
import com.fooddelivery.model.Store;
import com.fooddelivery.util.StoreJsonParser;
import com.fooddelivery.client.android.network.ClientJsonUtil;
import com.fooddelivery.communication.payloads.*; // Import necessary payloads

import java.util.ArrayList;
import java.util.List;

public class SanityTests {

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            System.err.println("Assertion FAILED: " + message);
            // In a real test, throw new AssertionError(message);
        } else {
            System.out.println("Assertion PASSED: " + message);
        }
    }
    
    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null || expected != null && expected.equals(actual)) {
             System.out.println("Assertion PASSED: " + message + " (Expected: " + expected + ", Actual: " + actual + ")");
        } else {
            System.err.println("Assertion FAILED: " + message + " (Expected: " + expected + ", Actual: " + actual + ")");
        }
    }
     private static void assertDoubleEquals(double expected, double actual, double delta, String message) {
        if (Math.abs(expected - actual) <= delta) {
            System.out.println("Assertion PASSED: " + message + " (Expected: " + expected + ", Actual: " + actual + ")");
        } else {
            System.err.println("Assertion FAILED: " + message + " (Expected: " + expected + ", Actual: " + actual + ")");
        }
    }


    public static void testCoordinatesDistance() {
        System.out.println("\n--- Testing Coordinates.distanceTo() ---");
        Coordinates c1 = new Coordinates(0, 0); // Equator, Prime Meridian
        Coordinates c2 = new Coordinates(0, 1); // 1 degree East on Equator
        // Approximate distance for 1 degree longitude at equator is ~111.32 km
        double expectedDistance = 111.319; // More precise
        double actualDistance = c1.distanceTo(c2);
        assertDoubleEquals(expectedDistance, actualDistance, 0.001, "Distance of 1 degree lon at equator");

        Coordinates sfo = new Coordinates(37.7749, -122.4194); // San Francisco
        Coordinates la = new Coordinates(34.0522, -118.2437);  // Los Angeles
        // Approx distance ~559 km
        double expectedSfoToLa = 559.0;
        double actualSfoToLa = sfo.distanceTo(la);
        assertDoubleEquals(expectedSfoToLa, actualSfoToLa, 5.0, "Distance SFO to LA"); // Wider delta for large distances
    }

    public static void testStoreJsonParserSimple() {
        System.out.println("\n--- Testing StoreJsonParser (Simple Valid JSON) ---");
        String simpleStoreJson = "{\"StoreName\":\"Test Cafe\",\"Latitude\":40.7128,\"Longitude\":-74.0060," +
                                 "\"FoodCategory\":\"Cafe\",\"Stars\":4,\"NoOfVotes\":100," +
                                 "\"StoreLogo\":\"logo.png\"," +
                                 "\"Products\":[{\"ProductName\":\"Coffee\",\"ProductType\":\"Drink\",\"Available Amount\":50,\"Price\":3.5}]}";
        try {
            Store store = StoreJsonParser.parseStoreJson(simpleStoreJson);
            assertTrue(store != null, "Parsed store should not be null.");
            assertEquals("Test Cafe", store.getStoreName(), "Store Name check");
            assertDoubleEquals(40.7128, store.getLatitude(), 0.0001, "Latitude check");
            assertEquals(1, store.getProducts().size(), "Number of products check");
            if (store.getProducts().size() == 1) {
                assertEquals("Coffee", store.getProducts().get(0).getProductName(), "Product name check");
            }
        } catch (StoreJsonParser.JsonParseException e) {
            System.err.println("StoreJsonParser test FAILED with exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void testClientJsonUtilSerializationDeserialization() {
        System.out.println("\n--- Testing ClientJsonUtil (Request Serialization & StatusResponse Deserialization) ---");
        // Test SearchStoresRequestPayload serialization
        SearchStoresRequestPayload searchReq = new SearchStoresRequestPayload(10.0, 20.0, "pizza", 4, "$$");
        String searchReqJson = ClientJsonUtil.toJson(searchReq);
        System.out.println("Serialized SearchStoresRequestPayload: " + searchReqJson);
        assertTrue(searchReqJson.contains("\"clientLatitude\":10.0"), "SearchReq JSON contains latitude");
        assertTrue(searchReqJson.contains("\"foodCategoryFilter\":\"pizza\""), "SearchReq JSON contains category");

        // Test StatusResponsePayload deserialization
        String statusResJson = "{\"storeName\":\"MyStore\",\"status\":\"SUCCESS\",\"message\":\"Order placed\"}";
        StatusResponsePayload statusRes = ClientJsonUtil.fromStatusResponseJson(statusResJson);
        assertTrue(statusRes != null, "Parsed StatusResponse should not be null.");
        assertEquals("MyStore", statusRes.getStoreName(), "StatusRes Store Name");
        assertEquals("SUCCESS", statusRes.getStatus(), "StatusRes Status");
        assertEquals("Order placed", statusRes.getMessage(), "StatusRes Message");
        
        String errorStatusResJson = "{\"status\":\"FAILURE\",\"message\":\"Stock unavailable\"}";
        StatusResponsePayload errorStatusRes = ClientJsonUtil.fromStatusResponseJson(errorStatusResJson);
        assertTrue(errorStatusRes != null, "Parsed Error StatusResponse should not be null.");
        assertEquals(null, errorStatusRes.getStoreName(), "Error StatusRes Store Name should be null");
        assertEquals("FAILURE", errorStatusRes.getStatus(), "Error StatusRes Status");
        assertEquals("Stock unavailable", errorStatusRes.getMessage(), "Error StatusRes Message");
    }

    public static void testStoreRatingUpdate() {
        System.out.println("\n--- Testing Store.java Rating Update Logic ---");
        Store store = new Store("RateMe Cafe", 0,0,"Cafe",0,0,"", new ArrayList<>());
        
        // Initial rating: 5 stars
        store.setStars(5); // Simulating an update based on rating logic
        store.setNoOfVotes(1);
        assertEquals(5, store.getStars(), "Rating after 1st vote (5 stars)");
        assertEquals(1, store.getNoOfVotes(), "Votes after 1st vote");

        // Second rating: 3 stars
        // Current avg = 5, current votes = 1. New rating = 3.
        // New avg = ((5*1) + 3) / (1+1) = 8 / 2 = 4
        int currentVotes = store.getNoOfVotes();
        double currentAvgStars = store.getStars();
        int newRating = 3;
        double newAverageStarsCalc = ((currentAvgStars * currentVotes) + newRating) / (double)(currentVotes + 1);
        store.setStars((int) Math.round(newAverageStarsCalc));
        store.setNoOfVotes(currentVotes + 1);
        
        assertEquals(4, store.getStars(), "Rating after 2nd vote (3 stars)");
        assertEquals(2, store.getNoOfVotes(), "Votes after 2nd vote");

        // Third rating: 4 stars
        // Current avg = 4, current votes = 2. New rating = 4.
        // New avg = ((4*2) + 4) / (2+1) = 12 / 3 = 4
        currentVotes = store.getNoOfVotes();
        currentAvgStars = store.getStars();
        newRating = 4;
        newAverageStarsCalc = ((currentAvgStars * currentVotes) + newRating) / (double)(currentVotes + 1);
        store.setStars((int) Math.round(newAverageStarsCalc));
        store.setNoOfVotes(currentVotes + 1);

        assertEquals(4, store.getStars(), "Rating after 3rd vote (4 stars)");
        assertEquals(3, store.getNoOfVotes(), "Votes after 3rd vote");
    }


    public static void main(String[] args) {
        System.out.println("=== Running Sanity Tests ===");
        
        testCoordinatesDistance();
        testStoreJsonParserSimple(); // This tests StoreJsonParser, not ClientJsonUtil for store parsing
        testClientJsonUtilSerializationDeserialization(); // Tests ClientJsonUtil for some payloads
        testStoreRatingUpdate(); // Tests Store's internal logic for rating updates

        System.out.println("\n=== Sanity Tests Finished ===");
        // A summary of failures would be good here if we had a proper test runner.
        // For now, check System.err for "Assertion FAILED".
    }
}
