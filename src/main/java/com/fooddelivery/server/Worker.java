package com.fooddelivery.server;

import com.fooddelivery.model.Product;
import com.fooddelivery.model.Store;
import com.fooddelivery.util.JsonUtil; // For responses

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
// import java.util.List; // Not explicitly needed here if Store handles its product list internally
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Worker {

    private int port;
    private ServerSocket serverSocket;
    private boolean isRunning;
    private final Map<String, Store> localStores;
    private ExecutorService masterRequestExecutorService;


    public Worker(int port) {
        this.port = port;
        this.localStores = Collections.synchronizedMap(new HashMap<>());
        this.isRunning = false;
        this.masterRequestExecutorService = Executors.newCachedThreadPool();
    }
    
    public int getPort() { return port; }

    public synchronized String addProductToStore(String storeName, Product newProductDetails) {
        Store store = localStores.get(storeName);
        if (store == null) {
            return JsonUtil.createStatusResponseJson(storeName, "FAILURE", "Store not found.");
        }
        Product existingProduct = store.findProduct(newProductDetails.getProductName());
        if (existingProduct != null) {
            // Option 1: Update existing product (if allowed by requirements)
            // existingProduct.setPrice(newProductDetails.getPrice());
            // existingProduct.setAvailableAmount(newProductDetails.getAvailableAmount());
            // existingProduct.setProductType(newProductDetails.getProductType());
            // existingProduct.setAvailableForCustomer(true); // Make sure it's available
            // store.calculateAndSetPriceCategory();
            // System.out.println("Worker (" + port + "): Updated product " + newProductDetails.getProductName() + " in store " + storeName);
            // return JsonUtil.createStatusResponseJson(storeName, "SUCCESS", "Product " + newProductDetails.getProductName() + " updated.");
            
            // Option 2: Return failure if product already exists (as per current logic)
             return JsonUtil.createStatusResponseJson(storeName, "FAILURE", "Product '" + newProductDetails.getProductName() + "' already exists in store " + storeName + ".");
        }
        
        // Create the new product using details from payload.
        // The Product object passed (newProductDetails) is already configured by MasterRequestHandler
        store.addProduct(newProductDetails); 
        store.calculateAndSetPriceCategory(); // Recalculate price category
        System.out.println("Worker (" + port + "): Added product " + newProductDetails.getProductName() + " to store " + storeName);
        return JsonUtil.createStatusResponseJson(storeName, "SUCCESS", "Product " + newProductDetails.getProductName() + " added to store.");
    }

    public synchronized String removeProductFromStore(String storeName, String productName) {
        Store store = localStores.get(storeName);
        if (store == null) {
            return JsonUtil.createStatusResponseJson(storeName, "FAILURE", "Store not found.");
        }
        Product product = store.findProduct(productName);
        if (product == null) {
            return JsonUtil.createStatusResponseJson(storeName, "FAILURE", "Product '" + productName + "' not found in store " + storeName + ".");
        }
        
        product.setAvailableForCustomer(false); 
        
        System.out.println("Worker (" + port + "): Marked product " + productName + " as unavailable in store " + storeName);
        return JsonUtil.createStatusResponseJson(storeName, "SUCCESS", "Product " + productName + " marked as unavailable.");
    }

    public synchronized String updateProductStock(String storeName, String productName, int quantityChange) {
        Store store = localStores.get(storeName);
        if (store == null) {
            return JsonUtil.createStatusResponseJson(storeName, "FAILURE", "Store not found.");
        }
        Product product = store.findProduct(productName);
        if (product == null) {
            return JsonUtil.createStatusResponseJson(storeName, "FAILURE", "Product '" + productName + "' not found for stock update in store " + storeName + ".");
        }
        
        int currentStock = product.getAvailableAmount();
        int newStock = currentStock + quantityChange;

        if (newStock < 0) {
            // Decision: either reject, or set to 0. Setting to 0 as per previous example.
            product.setAvailableAmount(0); 
            System.out.println("Worker (" + port + "): Stock for " + productName + " in store " + storeName + " would go negative. Set to 0. Original change: " + quantityChange);
            return JsonUtil.createStatusResponseJson(storeName, "SUCCESS", "Stock for " + productName + " updated. New stock: 0 (original change " + quantityChange + " would lead to negative).");

        } else {
            product.setAvailableAmount(newStock);
            System.out.println("Worker (" + port + "): Updated stock for " + productName + " in store " + storeName + " by " + quantityChange + ". New stock: " + product.getAvailableAmount());
            return JsonUtil.createStatusResponseJson(storeName, "SUCCESS", "Stock for " + productName + " updated. New stock: " + product.getAvailableAmount());
        }
    }
    
    public void startServer() {
        isRunning = true;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Worker server started on port " + port);

            while (isRunning) {
                try {
                    Socket masterConnSocket = serverSocket.accept();
                    // System.out.println("Worker (" + port + "): Connection from Master: " + masterConnSocket.getRemoteSocketAddress()); // Already logged in MasterRequestHandler
                    masterRequestExecutorService.submit(new MasterRequestHandler(masterConnSocket, this)); 
                } catch (IOException e) {
                    if (!isRunning) { System.out.println("Worker server stopping."); break; }
                    System.err.println("Worker (" + port + "): Error accepting Master connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Worker (" + port + "): Could not start worker server on port " + port + ": " + e.getMessage());
        } finally {
            stopServer();
        }
    }

    public void stopServer() {
        System.out.println("Worker (" + port + "): Shutting down server...");
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
        } catch (IOException e) { System.err.println("Worker (" + port + "): Error closing worker server socket: " + e.getMessage()); }
        
        masterRequestExecutorService.shutdown();
        try {
            if(!masterRequestExecutorService.awaitTermination(5, TimeUnit.SECONDS)) {
                masterRequestExecutorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            masterRequestExecutorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("Worker server (" + port + ") shut down.");
    }

    public synchronized void addStore(Store store) { 
        if (store != null && store.getStoreName() != null) {
            localStores.put(store.getStoreName(), store);
            store.calculateAndSetPriceCategory(); 
            System.out.println("Worker (" + port + "): Added store - " + store.getStoreName() + 
                               ", Price Category: " + store.getPriceCategory() + 
                               ", Products: " + (store.getProducts() != null ? store.getProducts().size() : 0));
        }
    }

    public Store getStore(String storeName) { 
        return localStores.get(storeName); 
    }

    public Map<String, Store> getAllStores() { 
        synchronized (localStores) { 
            return new HashMap<>(localStores); 
        } 
    }

    public synchronized String getSalesByProductForStore(String storeName) {
        Store store = localStores.get(storeName);
        if (store == null) {
            return JsonUtil.createStatusResponseJson(storeName, "FAILURE", "Store not found by worker.");
        }

        List<com.fooddelivery.communication.payloads.SalesDataEntry> entries = new ArrayList<>();
        Map<String, Integer> salesQuantities = store.getSalesByProduct(); // productName -> quantity
        
        double storeTotalRevenueFromProducts = 0; // Will be based on current prices

        if (salesQuantities != null && !salesQuantities.isEmpty()) {
            for (Map.Entry<String, Integer> entry : salesQuantities.entrySet()) {
                String productName = entry.getKey();
                int quantitySold = entry.getValue();
                Product product = store.findProduct(productName); 
                
                double revenueForThisProduct = 0;
                if (product != null) {
                    // Calculate revenue for this product based on its current price.
                    // This is a simplification; ideally, price at time of sale would be used.
                    revenueForThisProduct = product.getPrice() * quantitySold;
                } else {
                    // Product might have been removed from store but sales data still exists.
                    // Log this or handle as per requirements. For now, revenue is 0 for this entry.
                     System.out.println("Worker (" + port + "): Product " + productName + " not found in store " + storeName + " for sales report, but sales data exists.");
                }
                entries.add(new com.fooddelivery.communication.payloads.SalesDataEntry(productName, quantitySold, revenueForThisProduct));
                storeTotalRevenueFromProducts += revenueForThisProduct;
            }
        }
        
        // Using store.getTotalRevenue() which is updated by recordSale with actual price at time of sale.
        // This is more accurate than storeTotalRevenueFromProducts if product prices change.
        return JsonUtil.createSalesResponseJson(
            "SALES_BY_PRODUCT_FOR_STORE", 
            storeName, 
            entries, 
            store.getTotalRevenue() 
        );
    }

    public synchronized String handleWorkerSearchStoresRequest(com.fooddelivery.communication.payloads.SearchStoresRequestPayload requestPayload) {
        List<com.fooddelivery.communication.payloads.StoreInfoForClient> matchingStores = new ArrayList<>();
        Map<String, Store> currentLocalStores = getAllStores(); // Gets a copy

        com.fooddelivery.model.Coordinates clientCoords = new com.fooddelivery.model.Coordinates(requestPayload.getClientLatitude(), requestPayload.getClientLongitude());

        for (Store store : currentLocalStores.values()) {
            // Initial assumption: store is available unless specific checks fail
            
            // 1. Distance Filter (Max 5km)
            com.fooddelivery.model.Coordinates storeCoords = new com.fooddelivery.model.Coordinates(store.getLatitude(), store.getLongitude());
            double distance = storeCoords.distanceTo(clientCoords);
            if (distance > 5.0) { 
                // System.out.println("Worker ("+getPort()+"): Store " + store.getStoreName() + " filtered out by distance: " + distance + "km");
                continue; 
            }

            // 2. Food Category Filter
            if (requestPayload.getFoodCategoryFilter() != null && !requestPayload.getFoodCategoryFilter().isEmpty()) {
                if (!store.getFoodCategory().equalsIgnoreCase(requestPayload.getFoodCategoryFilter())) {
                    // System.out.println("Worker ("+getPort()+"): Store " + store.getStoreName() + " filtered out by food category.");
                    continue;
                }
            }

            // 3. Stars Filter
            if (requestPayload.getMinStarsFilter() > 0) { 
                if (store.getStars() < requestPayload.getMinStarsFilter()) {
                    // System.out.println("Worker ("+getPort()+"): Store " + store.getStoreName() + " filtered out by stars.");
                    continue;
                }
            }

            // 4. Price Category Filter
            if (requestPayload.getPriceRangeFilter() != null && !requestPayload.getPriceRangeFilter().isEmpty()) {
                if (store.getPriceCategory() == null || !store.getPriceCategory().equals(requestPayload.getPriceRangeFilter())) {
                    // System.out.println("Worker ("+getPort()+"): Store " + store.getStoreName() + " filtered out by price category.");
                    continue;
                }
            }
            
            // If all filters pass, add to results
            com.fooddelivery.communication.payloads.StoreInfoForClient storeInfo = new com.fooddelivery.communication.payloads.StoreInfoForClient(
                store.getStoreName(),
                store.getFoodCategory(),
                store.getStars(),
                store.getPriceCategory(),
                distance, 
                store.getStoreLogoPath(),
                store.getLatitude(),
                store.getLongitude()
            );
            matchingStores.add(storeInfo);
        }
        System.out.println("Worker ("+getPort()+"): Found " + matchingStores.size() + " stores matching search criteria. Returning to Master.");
        return JsonUtil.createSearchStoresResponseJson(matchingStores);
    }

    public synchronized String handleWorkerRateStoreRequest(String storeName, int stars) {
        Store store = localStores.get(storeName);
        if (store == null) {
            return JsonUtil.createStatusResponseJson(storeName, "FAILURE", "Store not found by worker for rating.");
        }

        if (stars < 1 || stars > 5) {
            // This validation should ideally also be in RateStoreRequestPayload or ClientJsonParser for early client feedback
            return JsonUtil.createStatusResponseJson(storeName, "FAILURE", "Invalid star rating. Must be between 1 and 5.");
        }

        int currentVotes = store.getNoOfVotes();
        // Store.stars is int, representing current average.
        double currentAvgStars = store.getStars(); 
        
        // Calculate new average. (currentAvg * currentVotes + newRating) / (newTotalVotes)
        double newAverageStars = ((currentAvgStars * currentVotes) + stars) / (double)(currentVotes + 1);
        
        store.setStars((int) Math.round(newAverageStars)); // Round to nearest int for storage
        store.setNoOfVotes(currentVotes + 1);

        System.out.println("Worker (" + getPort() + "): Rated store " + storeName + " with " + stars + 
                           " stars. New avg: " + store.getStars() + ", Total votes: " + store.getNoOfVotes());
        return JsonUtil.createStatusResponseJson(storeName, "SUCCESS", "Store rated successfully. New average: " + store.getStars() + ", Total votes: " + store.getNoOfVotes());
    }

    public synchronized List<SalesDataEntry> executeMapSalesByProductCategoryTask(String targetProductType) {
        List<SalesDataEntry> workerResults = new ArrayList<>();
        System.out.println("Worker (" + port + "): Starting MAP_SALES_BY_PRODUCT_CATEGORY_TASK for type: " + targetProductType);

        for (Store store : localStores.values()) {
            double revenueForStoreForProductType = 0;
            Map<String, Integer> salesByProdQty = store.getSalesByProduct(); // productName -> quantitySold

            for (Map.Entry<String, Integer> saleEntry : salesByProdQty.entrySet()) {
                String productName = saleEntry.getKey();
                int quantitySold = saleEntry.getValue();
                Product product = store.findProduct(productName);

                if (product != null && product.getProductType().equalsIgnoreCase(targetProductType)) {
                    // Using current price. For historical accuracy, price at time of sale is needed.
                    revenueForStoreForProductType += product.getPrice() * quantitySold;
                }
            }

            if (revenueForStoreForProductType > 0) {
                // For this task, itemName in SalesDataEntry will be the storeName
                // totalQuantity can be a placeholder (e.g., 0 or 1) as we sum revenue.
                workerResults.add(new SalesDataEntry(store.getStoreName(), 0, revenueForStoreForProductType));
            }
        }
        System.out.println("Worker (" + port + "): Finished MAP_SALES_BY_PRODUCT_CATEGORY_TASK. Emitting " + workerResults.size() + " store entries.");
        return workerResults;
    }

    public synchronized List<SalesDataEntry> executeMapSalesByStoreTypeTask(String targetFoodCategory) {
        List<SalesDataEntry> workerResults = new ArrayList<>();
        System.out.println("Worker (" + port + "): Starting MAP_SALES_BY_STORE_TYPE_TASK for FoodCategory: " + targetFoodCategory);

        for (Store store : localStores.values()) {
            if (store.getFoodCategory().equalsIgnoreCase(targetFoodCategory)) {
                // For this task, itemName in SalesDataEntry will be the storeName.
                // totalQuantity can be a placeholder. totalRevenue is the store's overall total revenue.
                workerResults.add(new SalesDataEntry(store.getStoreName(), 0, store.getTotalRevenue()));
            }
        }
        System.out.println("Worker (" + port + "): Finished MAP_SALES_BY_STORE_TYPE_TASK. Emitting " + workerResults.size() + " store entries for food category '" + targetFoodCategory + "'.");
        return workerResults;
    }

    public static void main(String[] args) {
        if (args.length < 1) { System.err.println("Usage: java com.fooddelivery.server.Worker <port>"); System.exit(1); }
        try {
            int workerPort = Integer.parseInt(args[0]);
            Worker worker = new Worker(workerPort);
            Runtime.getRuntime().addShutdownHook(new Thread(worker::stopServer));
            worker.startServer();
        } catch (NumberFormatException e) { System.err.println("Invalid port number: " + args[0]); System.exit(1); }
    }
}
