package com.fooddelivery.communication.payloads;

import java.io.Serializable;
import java.util.List;

public class SearchStoresResponsePayload implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<StoreInfoForClient> results;

    public SearchStoresResponsePayload(List<StoreInfoForClient> results) {
        this.results = results;
    }

    // Getter
    public List<StoreInfoForClient> getResults() { return results; }
    // Setter
    public void setResults(List<StoreInfoForClient> results) { this.results = results; }
}
