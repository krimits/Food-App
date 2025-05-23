
package com.example.myapplication;
import org.json.JSONArray;
import org.json.JSONObject;


import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.*;

/**
 * Βοηθητική κλάση για την ανάλυση JSON αρχείων με καταστήματα
 */
class StoreParser {
    /**
     * Φορτώνει καταστήματα από ένα αρχείο JSON
     * 
     * @param jsonFilePath Η διαδρομή του αρχείου JSON
     * @return ArrayList με τα καταστήματα που φορτώθηκαν
     * @throws IOException Σε περίπτωση σφάλματος ανάγνωσης του αρχείου
     */
    public static ArrayList<Store> parseStoresFromJsonFile(String jsonFilePath) throws IOException {
        ArrayList<Store> stores = new ArrayList<>();
        
        try (FileReader reader = new FileReader(jsonFilePath)) {
            StringBuilder contentBuilder = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                contentBuilder.append((char) c);
            }
            String jsonContent = contentBuilder.toString();
            
            // Ανάλυση του JSON πίνακα
            JSONArray jsonArray = new JSONArray(jsonContent);
            
            // Επεξεργασία κάθε καταστήματος
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                
                // Ανάγνωση πληροφοριών καταστήματος
                String name = (String) jsonObject.get("StoreName");
                double latitude = ((Number) jsonObject.get("Latitude")).doubleValue();
                double longitude = ((Number) jsonObject.get("Longitude")).doubleValue();
                String category = (String) jsonObject.get("FoodCategory");
                double stars = ((Number) jsonObject.get("Stars")).doubleValue();
                int reviews = ((Number) jsonObject.get("NoOfVotes")).intValue();
                String storeLogoPath = (String) jsonObject.get("StoreLogo");
                
                // Ανάγνωση λίστας προϊόντων
                ArrayList<Product> products = new ArrayList<>();
                JSONArray productsArray = (JSONArray) jsonObject.get("Products");
                for (Object prodObj : productsArray) {
                    JSONObject productJson = (JSONObject) prodObj;
                    
                    // Εξαγωγή πεδίων προϊόντος
                    String productName = (String) productJson.get("ProductName");
                    String productType = (String) productJson.get("ProductType");
                    int amount = ((Number) productJson.get("AvailableAmount")).intValue();
                    double productPrice = ((Number) productJson.get("Price")).doubleValue();
                    
                    products.add(new Product(productName, productType, amount, productPrice));
                }
                
                Store store = new Store(name, latitude, longitude, category, stars, reviews, storeLogoPath, products);
                stores.add(store);
            }
        }
        
        return stores;
    }
}

public class Manager {
    // Λίστα για την αποθήκευση όλων των καταστημάτων
    private static ArrayList<Store> stores = new ArrayList<>();
    
    /**
     * Φορτώνει καταστήματα από ένα αρχείο JSON και τα προσθέτει στη λίστα καταστημάτων.
     * 
     * @param jsonFilePath Η διαδρομή του αρχείου JSON με τα καταστήματα
     * @return Τον αριθμό των καταστημάτων που προστέθηκαν επιτυχώς
     */
    private static int loadStoresFromJsonFile(String jsonFilePath) {
        int successCount = 0;
        
        try {
            // Χρήση του StoreParser για την ανάλυση του JSON αρχείου
            ArrayList<Store> parsedStores = StoreParser.parseStoresFromJsonFile(jsonFilePath);
            
            if (parsedStores.isEmpty()) {
                System.out.println("Δεν βρέθηκαν καταστήματα στο αρχείο ή υπήρξε πρόβλημα κατά την ανάλυση.");
            } else {
                // Προσθήκη των καταστημάτων στη λίστα
                for (Store store : parsedStores) {
                    // Έλεγχος αν το κατάστημα υπάρχει ήδη (με βάση το όνομα)
                    boolean storeExists = false;
                    for (Store existingStore : stores) {
                        if (existingStore.getStoreName().equalsIgnoreCase(store.getStoreName())) {
                            storeExists = true;
                            break;
                        }
                    }
                    
                    // Προσθήκη του καταστήματος αν δεν υπάρχει ήδη
                    if (!storeExists) {
                        stores.add(store);
                        successCount++;
                        System.out.println(store.toString());
                    } else {
                        System.out.println("Το κατάστημα '" + store.getStoreName() + "' υπάρχει ήδη.");
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Σφάλμα κατά την ανάγνωση του αρχείου: " + e.getMessage());
        }
        
        return successCount;
    }
    
    /**
     * Επιστρέφει τη λίστα με όλα τα καταστήματα
     * 
     * @return ArrayList με όλα τα καταστήματα
     */
    public static ArrayList<Store> getAllStores() {
        return stores;
    }
    
    /**
     * Αναζητά ένα κατάστημα με βάση το όνομά του
     * 
     * @param storeName Το όνομα του καταστήματος
     * @return Το κατάστημα ή null αν δεν βρεθεί
     */
    public static Store findStoreByName(String storeName) {
        for (Store store : stores) {
            if (store.getStoreName().equalsIgnoreCase(storeName)) {
                return store;
            }
        }
        return null;
    }
    
    /**
     * Προσθέτει ένα νέο κατάστημα στη λίστα
     * 
     * @param store Το νέο κατάστημα
     * @return true αν προστέθηκε επιτυχώς, false αν υπάρχει ήδη
     */
    public static boolean addStore(Store store) {
        // Έλεγχος αν το κατάστημα υπάρχει ήδη
        for (Store existingStore : stores) {
            if (existingStore.getStoreName().equalsIgnoreCase(store.getStoreName())) {
                return false; // Το κατάστημα υπάρχει ήδη
            }
        }
        
        // Προσθήκη του καταστήματος
        stores.add(store);
        return true;
    }
    
    public static void main(String[] args) throws ParseException, FileNotFoundException {
        Scanner sc = new Scanner(System.in);

        boolean flag = true;

        while (flag) {
            // Display menu options
            System.out.println("1.Add store");
            System.out.println("2.Add Product");
            System.out.println("3.Remove Product");
            System.out.println("4.Total sales by store type");
            System.out.println("5.Total sales by product category");
            System.out.println("6.Exit");
            System.out.print("Choose an option: ");
            String number = sc.nextLine();

            if (number.equals("1")) {
                System.out.print("Give the json file of the store: ");
                String jsonPath = sc.nextLine();
                
                // Χρήση της μεθόδου loadStoresFromJsonFile που έχει ήδη υλοποιηθεί
                int addedStores = loadStoresFromJsonFile(jsonPath);
                System.out.println("Προστέθηκαν " + addedStores + " καταστήματα επιτυχώς.");
                
                ArrayList<Store> storesToSend = new ArrayList<>(stores);
                Socket requestSocket = null;
                ObjectOutputStream out = null;
                ObjectInputStream in = null;
                try {
                    // Connect to master
                    requestSocket = new Socket("localhost", 4321);
                    out = new ObjectOutputStream(requestSocket.getOutputStream());
                    in = new ObjectInputStream(requestSocket.getInputStream());
                
                    // Send to master
                    out.writeObject("manager");
                    out.flush();
                
                    out.writeObject(storesToSend);
                    out.flush();
                
                    // Receive from master
                    String res = (String) in.readObject();
                    System.out.println(res);
                    System.out.print("\n");
                
                } catch (UnknownHostException unknownHost) {
                    System.err.println("You are trying to connect to an unknown host!");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (in != null) in.close();
                        if (out != null) out.close();
                        if (requestSocket != null) requestSocket.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }

            } else if (number.equals("2")) {
                Socket requestSocket = null;
                ObjectOutputStream out = null;
                ObjectInputStream in = null;

                String s = null;
                String ex = null;

                try {
                    // Connect to master
                    requestSocket = new Socket("localhost", 4321);
                    out = new ObjectOutputStream(requestSocket.getOutputStream());
                    in = new ObjectInputStream(requestSocket.getInputStream());

                    System.out.print("Enter store name to add a product: ");
                    String storeName = sc.nextLine();

                    // Send to master
                    out.writeObject("findStore");
                    out.flush();

                    out.writeObject(storeName);
                    out.flush();

                    // Receive from master
                    s = (String) in.readObject();

                } catch (UnknownHostException unknownHost) {
                    System.err.println("You are trying to connect to an unknown host!");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                } finally {
                    try {
                        in.close();
                        out.close();
                        requestSocket.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }

                //if the store doesn't exist the worker sends back null, otherwise sends the StoreName.
                if (s != null){
                    requestSocket = null;
                    out = null;
                    in = null;
                    String productName = null;

                    try {
                        // Connect to master
                        requestSocket = new Socket("localhost", 4321);
                        out = new ObjectOutputStream(requestSocket.getOutputStream());
                        in = new ObjectInputStream(requestSocket.getInputStream());

                        System.out.print("Enter Product Name: ");
                        productName = sc.nextLine();

                        // Send to master
                        out.writeObject("findProduct");
                        out.flush();

                        out.writeObject(s);
                        out.flush();

                        out.writeObject(productName);
                        out.flush();

                        // Receive from master
                        ex = (String) in.readObject();

                    } catch (UnknownHostException unknownHost) {
                        System.err.println("You are trying to connect to an unknown host!");
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    } finally {
                        try {
                            in.close();
                            out.close();
                            requestSocket.close();
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    }

                    requestSocket = null;
                    out = null;
                    in = null;

                    //if the product doesn't exist the worker sends back "doesnt exist", otherwise sends "exists"
                    if (ex.equalsIgnoreCase("exists")) {
                        try {
                            // Connect to master
                            requestSocket = new Socket("localhost", 4321);
                            out = new ObjectOutputStream(requestSocket.getOutputStream());
                            in = new ObjectInputStream(requestSocket.getInputStream());

                            System.out.print("Product already exists. How much would you like to add to the quantity? ");
                            int additionalAmount = Integer.parseInt(sc.nextLine());

                            // Send to master
                            out.writeObject("AmountInc");
                            out.flush();

                            out.writeObject(s);
                            out.flush();

                            out.writeObject(productName);
                            out.flush();

                            out.writeInt(additionalAmount);
                            out.flush();

                            // Receive from master
                            String res = (String) in.readObject();
                            System.out.println(res);
                            System.out.print("\n");

                        } catch (UnknownHostException unknownHost) {
                            System.err.println("You are trying to connect to an unknown host!");
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        } finally {
                            try {
                                in.close();
                                out.close();
                                requestSocket.close();
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                        }
                    } else {

                        Product pro = null;
                        try {
                            // Connect to master
                            requestSocket = new Socket("localhost", 4321);
                            out = new ObjectOutputStream(requestSocket.getOutputStream());
                            in = new ObjectInputStream(requestSocket.getInputStream());

                            System.out.print("Enter Product Type: ");
                            String productType = sc.nextLine();

                            System.out.print("Enter Available Amount: ");
                            int amount = Integer.parseInt(sc.nextLine());

                            System.out.print("Enter Product Price: ");
                            double productPrice = Double.parseDouble(sc.nextLine());

                            pro = new Product(productName, productType, amount, productPrice);

                            // Send to master
                            out.writeObject("NewProduct");
                            out.flush();

                            out.writeObject(s);
                            out.flush();

                            out.writeObject(pro);
                            out.flush();

                            // Receive from master
                            String res = (String) in.readObject();
                            System.out.println(res);
                            System.out.print("\n");

                        } catch (UnknownHostException unknownHost) {
                            System.err.println("You are trying to connect to an unknown host!");
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        } finally {
                            try {
                                in.close();
                                out.close();
                                requestSocket.close();
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                        }
                    }

                }else System.out.println("Store not found.");


            } else if (number.equals("3")) {
                Socket requestSocket = null;
                ObjectOutputStream out = null;
                ObjectInputStream in = null;

                String storeName = null;

                try {
                    // Connect to master
                    requestSocket = new Socket("localhost", 4321);
                    out = new ObjectOutputStream(requestSocket.getOutputStream());
                    in = new ObjectInputStream(requestSocket.getInputStream());

                    System.out.print("Enter store name to remove a product: ");
                    String s = sc.nextLine();

                    // Send to master
                    out.writeObject("findStore");
                    out.flush();

                    out.writeObject(s);
                    out.flush();

                    // Receive from master
                    storeName = (String) in.readObject();

                } catch (UnknownHostException unknownHost) {
                    System.err.println("You are trying to connect to an unknown host!");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                } finally {
                    try {
                        if (in != null) in.close();
                        if (out != null) out.close();
                        if (requestSocket != null) requestSocket.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }

                //if the store doesn't exist the worker sends back null, otherwise sends the StoreName.
                if (storeName != null) {
                    requestSocket = null;
                    out = null;
                    in = null;

                    String productName = null;

                    try {
                        // Connect to master
                        requestSocket = new Socket("localhost", 4321);
                        out = new ObjectOutputStream(requestSocket.getOutputStream());
                        in = new ObjectInputStream(requestSocket.getInputStream());

                        System.out.print("Enter Product Name:");
                        String p = sc.nextLine();

                        // Send to master
                        out.writeObject("findProduct2");
                        out.flush();

                        out.writeObject(storeName);
                        out.flush();

                        out.writeObject(p);
                        out.flush();

                        // Receive from master
                        productName = (String) in.readObject();

                    } catch (UnknownHostException unknownHost) {
                        System.err.println("You are trying to connect to an unknown host!");
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    } finally {
                        try {
                            in.close();
                            out.close();
                            requestSocket.close();
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    }

                    //if the product doesn't exist the worker sends back null, otherwise sends the ProductName.
                    if (productName != null && !productName.equals("hidden")) {

                        requestSocket = null;
                        out = null;
                        in = null;

                        try {
                            // Connect to master
                            requestSocket = new Socket("localhost", 4321);
                            out = new ObjectOutputStream(requestSocket.getOutputStream());
                            in = new ObjectInputStream(requestSocket.getInputStream());

                            System.out.println("1. Remove the product");
                            System.out.println("2. Decrease the quantity of the product");
                            System.out.print("Choose an option: ");
                            String num = sc.nextLine();

                            if(num.equals("1")){
                                // Send to master
                                out.writeObject("remove");
                                out.flush();

                                out.writeObject(storeName);
                                out.flush();

                                out.writeObject(productName);
                                out.flush();

                                // Receive from master
                                String res = (String) in.readObject();
                                System.out.println(res);
                                System.out.print("\n");

                            } else if (num.equals("2")) {

                                System.out.print("How much would you like to decrease the quantity?");
                                int amount = Integer.parseInt(sc.nextLine());

                                // Send to master
                                out.writeObject("AmountDec");
                                out.flush();

                                out.writeObject(storeName);
                                out.flush();

                                out.writeObject(productName);
                                out.flush();

                                out.writeInt(amount);
                                out.flush();

                                // Receive from master
                                String res = (String) in.readObject();
                                System.out.println(res);
                                System.out.print("\n");

                            }

                        } catch (UnknownHostException unknownHost) {
                            System.err.println("You are trying to connect to an unknown host!");
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        } finally {
                            try {
                                in.close();
                                out.close();
                                requestSocket.close();
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                        }

                    } else if (productName == null){
                        System.out.println("The product doesn't exist");
                    } else if (productName.equals("hidden")) {
                        System.out.println("The product is already removed");
                    }

                }else System.out.println("Store not found.");


            } else if (number.equals("4")) {
                System.out.print("Enter the store type (e.g., pizzeria, burger):");
                String storeType = sc.nextLine();

                Socket requestSocket = null;
                ObjectOutputStream out = null;
                ObjectInputStream in = null;
                try{
                    // Connect to master
                    requestSocket = new Socket("localhost", 4321);
                    out = new ObjectOutputStream(requestSocket.getOutputStream());
                    in = new ObjectInputStream(requestSocket.getInputStream());

                    // Send to master
                    out.writeObject("storeType");
                    out.flush();

                    out.writeObject(storeType);
                    out.flush();

                    // Receive from master
                    Map<String, Integer> result = (Map<String, Integer>) in.readObject();

                    int total = 0;
                    System.out.println("Sales by Store for type: " + storeType);
                    for (Map.Entry<String, Integer> entry : result.entrySet()) { // Print for every store
                        System.out.println("• " + entry.getKey() + ": " + entry.getValue());
                        total += entry.getValue();
                    }
                    System.out.println("Total Sales: " + total + "\n");

                } catch (UnknownHostException unknownHost) {
                    System.err.println("You are trying to connect to an unknown host!");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                } finally {
                    try {
                        if (in != null) in.close();
                        if (out != null) out.close();
                        if (requestSocket != null) requestSocket.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }


            } else if (number.equals("5")) {
                System.out.print("Enter the product category (e.g., pizza, salad, burger): ");
                String productCategory = sc.nextLine();

                Socket requestSocket = null;
                ObjectOutputStream out = null;
                ObjectInputStream in = null;
                try {
                    // Connect to master
                    requestSocket = new Socket("localhost", 4321);
                    out = new ObjectOutputStream(requestSocket.getOutputStream());
                    in = new ObjectInputStream(requestSocket.getInputStream());

                    // Send to master
                    out.writeObject("productCategory");
                    out.flush();

                    out.writeObject(productCategory);
                    out.flush();

                    // Receive from master
                    Map<String, Integer> result = (Map<String, Integer>) in.readObject();

                    int total = 0;
                    System.out.println("Sales by Store for product category: " + productCategory);
                    for (Map.Entry<String, Integer> entry : result.entrySet()) { // Print for every store
                        System.out.println("• " + entry.getKey() + ": " + entry.getValue());
                        total += entry.getValue();
                    }
                    System.out.println("Total Sales: " + total + "\n");



                } catch (UnknownHostException unknownHost) {
                    System.err.println("You are trying to connect to an unknown host!");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                } finally {
                    try {
                        in.close();
                        out.close();
                        requestSocket.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }


            } else if (number.equals("6")) {
                System.out.println("Exit");
                flag = false;
            } else {
                System.out.println("Wrong number. Try again");
            }
        }

    }
}