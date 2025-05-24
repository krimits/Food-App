package com.fooddelivery.server;

import com.fooddelivery.communication.Message;
import com.fooddelivery.communication.MessageType;
import com.fooddelivery.util.JsonUtil; // Using our basic JsonUtil

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
            // Set a timeout for reading operations to prevent thread hanging indefinitely
            clientSocket.setSoTimeout(30000); // 30 seconds timeout for reads

            String inputLine;
            // Assume client sends one line: MessageType (e.g. ADD_STORE_REQUEST)
            // And the next line is the JSON payload.
            // This is a simplified protocol for now.
            // A more robust way would be to send serialized Message objects or length-prefixed JSON.
            
            String messageTypeStr = in.readLine();
            if (messageTypeStr == null) {
                System.out.println("Client disconnected before sending message type: " + clientSocket.getRemoteSocketAddress());
                return;
            }

            MessageType type;
            try {
                type = MessageType.valueOf(messageTypeStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("Received invalid message type from client: " + messageTypeStr);
                out.println(JsonUtil.createStatusResponseJson(null, "FAILURE", "Invalid message type"));
                return;
            }
            
            String payload = in.readLine();
            if (payload == null) {
                 System.out.println("Client disconnected before sending payload: " + clientSocket.getRemoteSocketAddress());
                 return;
            }

            System.out.println("ClientHandler: Received Message Type: " + type);
            // System.out.println("ClientHandler: Received Payload: " + payload); // Potentially large

            Message clientMessage = new Message(type, payload);

            switch (clientMessage.getType()) {
                case ADD_STORE_REQUEST:
                    master.handleAddStoreRequest(clientMessage, out);
                    break;
                // Other cases for different message types will be added here
                default:
                    System.err.println("Unsupported message type: " + clientMessage.getType());
                    out.println(JsonUtil.createStatusResponseJson(null, "FAILURE", "Unsupported message type"));
            }

        } catch (SocketTimeoutException e) {
            System.err.println("ClientHandler: Socket read timeout: " + e.getMessage());
            out.println(JsonUtil.createStatusResponseJson(null, "FAILURE", "Request timeout"));
        } catch (IOException e) {
            if (!clientSocket.isClosed()) {
                 System.err.println("ClientHandler: IOException: " + e.getMessage());
            } else {
                 System.out.println("ClientHandler: Connection closed by client or server.");
            }
        } catch (Exception e) {
            System.err.println("ClientHandler: Unexpected error: " + e.getMessage());
            e.printStackTrace();
            if (!clientSocket.isClosed() && out != null) {
                 out.println(JsonUtil.createStatusResponseJson(null, "FAILURE", "Internal server error"));
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
            System.out.println("ClientHandler: Connection closed with " + (clientSocket != null ? clientSocket.getRemoteSocketAddress() : "client"));
        } catch (IOException e) {
            System.err.println("ClientHandler: Error closing resources: " + e.getMessage());
        }
    }
}
