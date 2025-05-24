package com.fooddelivery.client.android.viewmodels;

import com.fooddelivery.client.android.repository.StoreRepository;
import com.fooddelivery.client.android.repository.ResponseCallback; // Using the repository's callback
import com.fooddelivery.communication.payloads.SearchStoresRequestPayload;
import com.fooddelivery.communication.payloads.SearchStoresResponsePayload;
import com.fooddelivery.communication.payloads.StoreInfoForClient;
import com.fooddelivery.client.android.network.MasterServerConnector; // For main method simulation

import java.util.ArrayList;
import java.util.List;

// Illustrative ViewModel for search functionality
public class SearchViewModel {

    private StoreRepository storeRepository;

    // Conceptual state that would be observed by UI (e.g., using LiveData in Android)
    private List<StoreInfoForClient> searchResults;
    private boolean isLoading;
    private String errorMessage;

    // Search filter parameters - typically bound to UI input fields
    private double currentLatitude = 0.0; // Default or obtained from GPS
    private double currentLongitude = 0.0; // Default or obtained from GPS
    private String foodCategoryFilter = "";
    private int minStarsFilter = 0; // 0 means no filter
    private String priceRangeFilter = ""; // e.g., "$", "$$", "$$$", empty means no filter

    public SearchViewModel() {
        this.storeRepository = StoreRepository.getInstance();
        this.searchResults = new ArrayList<>();
        this.isLoading = false;
        this.errorMessage = null;
    }

    // --- Methods to update filter parameters (called from UI/Activity) ---
    public void setLocation(double latitude, double longitude) {
        this.currentLatitude = latitude;
        this.currentLongitude = longitude;
    }

    public void setFoodCategoryFilter(String category) {
        this.foodCategoryFilter = category == null ? "" : category;
    }

    public void setMinStarsFilter(int stars) {
        this.minStarsFilter = Math.max(0, stars); // Ensure non-negative
    }

    public void setPriceRangeFilter(String range) {
        this.priceRangeFilter = range == null ? "" : range;
    }

    // --- Method to trigger search ---
    public void executeSearch() {
        System.out.println("SearchViewModel: Initiating search with params: Lat=" + currentLatitude + 
                           ", Lon=" + currentLongitude + ", Cat='" + foodCategoryFilter + 
                           "', Stars=" + minStarsFilter + ", Price='" + priceRangeFilter + "'");
        
        isLoading = true;
        errorMessage = null;
        // In real Android, notify observers about loading state change here

        SearchStoresRequestPayload requestPayload = new SearchStoresRequestPayload(
            currentLatitude,
            currentLongitude,
            foodCategoryFilter,
            minStarsFilter,
            priceRangeFilter
        );

        storeRepository.searchStores(requestPayload, new ResponseCallback<SearchStoresResponsePayload>() {
            @Override
            public void onSuccess(SearchStoresResponsePayload result) {
                isLoading = false;
                if (result != null && result.getResults() != null) {
                    searchResults = result.getResults();
                    System.out.println("SearchViewModel: Search successful. Found " + searchResults.size() + " stores.");
                    // In real Android, notify observers of new results
                    for(StoreInfoForClient store : searchResults) {
                        System.out.println("  - " + store.getStoreName() + " (Dist: " + String.format("%.2f",store.getDistanceKm())+"km)");
                    }
                } else {
                    searchResults = new ArrayList<>(); // Clear previous results
                    System.out.println("SearchViewModel: Search successful but no results found or null payload.");
                }
                // Notify observers
            }

            @Override
            public void onError(String errorMsg) {
                isLoading = false;
                errorMessage = errorMsg;
                searchResults = new ArrayList<>(); // Clear previous results on error
                System.err.println("SearchViewModel: Search failed: " + errorMessage);
                // In real Android, notify observers of error
            }
        });
    }

    // --- Getters for conceptual state (for UI to observe) ---
    public List<StoreInfoForClient> getSearchResults() {
        return searchResults; // In real Android, this would be LiveData<List<StoreInfoForClient>>
    }

    public boolean isLoading() {
        return isLoading; // LiveData<Boolean>
    }

    public String getErrorMessage() {
        return errorMessage; // LiveData<String>
    }

    // --- Example Main method for simulation (not part of actual Android ViewModel) ---
    public static void main(String[] args) {
        // This is a simple simulation.
        // Assumes MasterServerConnector can connect to a running Master.
        // MasterServerConnector needs to be connected before repository methods are called.
        
        MasterServerConnector.getInstance().connect("localhost", 6000, new MasterServerConnector.ResponseCallback() {
            @Override
            public void onSuccess(String response) {
                System.out.println("SIMULATION: Connected to Master: " + response);
                
                SearchViewModel viewModel = new SearchViewModel();
                // Simulate setting search parameters
                viewModel.setLocation(37.9932963, 23.733413); // Example coordinates
                // viewModel.setFoodCategoryFilter("pizzeria");
                // viewModel.setMinStarsFilter(3);
                // viewModel.setPriceRangeFilter("$$");

                viewModel.executeSearch();

                // In a real app, you wouldn't sleep the main thread.
                // This is just to allow async operations to potentially complete for the demo.
                try {
                    Thread.sleep(5000); // Wait for search to complete (crude)
                     System.out.println("SIMULATION: Final results in VM: " + viewModel.getSearchResults().size() + " stores.");
                     if(viewModel.getErrorMessage() != null) System.err.println("SIMULATION: Error: " + viewModel.getErrorMessage());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                MasterServerConnector.getInstance().disconnect(); // Disconnect when done
            }

            @Override
            public void onError(Exception e) {
                System.err.println("SIMULATION: Failed to connect to Master: " + e.getMessage());
            }
        });
         try { Thread.sleep(7000); } catch (InterruptedException e) { e.printStackTrace(); } // Keep main alive for sim
    }
}
