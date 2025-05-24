package com.fooddelivery.communication.payloads;

import java.io.Serializable;
import java.util.List;

public class PurchaseRequestPayload implements Serializable {
    private static final long serialVersionUID = 1L;
    // storeName will be sent as part of the routing key (e.g., "PURCHASE_REQUEST:MyStore")
    // It can also be included here for validation/consistency if desired.
    private String storeName; 
    private List<OrderItemPayload> items;
    // Could add other fields like deliveryAddress, userToken etc. in a fuller app

    public PurchaseRequestPayload(String storeName, List<OrderItemPayload> items) {
        this.storeName = storeName;
        this.items = items;
    }

    // Getters
    public String getStoreName() { return storeName; }
    public List<OrderItemPayload> getItems() { return items; }
    // Setters
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public void setItems(List<OrderItemPayload> items) { this.items = items; }
}
