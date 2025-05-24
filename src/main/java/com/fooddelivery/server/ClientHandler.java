package com.fooddelivery.server;

import com.fooddelivery.communication.Message;
import com.fooddelivery.communication.MessageType;
import com.fooddelivery.util.JsonUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private Master master;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket socket, Master master) {
        this.clientSocket = socket;
        this.master = master;
        try {
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            System.err.println("ClientHandler: Error getting streams: " + e.getMessage());
            closeConnection();
        }
    }

    @Override
    public void run() {
        try {
            clientSocket.setSoTimeout(30000); // 30 seconds timeout for reads

            String firstLine = in.readLine();
            if (firstLine == null) {
                System.out.println("Client disconnected before sending any data: " + clientSocket.getRemoteSocketAddress());
                return;
            }

            String[] parts = firstLine.trim().split(":", 2);
            MessageType type;
            String routingKey = null; // e.g., storeName for product management

            try {
                type = MessageType.valueOf(parts[0].toUpperCase());
                if (parts.length > 1) {
                    routingKey = parts[1];
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Received invalid message type format from client: " + firstLine);
                out.println(JsonUtil.createStatusResponseJson(null, "FAILURE", "Invalid message type format. Expected TYPE or TYPE:ROUTING_KEY."));
                return;
            }
            
            String payload = in.readLine();
            if (payload == null) {
                 System.out.println("Client disconnected before sending payload: " + clientSocket.getRemoteSocketAddress());
                 return;
            }

            System.out.println("ClientHandler: Received Type: " + type + (routingKey != null ? ", RoutingKey: " + routingKey : ""));
            
            // Pass routingKey to master handlers if needed
            switch (type) {
                case ADD_STORE_REQUEST:
                    // ADD_STORE_REQUEST's payload (store JSON) contains StoreName, so routingKey from first line is not strictly needed here
                    // but JsonUtil.extractStoreName will be used by master.handleAddStoreRequest
                    master.handleAddStoreRequest(new Message(type, payload), out);
                    break;
                case ADD_PRODUCT_REQUEST:
                case REMOVE_PRODUCT_REQUEST:
                case UPDATE_STOCK_REQUEST:
                    if (routingKey == null || routingKey.trim().isEmpty()) {
                        out.println(JsonUtil.createStatusResponseJson(null, "FAILURE", type + " requires a storeName as routing key in the format TYPE:storeName."));
                        return;
                    }
                    master.handleProductManagementRequest(type, routingKey, payload, out);
                    break;
                case GET_SALES_BY_PRODUCT_REQUEST:
                    // Expects routingKey (storeName) to be provided in the first line TYPE:ROUTING_KEY
                    if (routingKey == null || routingKey.trim().isEmpty()) {
                        out.println(JsonUtil.createStatusResponseJson(null, "FAILURE", type + " requires a storeName as routing key."));
                        return;
                    }
                    // Payload for this specific request might be empty or contain other filters not yet used.
                    // For now, Master just needs storeName for routing.
                    master.handleGetSalesByProductRequest(type, routingKey, payload, out);
                    break;
                case SEARCH_STORES_REQUEST:
                    // RoutingKey (e.g. client location string) might be null or not used by Master yet.
                    // Payload contains client's lat/lon and filters.
                    master.handleSearchStoresRequest(type, routingKey, payload, out);
                    break;
                case RATE_STORE_REQUEST:
                    if (routingKey == null || routingKey.trim().isEmpty()) {
                        out.println(JsonUtil.createStatusResponseJson(null, "FAILURE", type + " requires a storeName as routing key."));
                        return;
                    }
                    master.handleRateStoreRequest(type, routingKey, payload, out);
                    break;
                case GET_SALES_BY_STORE_TYPE_REQUEST:
                    if (routingKey == null || routingKey.trim().isEmpty()) {
                        out.println(JsonUtil.createStatusResponseJson(null, "FAILURE", type + " requires a foodCategory as routing key."));
                        return;
                    }
                    // payload might be empty or contain additional filters in future
                    master.handleGetSalesByStoreTypeRequest(type, routingKey, payload, out); // routingKey is the foodCategory
                    break;
                default:
                    System.err.println("Unsupported message type: " + type);
                    out.println(JsonUtil.createStatusResponseJson(null, "FAILURE", "Unsupported message type by Master."));
            }

        } catch (SocketTimeoutException e) {
            System.err.println("ClientHandler: Socket read timeout: " + e.getMessage());
            if (out != null && !clientSocket.isClosed()) out.println(JsonUtil.createStatusResponseJson(null, "FAILURE", "Request timeout"));
        } catch (IOException e) {
            if (!clientSocket.isClosed()) {
                 System.err.println("ClientHandler: IOException: " + e.getMessage());
            } else {
                 System.out.println("ClientHandler: Connection closed by client or server.");
            }
        } catch (Exception e) {
            System.err.println("ClientHandler: Unexpected error: " + e.getMessage());
            e.printStackTrace();
            if (out != null && !clientSocket.isClosed()) {
                 out.println(JsonUtil.createStatusResponseJson(null, "FAILURE", "Internal server error in Master."));
            }
        }
        finally {
            closeConnection();
        }
    }

    private void closeConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
        } catch (IOException e) {
            // System.err.println("ClientHandler: Error closing resources: " + e.getMessage());
        }
    }
}
