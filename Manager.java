import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.*;

public class Manager {
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
                ArrayList<Store> stores = new ArrayList<>();

                System.out.print("Give the json file of the store: ");
                String jsonPath = sc.nextLine();

                try (FileReader reader = new FileReader(jsonPath)) {

                    StringBuilder contentBuilder = new StringBuilder();
                    int c;
                    while ((c = reader.read()) != -1) {
                        contentBuilder.append((char) c); // Read JSON file content
                    }
                    String jsonContent = contentBuilder.toString();

                    // Parse the JSON array using json-simple
                    JSONParser parser = new JSONParser();
                    JSONArray jsonArray = (JSONArray) parser.parse(jsonContent);

                    // Loop through JSON objects (stores)
                    for (Object obj : jsonArray) {
                        JSONObject jsonObject = (JSONObject) obj;

                        // Read store info
                        String name = (String) jsonObject.get("StoreName");
                        double latitude = ((Number) jsonObject.get("Latitude")).doubleValue();
                        double longitude = ((Number) jsonObject.get("Longitude")).doubleValue();
                        String category = (String) jsonObject.get("FoodCategory");
                        double stars = ((Number) jsonObject.get("Stars")).doubleValue();
                        int reviews = ((Number) jsonObject.get("NoOfVotes")).intValue();
                        String storeLogoPath = (String) jsonObject.get("StoreLogo");

                        // Read product list
                        ArrayList<Product> products = new ArrayList<>();
                        JSONArray productsArray = (JSONArray) jsonObject.get("Products");
                        for (Object prodObj : productsArray) {
                            JSONObject productJson = (JSONObject) prodObj;

                            // Extract product fields
                            String productName = (String) productJson.get("ProductName");
                            String productType = (String) productJson.get("ProductType");
                            int amount = ((Number) productJson.get("AvailableAmount")).intValue();
                            double productPrice = ((Number) productJson.get("Price")).doubleValue();

                            products.add(new Product(productName, productType, amount, productPrice));
                        }

                        Store s = new Store(name, latitude, longitude, category, stars, reviews, storeLogoPath, products);

                        stores.add(s);
                        System.out.println(s);

                        Socket requestSocket = null;
                        ObjectOutputStream out = null;
                        ObjectInputStream in = null;
                        try {
                            // Connect to master
                            requestSocket = new Socket("127.0.0.1", 4321);
                            out = new ObjectOutputStream(requestSocket.getOutputStream());
                            in = new ObjectInputStream(requestSocket.getInputStream());

                            // Send to master
                            out.writeObject("manager");
                            out.flush();

                            out.writeObject(stores);
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
                            // TODO Auto-generated catch block
                            e.printStackTrace();
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
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (org.json.simple.parser.ParseException e) {
                    e.printStackTrace();
                }

            } else if (number.equals("2")) {
                Socket requestSocket = null;
                ObjectOutputStream out = null;
                ObjectInputStream in = null;

                String s = null;
                String ex = null;

                try {
                    // Connect to master
                    requestSocket = new Socket("127.0.0.1", 4321);
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
                        requestSocket = new Socket("127.0.0.1", 4321);
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
                            requestSocket = new Socket("127.0.0.1", 4321);
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
                            requestSocket = new Socket("127.0.0.1", 4321);
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
                    requestSocket = new Socket("127.0.0.1", 4321);
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
                        in.close();
                        out.close();
                        requestSocket.close();
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
                        requestSocket = new Socket("127.0.0.1", 4321);
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
                    if (productName != null && productName!= "hidden") {

                        requestSocket = null;
                        out = null;
                        in = null;

                        try {
                            // Connect to master
                            requestSocket = new Socket("127.0.0.1", 4321);
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
                    } else if (productName == "hidden") {
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
                    requestSocket = new Socket("127.0.0.1", 4321);
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
                        in.close();
                        out.close();
                        requestSocket.close();
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
                    requestSocket = new Socket("127.0.0.1", 4321);
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