package com.fooddelivery.server;

import com.fooddelivery.communication.Message;
import com.fooddelivery.communication.MessageType;
import com.fooddelivery.util.JsonUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class Master {
    private int port;
    private List<WorkerInfo> workerNodes;
    private ServerSocket serverSocket;
    private boolean isRunning;
    private ExecutorService clientExecutorService;

    static class WorkerInfo { 
        String id; String host; int port;
        public WorkerInfo(String id, String host, int port) { this.id = id; this.host = host; this.port = port; }
        public String getId() { return id; } public String getHost() { return host; } public int getPort() { return port; }
        @Override public String toString() { return "WorkerInfo{id='" + id + "', host='" + host + "', port=" + port + '}';}
    }

    public Master(int port, String[] workerArgs) {
        this.port = port;
        this.workerNodes = Collections.synchronizedList(new ArrayList<>());
        this.isRunning = false;
        this.clientExecutorService = Executors.newCachedThreadPool();

        for (String workerArg : workerArgs) {
            try {
                String[] parts = workerArg.split(":");
                String host = parts[0];
                int workerPort = Integer.parseInt(parts[1]);
                String workerId = host + ":" + workerPort;
                workerNodes.add(new WorkerInfo(workerId, host, workerPort));
                System.out.println("Registered worker: " + workerId);
            } catch (Exception e) {
                System.err.println("Invalid worker argument format: " + workerArg + ". Expected host:port. Error: " + e.getMessage());
            }
        }
        if (workerNodes.isEmpty()) {
            System.out.println("Warning: No worker nodes registered.");
        }
    }
    
    public void startServer() {
        isRunning = true;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Master server started on port " + port);

            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New connection from: " + clientSocket.getRemoteSocketAddress());
                    clientExecutorService.submit(new ClientHandler(clientSocket, this)); 
                } catch (IOException e) {
                    if (!isRunning) {
                        System.out.println("Master server stopping.");
                        break;
                    }
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Could not start master server on port " + port + ": " + e.getMessage());
        } finally {
            stopServer();
        }
    }
    
    public void stopServer() {
        System.out.println("Master: Shutting down server...");
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing master server socket: " + e.getMessage());
        }
        clientExecutorService.shutdown();
        try {
            if (!clientExecutorService.awaitTermination(5, TimeUnit.SECONDS)) {
                clientExecutorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            clientExecutorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("Master server shut down.");
    }

    public List<WorkerInfo> getWorkerNodes() {
        return new ArrayList<>(workerNodes); 
    }

    public void handleAddStoreRequest(Message clientMessage, PrintWriter clientOut) {
        String storeJsonPayload = clientMessage.getPayload();
        // For ADD_STORE_REQUEST, StoreName is extracted from the JSON payload itself.
        String storeName = JsonUtil.extractStoreName(storeJsonPayload); // Assumes "StoreName" key

        if (storeName == null || storeName.trim().isEmpty()) {
            System.err.println("Master: Could not extract StoreName from ADD_STORE_REQUEST payload.");
            clientOut.println(JsonUtil.createStatusResponseJson(null, "FAILURE", "Could not extract StoreName from payload."));
            return;
        }
        
        if (workerNodes.isEmpty()) {
            System.err.println("Master: No workers available for store: " + storeName);
            clientOut.println(JsonUtil.createStatusResponseJson(storeName, "FAILURE", "No workers available."));
            return;
        }

        int workerIndex = Math.abs(storeName.hashCode()) % workerNodes.size();
        WorkerInfo selectedWorker = workerNodes.get(workerIndex);
        System.out.println("Master: Selected worker " + selectedWorker.getId() + " for store " + storeName);

        // For ADD_STORE_REQUEST, the first line to worker is just the MessageType
        forwardRequestToWorker(selectedWorker, clientMessage.getType().name(), storeJsonPayload, clientOut, storeName);
    }

    // New generic method for product management requests
    public void handleProductManagementRequest(MessageType requestType, String storeName, String jsonPayload, PrintWriter clientOut) {
        if (storeName == null || storeName.trim().isEmpty()) {
            System.err.println("Master: StoreName is missing for " + requestType);
            clientOut.println(JsonUtil.createStatusResponseJson(null, "FAILURE", "StoreName is required for " + requestType));
            return;
        }
        
        if (workerNodes.isEmpty()) {
            System.err.println("Master: No workers available for store: " + storeName);
            clientOut.println(JsonUtil.createStatusResponseJson(storeName, "FAILURE", "No workers available for product management."));
            return;
        }

        // Select worker using storeName hash
        int workerIndex = Math.abs(storeName.hashCode()) % workerNodes.size();
        WorkerInfo selectedWorker = workerNodes.get(workerIndex);
        System.out.println("Master: Selected worker " + selectedWorker.getId() + " for " + requestType + " for store " + storeName);

        // For product management, first line includes storeName: TYPE:StoreName
        String firstLineToWorker = requestType.name() + ":" + storeName;
        forwardRequestToWorker(selectedWorker, firstLineToWorker, jsonPayload, clientOut, storeName);
    }
    
    public void handleGetSalesByProductRequest(MessageType requestType, String storeName, String jsonPayload, PrintWriter clientOut) {
        if (storeName == null || storeName.trim().isEmpty()) {
            clientOut.println(JsonUtil.createStatusResponseJson(null, "FAILURE", "StoreName is required for " + requestType));
            return;
        }
        if (workerNodes.isEmpty()) {
            clientOut.println(JsonUtil.createStatusResponseJson(storeName, "FAILURE", "No workers available."));
            return;
        }

        int workerIndex = Math.abs(storeName.hashCode()) % workerNodes.size();
        WorkerInfo selectedWorker = workerNodes.get(workerIndex);
        System.out.println("Master: Selected worker " + selectedWorker.getId() + " for " + requestType + " for store " + storeName);

        // For sales requests, first line also includes storeName: TYPE:StoreName
        String firstLineToWorker = requestType.name() + ":" + storeName;
        forwardRequestToWorker(selectedWorker, firstLineToWorker, jsonPayload, clientOut, storeName);
    }
    
    // Helper method to forward request to worker and relay response
    // Takes the full firstLine string to send to worker
    private void forwardRequestToWorker(WorkerInfo worker, String firstLineToWorker, String payload, PrintWriter clientOut, String storeNameForError) {
        try (Socket workerSocket = new Socket(worker.getHost(), worker.getPort());
             PrintWriter workerOut = new PrintWriter(workerSocket.getOutputStream(), true);
             BufferedReader workerIn = new BufferedReader(new InputStreamReader(workerSocket.getInputStream()))) {
            
            workerSocket.setSoTimeout(10000); 

            workerOut.println(firstLineToWorker); // e.g., "GET_SALES_BY_PRODUCT_REQUEST:MyStore" or "ADD_PRODUCT_REQUEST:MyStore" or "ADD_STORE_REQUEST"
            workerOut.println(payload);     
            System.out.println("Master: Sent to worker " + worker.getId() + " -> First Line: " + firstLineToWorker + ", Payload Length: " + (payload != null ? payload.length() : 0));

            String workerResponseJson = workerIn.readLine();
            if (workerResponseJson != null) {
                System.out.println("Master: Received response from worker " + worker.getId() + ": (length " + workerResponseJson.length() + ")");
                clientOut.println(workerResponseJson); 
            } else {
                System.err.println("Master: No response from worker " + worker.getId() + " for request on store " + (storeNameForError != null ? storeNameForError : ""));
                clientOut.println(JsonUtil.createStatusResponseJson(storeNameForError, "FAILURE", "No response from worker."));
            }

        } catch (UnknownHostException e) {
            System.err.println("Master: Worker host not found " + worker.getHost() + ": " + e.getMessage());
            clientOut.println(JsonUtil.createStatusResponseJson(storeNameForError, "FAILURE", "Worker host not found."));
        } catch (IOException e) {
            System.err.println("Master: IOException communicating with worker " + worker.getId() + ": " + e.getMessage());
            clientOut.println(JsonUtil.createStatusResponseJson(storeNameForError, "FAILURE", "Error communicating with worker."));
        } catch (Exception e) {
            System.err.println("Master: Unexpected error during worker communication for " + (storeNameForError != null ? storeNameForError : "") + ": " + e.getMessage());
            clientOut.println(JsonUtil.createStatusResponseJson(storeNameForError, "FAILURE", "Unexpected error processing request with worker."));
        }
    }
    
     public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java com.fooddelivery.server.Master <port> [worker1_host:port] ...");
            System.exit(1);
        }
        try {
            int masterPort = Integer.parseInt(args[0]);
            String[] workerArgs = new String[args.length - 1];
            System.arraycopy(args, 1, workerArgs, 0, args.length - 1);
            Master master = new Master(masterPort, workerArgs);
            Runtime.getRuntime().addShutdownHook(new Thread(master::stopServer));
            master.startServer();
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number provided.");
            System.exit(1);
        }
    }
}
