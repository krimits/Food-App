package com.fooddelivery.manager;

import com.fooddelivery.util.JsonUtil; 

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
        System.out.println("Manager Console. Connecting to Master at " + masterHost + ":" + masterPort);
        System.out.println("Commands:");
        System.out.println("  addstore <filepath.json>");
        System.out.println("  addproduct <storeName> <productName> <productType> <price> <initialStock>");
        System.out.println("  removeproduct <storeName> <productName>");
        System.out.println("  updatestock <storeName> <productName> <quantityChange>");
        System.out.println("  salesbyproduct <storeName>");
        System.out.println("  salesbystoretype <foodCategory>  (Placeholder - basic data only)");
        System.out.println("  salesbyproductcategory <productType> (Placeholder - basic data only)");
        System.out.println("  exit");

        try (BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))) {
            String commandLine;
            while (true) {
                System.out.print("manager> ");
                commandLine = consoleReader.readLine();
                if (commandLine == null || commandLine.trim().equalsIgnoreCase("exit")) {
                    System.out.println("Exiting Manager Console.");
                    break;
                }
                processCommand(commandLine.trim());
            }
        } catch (IOException e) {
            System.err.println("Error reading command from console: " + e.getMessage());
        }
    }

    private void processCommand(String commandLine) {
        String[] parts = commandLine.split("\s+", 2);
        String command = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";

        switch (command) {
            case "addstore":
                handleAddStoreCommand(args);
                break;
            case "addproduct":
                handleAddProductCommand(args);
                break;
            case "removeproduct":
                handleRemoveProductCommand(args);
                break;
            case "updatestock":
                handleUpdateStockCommand(args);
                break;
            case "salesbyproduct":
                handleSalesByProductCommand(args);
                break;
            case "salesbystoretype":
                handleSalesByStoreTypeCommand(args);
                break;
            case "salesbyproductcategory":
                handleSalesByProductCategoryCommand(args);
                break;
            case "":
                break; 
            default:
                System.out.println("Unknown command: '" + command + "'. Type 'exit' to close.");
        }
    }

    // ... (handleAddStoreCommand, handleAddProductCommand, handleRemoveProductCommand, handleUpdateStockCommand, readFileContent remain the same)
    private void handleAddStoreCommand(String filePath) { 
        if (filePath.isEmpty()) { System.out.println("Usage: addstore <filepath.json>"); return; }
        try {
            String storeJson = readFileContent(filePath);
            if (storeJson == null || storeJson.isEmpty()) { System.out.println("Error: File is empty or could not be read: " + filePath); return; }
            sendRequestToMaster("ADD_STORE_REQUEST", storeJson); 
        } catch (IOException e) { System.out.println("Error reading JSON file " + filePath + ": " + e.getMessage());}
    }
    private void handleAddProductCommand(String args) { 
        String[] parts = args.split("\s+", 5);
        if (parts.length < 5) { System.out.println("Usage: addproduct <storeName> <productName> <productType> <price> <initialStock>"); return; }
        String storeName = parts[0]; String productName = parts[1]; String productType = parts[2]; double price; int initialStock;
        try { price = Double.parseDouble(parts[3]); initialStock = Integer.parseInt(parts[4]); } catch (NumberFormatException e) { System.out.println("Error: Invalid price or initial stock number format."); return; }
        String payloadJson = JsonUtil.createAddProductRequestJson(productName, productType, price, initialStock);
        sendRequestToMaster("ADD_PRODUCT_REQUEST:" + storeName, payloadJson);
    }
    private void handleRemoveProductCommand(String args) { 
        String[] parts = args.split("\s+", 2);
        if (parts.length < 2) { System.out.println("Usage: removeproduct <storeName> <productName>"); return; }
        String storeName = parts[0]; String productName = parts[1];
        String payloadJson = JsonUtil.createRemoveProductRequestJson(productName);
        sendRequestToMaster("REMOVE_PRODUCT_REQUEST:" + storeName, payloadJson);
    }
    private void handleUpdateStockCommand(String args) { 
        String[] parts = args.split("\s+", 3);
        if (parts.length < 3) { System.out.println("Usage: updatestock <storeName> <productName> <quantityChange>"); return; }
        String storeName = parts[0]; String productName = parts[1]; int quantityChange;
        try { quantityChange = Integer.parseInt(parts[2]); } catch (NumberFormatException e) { System.out.println("Error: Invalid quantity change number format."); return; }
        String payloadJson = JsonUtil.createUpdateStockRequestJson(productName, quantityChange);
        sendRequestToMaster("UPDATE_STOCK_REQUEST:" + storeName, payloadJson);
    }
     private String readFileContent(String filePath) throws IOException {
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader fileReader = new BufferedReader(new FileReader(filePath))) { String line; while ((line = fileReader.readLine()) != null) { contentBuilder.append(line).append(System.lineSeparator()); } }
        return contentBuilder.toString().trim();
    }


    private void handleSalesByProductCommand(String storeName) {
        if (storeName.isEmpty()) {
            System.out.println("Usage: salesbyproduct <storeName>");
            return;
        }
        // Payload for GET_SALES_BY_PRODUCT_REQUEST needs storeName.
        // Master's ClientHandler expects TYPE:ROUTING_KEY, so storeName is the routing key.
        // The JSON payload can also contain the storeName for clarity or future filters.
        String payloadJson = JsonUtil.createSalesRequestJson(storeName, null, null);
        sendRequestToMaster("GET_SALES_BY_PRODUCT_REQUEST:" + storeName, payloadJson);
    }

    private void handleSalesByStoreTypeCommand(String foodCategory) {
        if (foodCategory.isEmpty()) {
            System.out.println("Usage: salesbystoretype <foodCategory>");
            return;
        }
        // Master will use foodCategory as routing key for now (or part of payload for later MapReduce)
        // For now, this will be a placeholder as full aggregation requires MapReduce.
        // Master might not have a specific handler for this yet beyond acknowledging.
        String payloadJson = JsonUtil.createSalesRequestJson(null, foodCategory, null);
        sendRequestToMaster("GET_SALES_BY_STORE_TYPE_REQUEST:" + foodCategory, payloadJson);
        System.out.println("(Note: Full sales by store type requires MapReduce; this may show limited/placeholder data.)");
    }

    private void handleSalesByProductCategoryCommand(String productType) {
        if (productType.isEmpty()) {
            System.out.println("Usage: salesbyproductcategory <productType>");
            return;
        }
        // Similar to above, productType is the key. Placeholder for now.
        String payloadJson = JsonUtil.createSalesRequestJson(null, null, productType);
        sendRequestToMaster("GET_SALES_BY_PRODUCT_CATEGORY_REQUEST:" + productType, payloadJson);
        System.out.println("(Note: Full sales by product category requires MapReduce; this may show limited/placeholder data.)");
    }
    
    // Generic method to send request to Master (remains the same)
    private void sendRequestToMaster(String firstLine, String payloadJson) { 
        try (Socket socket = new Socket(masterHost, masterPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            socket.setSoTimeout(15000); 
            System.out.println("Sending to Master -> First Line: " + firstLine);
            System.out.println("Sending to Master -> Payload JSON: " + payloadJson);
            out.println(firstLine); 
            out.println(payloadJson);  
            String response = "";
            // String line;
            // Read multiple lines if response is large/formatted JSON
            // However, current worker responses are single, compact JSON lines.
            // For potentially larger/formatted JSON responses from sales reports:
            // while ((line = in.readLine()) != null) {
            //    response += line;
            // }
            response = in.readLine(); // Assuming single line JSON response for now

            if (response != null && !response.isEmpty()) {
                System.out.println("Master Response: " + response);
                // Basic pretty print attempt for JSON if it's a sales response
                if (firstLine.startsWith("GET_SALES")) {
                    try {
                        // This is a crude way to somewhat format. A real JSON library is better.
                        String formatted = response.replace("{", "{\n  ")
                                               .replace("}", "\n}")
                                               .replace(",\"", ",\n  \"")
                                               .replace("[{", "[\n  {\n    ")
                                               .replace("},{", "  },\n  {\n    ")
                                               .replace("}]", "  }\n]");
                        System.out.println("Formatted Master Response (basic attempt): \n" + formatted);
                    } catch (Exception e) { /* ignore formatting error, print raw */ }
                }
            } else {
                System.out.println("No response or empty response received from Master.");
            }
        } catch (UnknownHostException e) { System.err.println("Error: Master host not found at " + masterHost + ":" + masterPort);
        } catch (IOException e) { System.err.println("Error connecting/communicating with Master: " + e.getMessage()); e.printStackTrace(); }
    }

    public static void main(String[] args) { 
        if (args.length < 2) { System.err.println("Usage: java com.fooddelivery.manager.ManagerConsole <masterHost> <masterPort>"); System.exit(1); }
        String host = args[0]; int port;
        try { port = Integer.parseInt(args[1]); } catch (NumberFormatException e) { System.err.println("Invalid Master port: " + args[1]); System.exit(1); return; }
        ManagerConsole console = new ManagerConsole(host, port);
        console.start();
    }
}
