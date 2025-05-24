package com.fooddelivery.server;

import com.fooddelivery.model.Store; // Keep this

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
    
    public int getPort() { // Added getter for port, useful for logging in handler
        return port;
    }

    public void startServer() {
        isRunning = true;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Worker server started on port " + port);

            while (isRunning) {
                try {
                    Socket masterConnSocket = serverSocket.accept(); // Connection from Master
                    System.out.println("Worker (" + port + "): Connection from Master: " + masterConnSocket.getRemoteSocketAddress());
                    masterRequestExecutorService.submit(new MasterRequestHandler(masterConnSocket, this)); // Use MasterRequestHandler
                } catch (IOException e) {
                    if (!isRunning) {
                        System.out.println("Worker server stopping.");
                        break;
                    }
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
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Worker (" + port + "): Error closing worker server socket: " + e.getMessage());
        }
        masterRequestExecutorService.shutdown();
        try {
            if(!masterRequestExecutorService.awaitTermination(5, TimeUnit.SECONDS)){
                masterRequestExecutorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            masterRequestExecutorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("Worker server (" + port + ") shut down.");
    }

    // addStore, getStore, getAllStores remain the same
    public synchronized void addStore(Store store) {
        if (store != null && store.getStoreName() != null) {
            localStores.put(store.getStoreName(), store);
            store.calculateAndSetPriceCategory(); // Ensure price category is calculated
            System.out.println("Worker (" + port + "): Added store - " + store.getStoreName() + 
                               ", Price Category: " + store.getPriceCategory() + 
                               ", Products: " + store.getProducts().size());
        }
    }

    public Store getStore(String storeName) {
        return localStores.get(storeName);
    }
    
    public Map<String, Store> getAllStores() {
        // Return a defensive copy if this map is to be iterated elsewhere while modifications can happen
        synchronized (localStores) {
            return new HashMap<>(localStores);
        }
    }

    public static void main(String[] args) { // main remains the same
        if (args.length < 1) {
            System.err.println("Usage: java com.fooddelivery.server.Worker <port>");
            System.exit(1);
        }
        try {
            int workerPort = Integer.parseInt(args[0]);
            Worker worker = new Worker(workerPort);
            Runtime.getRuntime().addShutdownHook(new Thread(worker::stopServer));
            worker.startServer();
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number: " + args[0]);
            System.exit(1);
        }
    }
}
