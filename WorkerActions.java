package com.example.myapplication;
import java.awt.print.Book;
import java.io.*;
import java.net.*;
import java.util.*;

public class WorkerActions extends Thread {
    ObjectInputStream in;
    ObjectOutputStream out;
    private final ArrayList<Store> stores;
    private final Object lock;
    private final Socket connection;

    public WorkerActions(Socket connection, ArrayList<Store> stores, Object lock) {
        this.connection = connection;
        this.stores = stores;
        this.lock = lock;
        try {
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            String role = (String) in.readObject();

            if (role.equals("manager")) {
                // Receive from master
                Store s = (Store) in.readObject();

                stores.add(s);

                // Send to master
                out.writeObject("Store added successfully");
                out.flush();

            }else if (role.equals("findStore")) {
                // Receive from master
                String storeName = (String) in.readObject();

                boolean storeFound = false;

                synchronized (lock) {
                    for (Store store : stores) {
                        if (store.getStoreName().equalsIgnoreCase(storeName)) {
                            storeFound = true;
                            break; // exit after store is found
                        }
                    }
                }

                if (!storeFound) {
                    storeName = null;
                }

                // Send to master
                out.writeObject(storeName);
                out.flush();

            }else if (role.equals("findProduct")) {
                // Receive from master
                String storeName = (String) in.readObject();
                String ProductName = (String) in.readObject();

                boolean productFound = false;

                synchronized (lock) {
                    for (Store store : stores) {
                        if (store.getStoreName().equalsIgnoreCase(storeName)) {
                            for (Product pro : store.getProducts()) {
                                if (pro.getName().equalsIgnoreCase(ProductName)) {
                                    productFound = true;
                                    break; // exit after product is found
                                }
                            }
                        }
                    }
                }

                // Send to master
                if (productFound) {
                    out.writeObject("exists");
                }else{
                    out.writeObject("doesnt exist");
                }
                out.flush();


            }else if (role.equals("findProduct2")) {
                // Receive from master
                String storeName = (String) in.readObject();
                String ProductName = (String) in.readObject();

                boolean productFound = false;

                synchronized (lock) {
                    for (Store store : stores) {
                        if (store.getStoreName().equalsIgnoreCase(storeName)) {
                            for (Product pro : store.getProducts()) {
                                if (pro.getName().equalsIgnoreCase(ProductName)) {
                                    productFound = true;
                                    // Send to master
                                    if (pro.getQuantity() == -1){
                                        out.writeObject("hidden");
                                        out.flush();
                                    }else {
                                        out.writeObject(ProductName);
                                        out.flush();
                                    }
                                    break; // exit after product is found
                                }
                            }
                        }
                    }
                }

                if (!productFound) {
                    ProductName = null;
                    out.writeObject(ProductName);
                    out.flush();
                }

            }else if (role.equals("AmountInc")) {
                // Receive from master
                String storeName = (String) in.readObject();
                String ProductName = (String) in.readObject();
                int amount = (int) in.readInt();

                synchronized (lock) {
                    for (Store store : stores) {
                        if (store.getStoreName().equalsIgnoreCase(storeName)) {
                            for (Product pro : store.getProducts()) {
                                if (pro.getName().equalsIgnoreCase(ProductName)) {
                                    pro.setQuantity(amount + pro.getQuantity());
                                    break; // exit after quantity is changed
                                }
                            }
                        }
                    }
                }

                // Send to master
                out.writeObject("Amount changed successfully");
                out.flush();

            }else if (role.equals("NewProduct")) {
                // Receive from master
                String storeName = (String) in.readObject();
                Product pro = (Product) in.readObject();

                synchronized (lock) {
                    for (Store store : stores) {
                        if (store.getStoreName().equalsIgnoreCase(storeName)) {
                            store.getProducts().add(pro);
                            System.out.println(store.getProducts());
                            break; // exit after product is added

                        }
                    }
                }

                // Send to master
                out.writeObject("Product added successfully");
                out.flush();

            }else if (role.equals("remove")) {
                // Receive from master
                String storeName = (String) in.readObject();
                String pro = (String) in.readObject();

                boolean prodFound = false;

                synchronized (lock) {
                    for (Store store : stores) {
                        if (store.getStoreName().equalsIgnoreCase(storeName)) {
                            prodFound = true;
                            for (Product prod : store.getProducts()) {
                                if (prod.getName().equalsIgnoreCase(pro)) {
                                    prod.setQuantity(-1);
                                    prod.setStatus("hidden");
                                    break; // exit after quantity is changed
                                }
                            }

                        }
                    }
                }

                // Send to master
                if (prodFound) {
                    out.writeObject("Product removed or updated successfully.");
                } else {
                    out.writeObject("Product not found.");
                }
                out.flush();

            }else if (role.equals("AmountDec")) {
                // Receive from master
                String storeName = (String) in.readObject();
                String ProductName = (String) in.readObject();
                int amount = (int) in.readInt();

                synchronized (lock) {
                    for (Store store : stores) {
                        if (store.getStoreName().equalsIgnoreCase(storeName)) {
                            for (Product pro : store.getProducts()) {
                                if (pro.getName().equalsIgnoreCase(ProductName)) {
                                    if ((pro.getQuantity() - amount)>=0) {
                                        pro.setQuantity(pro.getQuantity() - amount);
                                        out.writeObject("Amount changed successfully");
                                        out.flush();
                                    }else {
                                        out.writeObject("Amount is greater than the quantity");
                                        out.flush();
                                    }
                                }
                            }
                        }
                    }
                }


            }else if (role.equals("storeType")) {
                // Receive from master
                String requestedType = (String) in.readObject(); // e.g., "pizzeria"

                Map<String, Integer> result = new HashMap<>();

                synchronized (lock) {
                    int totalSold = 0;
                    for (Store store : stores) {
                        if (store.getCategory().equalsIgnoreCase(requestedType)) {
                            for (Purchase purchase : store.getPurchases()) {
                                for (Product p : purchase.getPurchasedProducts()) {
                                    totalSold += p.getQuantity();  // Sum all quantities
                                }
                            }
                            result.put(store.getStoreName(), totalSold);
                        }
                    }
                }

                // Send to master
                out.writeObject(result);
                out.flush();


            }else if (role.equals("productCategory")) {
                // Receive from master
                String requestedCategory = (String) in.readObject(); // e.g., "pizza"

                Map<String, Integer> result = new HashMap<>();

                synchronized (lock) {
                    for (Store store : stores) {
                        int totalCategorySales = 0;
                        for (Purchase purchase : store.getPurchases()) {
                            for (Product product : purchase.getPurchasedProducts()) {
                                if (product.getCategory().equalsIgnoreCase(requestedCategory)) {
                                    totalCategorySales += product.getQuantity();
                                }
                            }
                        }

                        if (totalCategorySales > 0) {
                            result.put(store.getStoreName(), totalCategorySales);
                        }
                    }
                }

                // Send to master
                out.writeObject(result);
                out.flush();

            }else if (role.equals("client")) {
                // Receive from master
                String clientId = (String) in.readObject();

                MapReduceRequest request = (MapReduceRequest) in.readObject();

                double userLat = request.getClientLatitude();
                double userLon = request.getClientLongitude();
                double maxDistance = request.getRadius();

                ArrayList<Store> result = new ArrayList<>();

                synchronized (lock) {
                    for (Store store : stores) {
                        double storeLat = store.getLatitude();
                        double storeLon = store.getLongitude();

                        double distance = Math.sqrt(Math.pow(userLat - storeLat, 2) + Math.pow(userLon - storeLon, 2));
                        if (distance <= maxDistance) {
                            result.add(store);
                        }
                    }
                }

                // Send to master
                out.writeObject(clientId);
                out.flush();

                out.writeObject(result);
                out.flush();

            }else if (role.equals("filter")) {
                // Receive from master
                String clientId = (String) in.readObject();
                MapReduceRequest request = (MapReduceRequest) in.readObject();

                double userLat = request.getClientLatitude();
                double userLon = request.getClientLongitude();
                double radius = request.getRadius();

                ArrayList<String> categories = (ArrayList<String>) request.getFoodCategories();
                double minStars = request.getMinStars();
                String price = request.getPriceCategory();

                ArrayList<Store> result = new ArrayList<>();

                synchronized (lock) {
                    for (Store store : stores) {
                        double distance = Math.sqrt(Math.pow(userLat - store.getLatitude(), 2) + Math.pow(userLon - store.getLongitude(), 2));
                        boolean matchesDistance = distance <= radius;
                        boolean matchesCategory = categories.isEmpty() || categories.contains(store.getCategory());
                        boolean matchesStars = minStars == 0 || store.getStars() >= minStars;
                        boolean matchesPrice = price.isEmpty() || store.calculatePriceCategory().equalsIgnoreCase(price);

                        if (matchesDistance && matchesCategory && matchesStars && matchesPrice) {
                            result.add(store);
                        }
                    }
                }

                // Send to master
                out.writeObject(clientId);
                out.flush();

                out.writeObject(result);
                out.flush();


            }else if (role.equals("fetchProducts")) {
                // Receive from master
                String responseId = (String) in.readObject();
                String storeName = (String) in.readObject();

                ArrayList<Product> available = new ArrayList<>();

                synchronized (lock) {
                    for (Store store : stores) {
                        if (store.getStoreName().equalsIgnoreCase(storeName)) {
                            for (Product product : store.getProducts()) {
                                if (product.getStatus().equalsIgnoreCase("visible")) {
                                    available.add(product);
                                }
                            }
                            break;
                        }
                    }
                }

                // Send to master
                out.writeObject(responseId);
                out.flush();
                out.writeObject(available);
                out.flush();

            }else if (role.equals("purchase")) {
                // Receive from master
                String responseId = (String) in.readObject();
                Purchase purchase = (Purchase) in.readObject();
                String storeName = (String) in.readObject();

                ArrayList<Product> requestedProducts = purchase.getPurchasedProducts();

                String message = "";

                synchronized (lock) {
                    Store targetStore = null;
                    for (Store s : stores) { // find the object store
                        if (s.getStoreName().equalsIgnoreCase(storeName)) {
                            targetStore = s;
                            break;
                        }
                    }

                    if (targetStore != null) { // if the store is found we store the products
                        Map<String, Product> storeProductMap = new HashMap<>();
                        for (Product p : targetStore.getProducts()) {
                            storeProductMap.put(p.getName().toLowerCase(), p);
                        }

                        boolean allValid = true;

                        for (Product req : requestedProducts) {
                            Product available = storeProductMap.get(req.getName().toLowerCase());

                            if (available == null) {
                                message = "Product not found: " + req.getName();
                                allValid = false;
                                break;
                            }

                            if (!available.getStatus().equalsIgnoreCase("visible")) {
                                message = "Product not available: " + req.getName();
                                allValid = false;
                                break;
                            }

                            if (available.getQuantity() < req.getQuantity()) {
                                message = "Not enough quantity for: " + req.getName();
                                allValid = false;
                                break;
                            }
                        }

                        if (allValid) {
                            for (Product req : requestedProducts) {
                                Product prod = storeProductMap.get(req.getName().toLowerCase());

                                prod.setQuantity(prod.getQuantity() - req.getQuantity());

                                // Fill up the empty fields
                                req.setCategory(prod.getCategory());
                                req.setPrice(prod.getPrice());
                            }

                            targetStore.getPurchases().add(purchase);
                            message = "Purchase successful at " + targetStore.getStoreName();
                            if (requestedProducts.isEmpty()) {
                                message = "The purchase requested is empty";
                            }

                        }
                    }
                }

                // Send to master
                out.writeObject(responseId);
                out.flush();
                out.writeObject(message);
                out.flush();


            }else if (role.equals("rate")) {
                // Receive from master
                String responseId = (String) in.readObject();
                String storeName = (String) in.readObject();
                int rating = (int) in.readObject();

                boolean storeFound = false;

                synchronized (lock) {
                    for (Store store : stores) {
                        if (store.getStoreName().equalsIgnoreCase(storeName)) {
                            double oldStars = store.getStars();          // current average rating
                            int oldReviews = store.getNoOfReviews();  // total reviews so far

                            int newReviews = oldReviews + 1;
                            double newAvg = (oldStars * oldReviews + rating) / newReviews;

                            // Update store fields
                            store.setStars(newAvg);
                            store.setNoOfReviews(newReviews);

                            storeFound = true;
                            break;
                        }
                    }
                }

                // Send to master
                if (storeFound) {
                    out.writeObject(responseId);
                    out.flush();
                    out.writeObject("Rating submitted successfully.");
                    out.flush();
                } else {
                    out.writeObject(responseId);
                    out.flush();
                    out.writeObject("Store not found.");
                    out.flush();
                }

            }else if (role.equals("customerPurchasesByStore")) {
                // Receive from master
                String customerName = (String) in.readObject();
                String storeName = (String) in.readObject();

                Map<String, Integer> customerPurchases = new HashMap<>();

                synchronized (lock) {
                    // Βρες το κατάστημα
                    Store targetStore = null;
                    for (Store store : stores) {
                        if (store.getStoreName().equalsIgnoreCase(storeName)) {
                            targetStore = store;
                            break;
                        }
                    }

                    if (targetStore != null) {
                        // Πέρασε από όλες τις αγορές του καταστήματος
                        for (Purchase purchase : targetStore.getPurchases()) {
                            // Έλεγξε αν η αγορά είναι από τον συγκεκριμένο πελάτη
                            if (purchase.getCustomerName().equalsIgnoreCase(customerName)) {
                                // Πρόσθεσε τα προϊόντα στο map
                                for (Product product : purchase.getPurchasedProducts()) {
                                    String productName = product.getName();
                                    int quantity = product.getQuantity();

                                    // Αν το προϊόν υπάρχει ήδη στο map, πρόσθεσε την ποσότητα
                                    customerPurchases.merge(productName, quantity, Integer::sum);
                                }
                            }
                        }
                    }
                }

                // Send to master
                out.writeObject(customerPurchases);
                out.flush();
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (connection != null && !connection.isClosed()) connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}