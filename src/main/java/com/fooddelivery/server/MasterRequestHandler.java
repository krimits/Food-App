package com.fooddelivery.server;

import com.fooddelivery.communication.MessageType;
import com.fooddelivery.communication.payloads.*; // Import all payloads
import com.fooddelivery.model.Product; // For creating Product object for addProductToStore
import com.fooddelivery.model.Store;   // For type casting if needed, though not directly here
import com.fooddelivery.util.JsonUtil;
import com.fooddelivery.util.StoreJsonParser; // For parsing payloads

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
            System.err.println("Worker ("+worker.getPort()+") MasterRequestHandler: Error getting streams: " + e.getMessage()); 
            closeConnection(); 
        }
    }


    @Override
    public void run() {
        String responseJson = null;
        String requestStoreName = null; // For error messages if payload parsing fails early
        MessageType type = null; // To hold the parsed message type

        try {
            masterSocket.setSoTimeout(30000); 

            String firstLine = in.readLine(); 
            if (firstLine == null) { 
                System.out.println("Worker ("+worker.getPort()+"): Master disconnected before sending any data from " + masterSocket.getRemoteSocketAddress());
                return; 
            }

            String[] parts = firstLine.trim().split(":", 2);
            try {
                type = MessageType.valueOf(parts[0].toUpperCase());
                if (parts.length > 1) {
                    requestStoreName = parts[1]; 
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Worker ("+worker.getPort()+"): Invalid message type from Master: " + firstLine + ". Error: " + e.getMessage());
                // We can't send a response if we don't know the protocol / payload structure. Master should timeout.
                return;
            }
            
            String payload = in.readLine();
            if (payload == null) { 
                System.out.println("Worker ("+worker.getPort()+"): Master disconnected before sending payload for type " + type + " from " + masterSocket.getRemoteSocketAddress());
                return; 
            }
            
            System.out.println("Worker (" + worker.getPort() + "): Received " + type + 
                               (requestStoreName != null ? " for store " + requestStoreName : "") + 
                               " from Master.");

            switch (type) {
                case ADD_STORE_REQUEST:
                    try {
                        Store store = StoreJsonParser.parseStoreJson(payload);
                        requestStoreName = store.getStoreName(); 
                        worker.addStore(store);
                        responseJson = JsonUtil.createStatusResponseJson(store.getStoreName(), "SUCCESS", "Store added by worker " + worker.getPort());
                    } catch (StoreJsonParser.JsonParseException e) {
                        System.err.println("Worker (" + worker.getPort() + "): Failed to parse store JSON: " + e.getMessage());
                        responseJson = JsonUtil.createStatusResponseJson(requestStoreName, "FAILURE", "Worker: StoreJsonParseException: " + e.getMessage());
                    }
                    break;
                
                case ADD_PRODUCT_REQUEST:
                    if (requestStoreName == null || requestStoreName.trim().isEmpty()) {
                        responseJson = JsonUtil.createStatusResponseJson(null, "FAILURE", "Store name (routing key) missing for ADD_PRODUCT_REQUEST.");
                        break;
                    }
                    try {
                        AddProductRequestPayload addProductPayload = StoreJsonParser.parseAddProductRequestPayload(payload);
                        Product product = new Product(
                            addProductPayload.getProductName(),
                            addProductPayload.getProductType(),
                            addProductPayload.getInitialAvailableAmount(),
                            addProductPayload.getPrice()
                        );
                        // The storeName (requestStoreName) is passed to the worker method.
                        responseJson = worker.addProductToStore(requestStoreName, product);
                    } catch (StoreJsonParser.JsonParseException e) {
                        responseJson = JsonUtil.createStatusResponseJson(requestStoreName, "FAILURE", "Worker: AddProductRequestParseException: " + e.getMessage());
                    }
                    break;

                case REMOVE_PRODUCT_REQUEST:
                     if (requestStoreName == null || requestStoreName.trim().isEmpty()) {
                        responseJson = JsonUtil.createStatusResponseJson(null, "FAILURE", "Store name (routing key) missing for REMOVE_PRODUCT_REQUEST.");
                        break;
                    }
                    try {
                        RemoveProductRequestPayload removePayload = StoreJsonParser.parseRemoveProductRequestPayload(payload);
                        responseJson = worker.removeProductFromStore(requestStoreName, removePayload.getProductName());
                    } catch (StoreJsonParser.JsonParseException e) {
                        responseJson = JsonUtil.createStatusResponseJson(requestStoreName, "FAILURE", "Worker: RemoveProductRequestParseException: " + e.getMessage());
                    }
                    break;

                case UPDATE_STOCK_REQUEST:
                     if (requestStoreName == null || requestStoreName.trim().isEmpty()) {
                        responseJson = JsonUtil.createStatusResponseJson(null, "FAILURE", "Store name (routing key) missing for UPDATE_STOCK_REQUEST.");
                        break;
                    }
                    try {
                        UpdateStockRequestPayload stockPayload = StoreJsonParser.parseUpdateStockRequestPayload(payload);
                        responseJson = worker.updateProductStock(requestStoreName, stockPayload.getProductName(), stockPayload.getQuantityChange());
                    } catch (StoreJsonParser.JsonParseException e) {
                        responseJson = JsonUtil.createStatusResponseJson(requestStoreName, "FAILURE", "Worker: UpdateStockRequestParseException: " + e.getMessage());
                    }
                    break;

                case GET_SALES_BY_PRODUCT_REQUEST:
                    if (requestStoreName == null) { // requestStoreName is the routingKey from first line
                        responseJson = JsonUtil.createStatusResponseJson(null, "FAILURE", "Store name (routing key) missing for sales request.");
                        break;
                    }
                    // Payload from Master might be empty or contain other filters not yet used by worker.
                    responseJson = worker.getSalesByProductForStore(requestStoreName);
                    break;

                default:
                    System.err.println("Worker (" + worker.getPort() + "): Unsupported message type from Master: " + type);
                    responseJson = JsonUtil.createStatusResponseJson(requestStoreName, "FAILURE", "Unsupported message type '" + type + "' by worker.");
            }
        } catch (SocketTimeoutException e) {
            System.err.println("Worker ("+worker.getPort()+") MasterRequestHandler: Socket read timeout: " + e.getMessage());
            // Response cannot be reliably sent. Master will also timeout.
        } catch (IOException e) {
             if (!masterSocket.isClosed()) {
                 System.err.println("Worker ("+worker.getPort()+") MasterRequestHandler: IOException: " + e.getMessage());
             } else {
                 System.out.println("Worker ("+worker.getPort()+") MasterRequestHandler: Connection closed by Master or network issue before full processing.");
             }
             // Response cannot be reliably sent.
        } catch (Exception e) { 
            System.err.println("Worker ("+worker.getPort()+") MasterRequestHandler: Unexpected error processing request type " + (type != null ? type : "UNKNOWN") + ": " + e.getMessage());
            e.printStackTrace();
            if (responseJson == null && out != null && !masterSocket.isClosed()){ 
                 responseJson = JsonUtil.createStatusResponseJson(requestStoreName, "FAILURE", "Unexpected worker error during request processing for " + (type != null ? type : "UNKNOWN") + ".");
            }
        }
        finally {
            if (responseJson != null && out != null && !masterSocket.isClosed()) {
                out.println(responseJson);
            } else if (responseJson == null && out != null && !masterSocket.isClosed()) {
                // This means an error happened before responseJson could be set by the switch, or an IO/Socket error.
                out.println(JsonUtil.createStatusResponseJson(requestStoreName, "FAILURE", "Worker error occurred processing " + (type != null ? type : "UNKNOWN") + ". Check worker logs."));
            }
            // If out is null or socket is closed, can't send anything.
            closeConnection();
        }
    }

    private void closeConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (masterSocket != null && !masterSocket.isClosed()) masterSocket.close();
        } catch (IOException e) { 
            // System.err.println("Worker ("+worker.getPort()+") MasterRequestHandler: Exception during closeConnection: " + e.getMessage());
        }
    }
}
