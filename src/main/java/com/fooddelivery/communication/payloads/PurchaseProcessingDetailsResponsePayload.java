package com.fooddelivery.communication.payloads;

import java.io.Serializable;
import java.util.Map;

// Sent by the Primary Worker to Master after successfully processing a purchase.
// Contains details needed by Master for replication and for final client response.
public class PurchaseProcessingDetailsResponsePayload implements Serializable {
    private static final long serialVersionUID = 1L;
    private String storeName;
    private String status; // SUCCESS or FAILURE
    private String message; // Overall message
    private Map<String, Integer> updatedProductStocks; // productName -> new stock level
    private SalesDataEntry saleRecorded; // Details of the sale that was recorded

    public PurchaseProcessingDetailsResponsePayload(String storeName, String status, String message, 
                                                    Map<String, Integer> updatedProductStocks, SalesDataEntry saleRecorded) {
        this.storeName = storeName;
        this.status = status;
        this.message = message;
        this.updatedProductStocks = updatedProductStocks;
        this.saleRecorded = saleRecorded;
    }

    // Getters
    public String getStoreName() { return storeName; }
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public Map<String, Integer> getUpdatedProductStocks() { return updatedProductStocks; }
    public SalesDataEntry getSaleRecorded() { return saleRecorded; }
}
