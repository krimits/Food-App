package com.fooddelivery.client.android.repository;

import com.fooddelivery.client.android.network.ClientJsonUtil;
import com.fooddelivery.client.android.network.MasterServerConnector;
import com.fooddelivery.communication.MessageType;
import com.fooddelivery.communication.payloads.*; // Import all relevant payloads

import java.util.List; // For PurchaseRequestPayload items if needed for validation here

public class StoreRepository {

    private static StoreRepository instance;
    private MasterServerConnector connector;

    private StoreRepository() {
        this.connector = MasterServerConnector.getInstance();
        // Ensure connector is initialized/connected elsewhere or add connect logic here if needed.
        // For this example, assume connector is managed (connected) by application lifecycle.
    }

    public static synchronized StoreRepository getInstance() {
        if (instance == null) {
            instance = new StoreRepository();
        }
        return instance;
    }

    public void searchStores(SearchStoresRequestPayload requestPayload, final ResponseCallback<SearchStoresResponsePayload> callback) {
        if (requestPayload == null) {
            if (callback != null) callback.onError("Search request payload cannot be null.");
            return;
        }
        String jsonRequest = ClientJsonUtil.toJson(requestPayload);
        // For SEARCH_STORES_REQUEST, routingKey in firstLine is optional/not strictly used by current Master broadcast logic
        String firstLine = MessageType.SEARCH_STORES_REQUEST.name(); 

        connector.sendMessage(firstLine, jsonRequest, new MasterServerConnector.ResponseCallback() {
            @Override
            public void onSuccess(String responseJson) {
                try {
                    SearchStoresResponsePayload responsePojo = ClientJsonUtil.fromSearchStoresResponseJson(responseJson);
                    if (callback != null) callback.onSuccess(responsePojo);
                } catch (Exception e) { // Catch potential ClientJsonUtil parsing exceptions
                    if (callback != null) callback.onError("Error parsing search response: " + e.getMessage());
                }
            }

            @Override
            public void onError(Exception e) {
                if (callback != null) callback.onError("Search request failed: " + e.getMessage());
            }
        });
    }

    public void purchaseItems(PurchaseRequestPayload requestPayload, final ResponseCallback<StatusResponsePayload> callback) {
        if (requestPayload == null || requestPayload.getStoreName() == null || requestPayload.getStoreName().trim().isEmpty()) {
            if (callback != null) callback.onError("Purchase request or store name cannot be null/empty.");
            return;
        }
        String jsonRequest = ClientJsonUtil.toJson(requestPayload);
        String firstLine = MessageType.PURCHASE_REQUEST.name() + ":" + requestPayload.getStoreName();

        connector.sendMessage(firstLine, jsonRequest, new MasterServerConnector.ResponseCallback() {
            @Override
            public void onSuccess(String responseJson) {
                try {
                    StatusResponsePayload responsePojo = ClientJsonUtil.fromStatusResponseJson(responseJson);
                    if (callback != null) callback.onSuccess(responsePojo);
                } catch (Exception e) {
                    if (callback != null) callback.onError("Error parsing purchase response: " + e.getMessage());
                }
            }

            @Override
            public void onError(Exception e) {
                if (callback != null) callback.onError("Purchase request failed: " + e.getMessage());
            }
        });
    }

    public void rateStore(RateStoreRequestPayload requestPayload, final ResponseCallback<StatusResponsePayload> callback) {
        if (requestPayload == null || requestPayload.getStoreName() == null || requestPayload.getStoreName().trim().isEmpty()) {
            if (callback != null) callback.onError("Rate store request or store name cannot be null/empty.");
            return;
        }
        String jsonRequest = ClientJsonUtil.toJson(requestPayload);
        String firstLine = MessageType.RATE_STORE_REQUEST.name() + ":" + requestPayload.getStoreName();

        connector.sendMessage(firstLine, jsonRequest, new MasterServerConnector.ResponseCallback() {
            @Override
            public void onSuccess(String responseJson) {
                try {
                    StatusResponsePayload responsePojo = ClientJsonUtil.fromStatusResponseJson(responseJson);
                    if (callback != null) callback.onSuccess(responsePojo);
                } catch (Exception e) {
                    if (callback != null) callback.onError("Error parsing rate store response: " + e.getMessage());
                }
            }

            @Override
            public void onError(Exception e) {
                if (callback != null) callback.onError("Rate store request failed: " + e.getMessage());
            }
        });
    }
}
