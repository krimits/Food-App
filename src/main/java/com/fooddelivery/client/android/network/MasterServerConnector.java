package com.fooddelivery.client.android.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
// If this were actual Android, would use android.os.Handler for UI thread callbacks.
// For this simulation, callbacks will be direct.

public class MasterServerConnector {

    private static MasterServerConnector instance;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private ExecutorService executorService; // For background network operations

    private String currentHost;
    private int currentPort;
    private boolean isConnected = false;

    public interface ResponseCallback {
        void onSuccess(String response); // Called on the thread that invoked it (simulation)
        void onError(Exception e);     // Called on the thread that invoked it (simulation)
    }

    private MasterServerConnector() {
        // Using a single thread executor for sequential message processing per connection.
        // If parallel messages over one socket were needed (not typical for this req/res model),
        // a different strategy or multiple connections would be required.
        executorService = Executors.newSingleThreadExecutor(); 
    }

    public static synchronized MasterServerConnector getInstance() {
        if (instance == null) {
            instance = new MasterServerConnector();
        }
        return instance;
    }

    public void connect(String host, int port, ResponseCallback callback) {
        executorService.submit(() -> {
            try {
                if (isConnected && socket != null && !socket.isClosed()) {
                    if (this.currentHost.equals(host) && this.currentPort == port) {
                        System.out.println("MasterServerConnector: Already connected to " + host + ":" + port);
                        if (callback != null) callback.onSuccess("Already connected.");
                        return;
                    } else {
                        disconnectInternal(); // Disconnect from previous if different target
                    }
                }
                System.out.println("MasterServerConnector: Connecting to " + host + ":" + port + "...");
                socket = new Socket(host, port);
                socket.setSoTimeout(20000); // 20 second timeout for read operations on the socket
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.currentHost = host;
                this.currentPort = port;
                isConnected = true;
                System.out.println("MasterServerConnector: Connected successfully.");
                if (callback != null) callback.onSuccess("Connected successfully to " + host + ":" + port);
            } catch (UnknownHostException e) {
                isConnected = false;
                System.err.println("MasterServerConnector: Connection Error - Unknown host " + host);
                if (callback != null) callback.onError(e);
            } catch (IOException e) {
                isConnected = false;
                System.err.println("MasterServerConnector: Connection Error - " + e.getMessage());
                if (callback != null) callback.onError(e);
            }
        });
    }

    public boolean isConnected() {
        return isConnected && socket != null && !socket.isClosed();
    }

    public void sendMessage(final String firstLine, final String jsonPayload, final ResponseCallback callback) {
        if (!isConnected()) {
            System.err.println("MasterServerConnector: Not connected. Call connect() first.");
            if (callback != null) callback.onError(new IOException("Not connected to server."));
            return;
        }

        executorService.submit(() -> {
            try {
                System.out.println("MasterServerConnector: Sending -> First Line: " + firstLine);
                System.out.println("MasterServerConnector: Sending -> Payload: " + jsonPayload);
                out.println(firstLine);
                out.println(jsonPayload);

                // Read response (assuming single line response from Master for now)
                String response = in.readLine(); 
                if (response != null) {
                    System.out.println("MasterServerConnector: Received <- " + response);
                    if (callback != null) {
                        // In real Android, post this to the UI thread
                        callback.onSuccess(response);
                    }
                } else {
                    System.err.println("MasterServerConnector: Received null response from server.");
                    if (callback != null) {
                        // In real Android, post this to the UI thread
                        callback.onError(new IOException("Received null response from server."));
                    }
                     // Consider this a connection issue, might need to disconnect or re-validate
                    disconnectInternal(); // Or a less drastic error handling
                }
            } catch (IOException e) {
                System.err.println("MasterServerConnector: Error sending/receiving message: " + e.getMessage());
                if (callback != null) {
                    // In real Android, post this to the UI thread
                    callback.onError(e);
                }
                // If an IOException occurs, the connection might be broken.
                disconnectInternal(); 
            }
        });
    }

    public void disconnect() {
        executorService.submit(this::disconnectInternalAndShutdownExecutor);
    }
    
    private void disconnectInternal() {
        if (isConnected) {
            System.out.println("MasterServerConnector: Disconnecting...");
        }
        isConnected = false; // Set first to prevent new messages trying to use closing resources
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("MasterServerConnector: Error while disconnecting: " + e.getMessage());
        } finally {
            out = null;
            in = null;
            socket = null;
            if (isConnected) { // Only print if it was connected before this attempt
                 System.out.println("MasterServerConnector: Disconnected.");
            }
        }
    }
    
    private void disconnectInternalAndShutdownExecutor() {
        disconnectInternal();
        System.out.println("MasterServerConnector: Shutting down executor service.");
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
         System.out.println("MasterServerConnector: Executor service shut down.");
         instance = null; // Allow re-creation if needed after full shutdown
    }
}
