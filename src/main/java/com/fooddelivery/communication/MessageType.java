package com.fooddelivery.communication;

public enum MessageType {
    // Manager to Master / Master to Worker
    ADD_STORE_REQUEST,
    ADD_STORE_RESPONSE,

    // Product Management (Manager via Master to Worker)
    ADD_PRODUCT_REQUEST, // To add a new product to a store
    ADD_PRODUCT_RESPONSE,
    REMOVE_PRODUCT_REQUEST, // To mark a product as unavailable (soft delete)
    REMOVE_PRODUCT_RESPONSE,
    UPDATE_STOCK_REQUEST, // To change available amount
    UPDATE_STOCK_RESPONSE,

    // Client to Master
    SEARCH_STORES_REQUEST,
    SEARCH_STORES_RESPONSE,
    PURCHASE_REQUEST,
    PURCHASE_RESPONSE,
    RATE_STORE_REQUEST,
    RATE_STORE_RESPONSE,

    // Manager to Master (for queries)
    GET_SALES_BY_PRODUCT_REQUEST,
    GET_SALES_BY_PRODUCT_RESPONSE,
    GET_SALES_BY_STORE_TYPE_REQUEST,
    GET_SALES_BY_STORE_TYPE_RESPONSE,
    GET_SALES_BY_PRODUCT_CATEGORY_REQUEST,
    GET_SALES_BY_PRODUCT_CATEGORY_RESPONSE,
    
    // Generic/Error Responses
    ERROR_RESPONSE,
    SUCCESS_RESPONSE, // For simple acknowledgements

    // Master to Worker (MapReduce related - to be detailed later)
    MAP_TASK_REQUEST,
    MAP_TASK_RESPONSE,
    REDUCE_TASK_REQUEST, // If needed directly, or master does reduce
    REDUCE_TASK_RESPONSE, // If needed

    // ... (existing types)
    WORKER_MAP_SALES_PRODUCT_CATEGORY_TASK_REQUEST, // Master to Worker
    WORKER_MAP_SALES_PRODUCT_CATEGORY_TASK_RESPONSE, // Worker to Master (though a generic MapTaskResponse might be better)
    WORKER_MAP_SALES_STORE_TYPE_TASK_REQUEST,     // For later use
    WORKER_MAP_SALES_STORE_TYPE_TASK_RESPONSE,      // For later use

    // For Master broadcasting deltas to Workers for replication
    REPLICATE_STATE_DELTA_REQUEST, 
    // Response from Worker to Master after primary processing a purchase/rating, carrying details for replication
    PRIMARY_PURCHASE_RESPONSE_DETAILS, // Worker to Master
    PRIMARY_RATING_RESPONSE_DETAILS    // Worker to Master
}
