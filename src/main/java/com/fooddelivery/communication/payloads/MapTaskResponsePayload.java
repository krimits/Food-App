package com.fooddelivery.communication.payloads;

import java.io.Serializable;
import java.util.List;

public class MapTaskResponsePayload implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<SalesDataEntry> mappedResults; // Worker sends its aggregated results for the task

    public MapTaskResponsePayload(List<SalesDataEntry> mappedResults) {
        this.mappedResults = mappedResults;
    }

    public List<SalesDataEntry> getMappedResults() { return mappedResults; }
}
