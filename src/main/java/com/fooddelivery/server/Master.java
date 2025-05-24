package com.fooddelivery.server;

package com.fooddelivery.server;

import com.fooddelivery.communication.Message;
import com.fooddelivery.communication.MessageType;
import com.fooddelivery.util.JsonUtil; // Using our basic JsonUtil

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
    // Removed workerCommunicationExecutorService, simple worker comms for now

    static class WorkerInfo { // Keep WorkerInfo as is
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
                    clientExecutorService.submit(new ClientHandler(clientSocket, this)); // Use ClientHandler
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
        return new ArrayList<>(workerNodes); // Return a copy
    }

    // Method to handle ADD_STORE_REQUEST
    public void handleAddStoreRequest(Message clientMessage, PrintWriter clientOut) {
        String storeJsonPayload = clientMessage.getPayload();
        String storeName = JsonUtil.extractStoreName(storeJsonPayload);

        if (storeName == null || storeName.trim().isEmpty()) {
            System.err.println("Master: Could not extract StoreName from payload or StoreName is empty.");
            clientOut.println(JsonUtil.createStatusResponseJson(null, "FAILURE", "Could not extract StoreName from payload."));
            return;
        }
        
        if (workerNodes.isEmpty()) {
            System.err.println("Master: No workers available to handle add store request for: " + storeName);
            clientOut.println(JsonUtil.createStatusResponseJson(storeName, "FAILURE", "No workers available."));
            return;
        }

        // Select worker using H(storeName) mod NumberOfNodes
        int workerIndex = Math.abs(storeName.hashCode()) % workerNodes.size();
        WorkerInfo selectedWorker = workerNodes.get(workerIndex);
        System.out.println("Master: Selected worker " + selectedWorker.getId() + " for store " + storeName);

        // Communicate with the selected worker
        try (Socket workerSocket = new Socket(selectedWorker.getHost(), selectedWorker.getPort());
             PrintWriter workerOut = new PrintWriter(workerSocket.getOutputStream(), true);
             BufferedReader workerIn = new BufferedReader(new InputStreamReader(workerSocket.getInputStream()))) {
            
            workerSocket.setSoTimeout(10000); // 10s timeout for worker communication

            // Send ADD_STORE_REQUEST to worker
            // Worker expects: MessageType on first line, JSON payload on second line
            workerOut.println(MessageType.ADD_STORE_REQUEST.name());
            workerOut.println(storeJsonPayload); // Forward the original JSON payload

            System.out.println("Master: Sent ADD_STORE_REQUEST for " + storeName + " to worker " + selectedWorker.getId());

            // Wait for response from worker (expects a single JSON line string for status)
            String workerResponseJson = workerIn.readLine();
            if (workerResponseJson != null) {
                System.out.println("Master: Received response from worker " + selectedWorker.getId() + ": " + workerResponseJson);
                clientOut.println(workerResponseJson); // Forward worker's response (JSON string) to client
            } else {
                System.err.println("Master: No response from worker " + selectedWorker.getId() + " for store " + storeName);
                clientOut.println(JsonUtil.createStatusResponseJson(storeName, "FAILURE", "No response from worker."));
            }

        } catch (UnknownHostException e) {
            System.err.println("Master: Worker host not found " + selectedWorker.getHost() + ": " + e.getMessage());
            clientOut.println(JsonUtil.createStatusResponseJson(storeName, "FAILURE", "Worker host not found."));
        } catch (IOException e) {
            System.err.println("Master: IOException communicating with worker " + selectedWorker.getId() + ": " + e.getMessage());
            clientOut.println(JsonUtil.createStatusResponseJson(storeName, "FAILURE", "Error communicating with worker."));
        } catch (Exception e) {
            System.err.println("Master: Unexpected error during worker communication for " + storeName + ": " + e.getMessage());
            clientOut.println(JsonUtil.createStatusResponseJson(storeName, "FAILURE", "Unexpected error processing request."));
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
