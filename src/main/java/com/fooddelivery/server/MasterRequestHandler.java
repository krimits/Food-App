package com.fooddelivery.server;

import com.fooddelivery.communication.MessageType;
import com.fooddelivery.model.Store;
import com.fooddelivery.util.JsonUtil; // For creating response JSON
import com.fooddelivery.util.StoreJsonParser; // For parsing store JSON

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class MasterRequestHandler implements Runnable {
    private Socket masterSocket;
    private Worker worker;
    private PrintWriter out;
    private BufferedReader in;

    public MasterRequestHandler(Socket socket, Worker worker) {
        this.masterSocket = socket;
        this.worker = worker;
        try {
            this.out = new PrintWriter(masterSocket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(masterSocket.getInputStream()));
        } catch (IOException e) {
            System.err.println("MasterRequestHandler: Error getting streams: " + e.getMessage());
            closeConnection();
        }
    }

    @Override
    public void run() {
        try {
            masterSocket.setSoTimeout(30000); // 30 seconds timeout

            String messageTypeStr = in.readLine();
             if (messageTypeStr == null) {
                System.out.println("Worker: Master disconnected before sending message type from " + masterSocket.getRemoteSocketAddress());
                return;
            }

            MessageType type;
            try {
                type = MessageType.valueOf(messageTypeStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("Worker: Received invalid message type from Master: " + messageTypeStr);
                // Cannot easily send JSON response if type parsing fails before payload read.
                // Master will likely timeout or get connection reset.
                return;
            }

            String payload = in.readLine();
            if (payload == null) {
                System.out.println("Worker: Master disconnected before sending payload from " + masterSocket.getRemoteSocketAddress());
                return;
            }
            
            System.out.println("Worker (" + worker.getPort() + "): Received message type " + type + " from Master.");

            switch (type) {
                case ADD_STORE_REQUEST:
                    handleAddStoreRequest(payload);
                    break;
                // Handle other message types from Master here
                default:
                    System.err.println("Worker (" + worker.getPort() + "): Unsupported message type from Master: " + type);
                    // Send generic error back to Master
                    out.println(JsonUtil.createStatusResponseJson(null, "FAILURE", "Unsupported message type by worker."));
            }
        } catch (SocketTimeoutException e) {
            System.err.println("Worker ("+worker.getPort()+") MasterRequestHandler: Socket read timeout: " + e.getMessage());
            // out might be null or masterSocket closed
        } catch (IOException e) {
             if (!masterSocket.isClosed()) {
                System.err.println("Worker ("+worker.getPort()+") MasterRequestHandler: IOException: " + e.getMessage());
            } else {
                System.out.println("Worker ("+worker.getPort()+") MasterRequestHandler: Connection closed by master or network issue.");
            }
        } catch (Exception e) { // Catch any other unexpected errors
            System.err.println("Worker ("+worker.getPort()+") MasterRequestHandler: Unexpected error: " + e.getMessage());
            e.printStackTrace();
            if (out != null && !masterSocket.isClosed()){
                 out.println(JsonUtil.createStatusResponseJson(null, "FAILURE", "Unexpected worker error."));
            }
        }
        finally {
            closeConnection();
        }
    }

    private void handleAddStoreRequest(String storeJsonPayload) {
        String responseJson;
        String storeNameForResponse = null;
        try {
            // Attempt to parse store name for better error messages even if full parsing fails
            storeNameForResponse = JsonUtil.extractStoreName(storeJsonPayload); // JsonUtil can extract simple names
            if(storeNameForResponse == null) storeNameForResponse = "UnknownStore";

            Store store = StoreJsonParser.parseStoreJson(storeJsonPayload);
            storeNameForResponse = store.getStoreName(); // Get definite name after successful parse
            
            worker.addStore(store); // This also calculates price category

            System.out.println("Worker (" + worker.getPort() + "): Successfully added store - " + store.getStoreName());
            responseJson = JsonUtil.createStatusResponseJson(store.getStoreName(), "SUCCESS", "Store added successfully by worker " + worker.getPort());
        } catch (StoreJsonParser.JsonParseException e) {
            System.err.println("Worker (" + worker.getPort() + "): Failed to parse store JSON: " + e.getMessage());
            responseJson = JsonUtil.createStatusResponseJson(storeNameForResponse, "FAILURE", "Worker failed to parse store JSON: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Worker (" + worker.getPort() + "): Error processing add store request: " + e.getMessage());
            e.printStackTrace();
            responseJson = JsonUtil.createStatusResponseJson(storeNameForResponse, "FAILURE", "Worker error processing add store request: " + e.getMessage());
        }
        out.println(responseJson);
    }

    private void closeConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (masterSocket != null && !masterSocket.isClosed()) masterSocket.close();
             System.out.println("Worker ("+worker.getPort()+") MasterRequestHandler: Connection closed with Master " + (masterSocket != null ? masterSocket.getRemoteSocketAddress() : ""));
        } catch (IOException e) {
            System.err.println("Worker ("+worker.getPort()+") MasterRequestHandler: Error closing resources: " + e.getMessage());
        }
    }
}
