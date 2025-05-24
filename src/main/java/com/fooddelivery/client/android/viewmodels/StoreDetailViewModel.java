package com.fooddelivery.client.android.viewmodels;

import com.fooddelivery.client.android.repository.StoreRepository;
import com.fooddelivery.client.android.repository.ResponseCallback;
import com.fooddelivery.communication.payloads.RateStoreRequestPayload;
import com.fooddelivery.communication.payloads.StatusResponsePayload;
import com.fooddelivery.communication.payloads.StoreInfoForClient; // Assuming this is used to display store details
import com.fooddelivery.client.android.network.MasterServerConnector; // For main method simulation

public class StoreDetailViewModel {

    private StoreRepository storeRepository;
    private StoreInfoForClient currentStore; // Holds the details of the store being viewed

    // Conceptual state
    private boolean isLoadingRating;
    private String ratingStatusMessage;

    public StoreDetailViewModel() {
        this.storeRepository = StoreRepository.getInstance();
    }

    public void setCurrentStore(StoreInfoForClient store) {
        this.currentStore = store;
        // In real Android, fetch full product list for this store here if needed
        System.out.println("StoreDetailViewModel: Displaying details for store: " + (store != null ? store.getStoreName() : "null"));
    }

    public StoreInfoForClient getCurrentStore() {
        return currentStore;
    }

    public void submitRating(int stars) {
        if (currentStore == null) {
            ratingStatusMessage = "Error: No store selected to rate.";
            System.err.println(ratingStatusMessage);
            return;
        }
        if (stars < 1 || stars > 5) {
            ratingStatusMessage = "Error: Rating must be between 1 and 5 stars.";
            System.err.println(ratingStatusMessage);
            return;
        }

        System.out.println("StoreDetailViewModel: Submitting rating of " + stars + " stars for store " + currentStore.getStoreName());
        isLoadingRating = true;
        ratingStatusMessage = null;
        // Notify observers loading

        RateStoreRequestPayload requestPayload = new RateStoreRequestPayload(currentStore.getStoreName(), stars);

        storeRepository.rateStore(requestPayload, new ResponseCallback<StatusResponsePayload>() {
            @Override
            public void onSuccess(StatusResponsePayload result) {
                isLoadingRating = false;
                if (result != null && "SUCCESS".equalsIgnoreCase(result.getStatus())) {
                    ratingStatusMessage = "Rating successful! " + (result.getMessage() != null ? result.getMessage() : "");
                    System.out.println("StoreDetailViewModel: " + ratingStatusMessage);
                    // Optionally update currentStore's displayed rating if response includes new avg
                } else {
                    ratingStatusMessage = "Rating failed: " + (result != null && result.getMessage() != null ? result.getMessage() : "Unknown error.");
                    System.err.println("StoreDetailViewModel: " + ratingStatusMessage);
                }
                // Notify observers
            }

            @Override
            public void onError(String errorMsg) {
                isLoadingRating = false;
                ratingStatusMessage = "Rating submission error: " + errorMsg;
                System.err.println("StoreDetailViewModel: " + ratingStatusMessage);
                // Notify observers
            }
        });
    }

    public boolean isLoadingRating() { return isLoadingRating; }
    public String getRatingStatusMessage() { return ratingStatusMessage; }
    
    // Example Main for simulation
    public static void main(String[] args) {
        // Assumes MasterServerConnector is connected
        MasterServerConnector.getInstance().connect("localhost", 6000, new MasterServerConnector.ResponseCallback() {
            @Override
            public void onSuccess(String response) {
                System.out.println("SIMULATION (StoreDetailVM): Connected to Master: " + response);

                StoreDetailViewModel detailViewModel = new StoreDetailViewModel();
                CartViewModel cartViewModel = new CartViewModel(); // For adding items

                // Simulate selecting a store
                StoreInfoForClient dummyStore = new StoreInfoForClient(
                    "Test Store For Rating & Cart", "pizzeria", 4, "$$", 1.5, "logo.png", 38.0, 23.7);
                detailViewModel.setCurrentStore(dummyStore);
                cartViewModel.setCurrentStore(dummyStore.getStoreName()); // Make sure CartViewModel also knows the store

                // Simulate adding items to cart
                cartViewModel.addOrUpdateItem("Pizza Margarita", 10.0, 2);
                cartViewModel.addOrUpdateItem("Coke", 2.0, 4);
                System.out.println("SIMULATION (StoreDetailVM): Cart Total: $" + String.format("%.2f", cartViewModel.getTotalCartPrice()));
                
                // Simulate checkout
                cartViewModel.checkout();
                try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); } // Wait for purchase
                System.out.println("SIMULATION (StoreDetailVM): Purchase status: " + cartViewModel.getStatusMessage());


                // Simulate rating the store
                detailViewModel.submitRating(5);
                try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); } // Wait for rating
                System.out.println("SIMULATION (StoreDetailVM): Rating status: " + detailViewModel.getRatingStatusMessage());

                MasterServerConnector.getInstance().disconnect();
            }
            @Override
            public void onError(Exception e) {
                System.err.println("SIMULATION (StoreDetailVM): Failed to connect to Master: " + e.getMessage());
            }
        });
        try { Thread.sleep(7000); } catch (InterruptedException e) { e.printStackTrace(); } // Keep main alive for sim
    }
}
