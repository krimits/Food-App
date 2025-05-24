package com.fooddelivery.communication.payloads;

import java.io.Serializable;

public class MapTaskRequestPayload implements Serializable {
    private static final long serialVersionUID = 1L;
    private String taskTypeIdentifier; // e.g., "PRODUCT_CATEGORY_SALES", "STORE_TYPE_SALES"
    private String targetCriteria;     // e.g., "salad" (productType), "pizzeria" (foodCategory)

    public MapTaskRequestPayload(String taskTypeIdentifier, String targetCriteria) {
        this.taskTypeIdentifier = taskTypeIdentifier;
        this.targetCriteria = targetCriteria;
    }

    public String getTaskTypeIdentifier() { return taskTypeIdentifier; }
    public String getTargetCriteria() { return targetCriteria; }
}
