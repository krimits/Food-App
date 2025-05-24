package com.fooddelivery.manager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class ManagerConsole {
    private String masterHost;
    private int masterPort;

    public ManagerConsole(String masterHost, int masterPort) {
        this.masterHost = masterHost;
        this.masterPort = masterPort;
    }

    public void start() {
        System.out.println("Manager Console started. Connecting to Master at " + masterHost + ":" + masterPort);
        System.out.println("Type 'addstore <filepath.json>' to add a new store.");
        System.out.println("Type 'exit' to close the console.");

        try (BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))) {
            String command;
            while (true) {
                System.out.print("manager> ");
                command = consoleReader.readLine();
                if (command == null || command.trim().equalsIgnoreCase("exit")) {
                    System.out.println("Exiting Manager Console.");
                    break;
                }
                processCommand(command.trim());
            }
        } catch (IOException e) {
            System.err.println("Error reading command from console: " + e.getMessage());
        }
    }

    private void processCommand(String command) {
        if (command.toLowerCase().startsWith("addstore ")) {
            String[] parts = command.split("\s+", 2);
            if (parts.length < 2) {
                System.out.println("Usage: addstore <filepath.json>");
                return;
            }
            String filePath = parts[1];
            handleAddStoreCommand(filePath);
        } else if (command.isEmpty()) {
            // Do nothing, just show prompt again
        } 
        // Add other command processing here later (e.g., addproduct, removeproduct, salesreport)
        // else if (command.toLowerCase().startsWith("addproduct ")) { ... }
        else {
            System.out.println("Unknown command: '" + command + "'. Type 'exit' to close.");
        }
    }

    private void handleAddStoreCommand(String filePath) {
        try {
            String storeJson = readFileContent(filePath);
            if (storeJson == null || storeJson.isEmpty()) {
                System.out.println("Error: File is empty or could not be read: " + filePath);
                return;
            }
            
            // Send to Master
            // For now, the protocol with Master's ClientHandler is:
            // 1. Send MessageType string ("ADD_STORE_REQUEST")
            // 2. Send JSON payload string
            try (Socket socket = new Socket(masterHost, masterPort);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                
                socket.setSoTimeout(15000); // 15 seconds timeout for server response

                System.out.println("Sending ADD_STORE_REQUEST to Master for file: " + filePath);
                out.println("ADD_STORE_REQUEST"); // MessageType
                out.println(storeJson);          // JSON Payload

                // Wait for Master's response (which is forwarded from the Worker)
                String response = in.readLine();
                if (response != null) {
                    System.out.println("Master Response: " + response);
                } else {
                    System.out.println("No response received from Master.");
                }

            } catch (UnknownHostException e) {
                System.err.println("Error: Master host not found at " + masterHost + ":" + masterPort);
            } catch (IOException e) {
                System.err.println("Error connecting to or communicating with Master: " + e.getMessage());
            }

        } catch (IOException e) {
            System.out.println("Error reading JSON file " + filePath + ": " + e.getMessage());
        }
    }

    private String readFileContent(String filePath) throws IOException {
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader fileReader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = fileReader.readLine()) != null) {
                contentBuilder.append(line).append(System.lineSeparator());
            }
        }
        return contentBuilder.toString().trim();
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java com.fooddelivery.manager.ManagerConsole <masterHost> <masterPort>");
            System.exit(1);
        }
        String host = args[0];
        int port;
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Invalid Master port: " + args[1]);
            System.exit(1);
            return;
        }

        ManagerConsole console = new ManagerConsole(host, port);
        console.start();
    }
}
