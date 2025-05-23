# Codebase Index

This document provides a summary of the Java classes in the Food-App project.

## `Actions.java`
- **Purpose**: Handles incoming requests and delegates tasks to worker nodes or reducers. It acts as a central coordinator or master.
- **Functionality**:
    - Manages store information by distributing data to workers.
    - Processes client requests for finding stores, filtering stores by various criteria (location, category, rating, price).
    - Handles product-related operations like finding products, updating inventory (incrementing/decrementing quantity), adding new products, and removing products.
    - Facilitates client purchases by interacting with workers.
    - Allows clients to rate stores.
    - Aggregates data from workers for queries like sales by store type or product category, often involving a reducer.

## `Client.java`
- **Purpose**: Provides a command-line user interface (CLI) for customers to interact with the Food-App.
- **Functionality**:
    - Allows users to find stores near their location (based on latitude and longitude).
    - Enables users to filter stores based on criteria such as food categories, minimum star rating, and price range.
    - Facilitates product purchases from selected stores.
    - Allows users to rate stores (1-5 stars).
    - Communicates with a master/server node to send requests and receive responses.

## `Manager.java`
- **Purpose**: Provides a command-line interface (CLI) for administrative tasks related to managing stores and products.
- **Functionality**:
    - Adds new stores to the system by parsing data from a JSON file (using the inner `StoreParser` class).
    - Adds new products to existing stores or updates the quantity of existing products.
    - Removes products from stores or marks them as hidden.
    - Retrieves and displays total sales aggregated by store type (e.g., "pizzeria").
    - Retrieves and displays total sales aggregated by product category (e.g., "pizza").
    - Communicates with a master/server node to effect these changes and queries.
- **Inner Class `StoreParser`**:
    - Parses store and product data from a JSON file format.

## `PurchaseResponse.java`
- **Purpose**: A simple data class (POJO) used to structure the response sent back after a purchase attempt.
- **Functionality**:
    - Holds a `status` (e.g., "success", "failure") and a `message` (providing details about the purchase outcome).

## `WorkerActions.java`
- **Purpose**: Represents a worker node in the distributed system. It manages a subset of store data and performs operations on this local data based on requests from a master/coordinator.
- **Functionality**:
    - Stores and manages a list of `Store` objects assigned to it.
    - Handles requests from the master to:
        - Add a new store to its local list.
        - Find a specific store or product within its managed stores.
        - Update product quantities (increment, decrement, mark as hidden/removed).
        - Add new products to a store.
        - Filter its local stores based on criteria (location, category, rating, price) for client requests.
        - Process parts of a client's purchase order relevant to its stores.
        - Update store ratings.
        - Provide data for aggregated queries (e.g., sales by store type/product category for its stores).
    - Uses `synchronized` blocks to ensure thread-safe access to its `stores` data, allowing concurrent request processing.
