package com.example.myapplication;
import java.io.*;
import java.net.*;
import java.text.ParseException;
import java.util.*;
import java.util.Map;
import java.util.HashMap;

public class Actions extends Thread {
    ObjectInputStream in;
    ObjectOutputStream out;
    String[][] workers; // Stores IP and port info for worker nodes
    int counterID;

    public Actions(Socket connection, String[][] workers, int counterID) {
        try {
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
            this.workers = workers;
            this.counterID = counterID;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            String role = (String) in.readObject(); // Read the role of the request

            if (role.equals("manager")) {
                // Receive from manager
                ArrayList<Store> stores = (ArrayList<Store>) in.readObject();
                int successCount = 0;

                for (Store store : stores) {
                    Socket workerSocket = null;
                    ObjectOutputStream outWorker = null;
                    ObjectInputStream inWorker = null;

                    try {
                        // Select worker using hash function
                        int workerId = Math.abs(store.getStoreName().hashCode()) % workers.length;
                        String workerIP = workers[workerId][0];
                        int workerPort = Integer.parseInt(workers[workerId][1]);

                        // Connect to selected worker
                        workerSocket = new Socket(workerIP, workerPort);
                        outWorker = new ObjectOutputStream(workerSocket.getOutputStream());
                        inWorker = new ObjectInputStream(workerSocket.getInputStream());

                        // Send to worker
                        outWorker.writeObject("manager");
                        outWorker.flush();

                        outWorker.writeObject(store);
                        outWorker.flush();

                        // Read from worker
                        String response = (String) inWorker.readObject();

                        if ("Store added successfully".equals(response)) {
                            successCount++;
                        }

                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (inWorker != null) inWorker.close();
                            if (outWorker != null) outWorker.close();
                            if (workerSocket != null) workerSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Send to manager
                if (successCount == stores.size()) {
                    out.writeObject("Store(s) added successfully");
                } else {
                    out.writeObject("Some stores failed to add");
                }
                out.flush();


            }else if (role.equals("findStore")) {
                // Receive from manager
                String storeName = (String) in.readObject(); // Get store name to find the object store

                Socket workerSocket = null;
                ObjectOutputStream outWorker = null;
                ObjectInputStream inWorker = null;

                try {
                    // Select worker using hash of store name
                    int workerId = Math.abs(storeName.hashCode()) % workers.length;
                    String workerIP = workers[workerId][0];
                    int workerPort = Integer.parseInt(workers[workerId][1]);

                    // Connect to selected worker
                    workerSocket = new Socket(workerIP, workerPort);
                    outWorker = new ObjectOutputStream(workerSocket.getOutputStream());
                    inWorker = new ObjectInputStream(workerSocket.getInputStream());

                    // Send to worker
                    outWorker.writeObject("findStore");
                    outWorker.flush();

                    outWorker.writeObject(storeName);
                    outWorker.flush();


                    // Receive from worker
                    String response = (String) inWorker.readObject();

                    // Send to manager
                    out.writeObject(response);
                    out.flush();

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (inWorker != null) inWorker.close();
                        if (outWorker != null) outWorker.close();
                        if (workerSocket != null) workerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }else if (role.equals("findProduct")) {
                // Receive from manager
                String storeName = (String) in.readObject();
                String ProductName = (String) in.readObject();

                Socket workerSocket = null;
                ObjectOutputStream outWorker = null;
                ObjectInputStream inWorker = null;

                try {
                    // Select worker using hash of store name
                    int workerId = Math.abs(storeName.hashCode()) % workers.length;
                    String workerIP = workers[workerId][0];
                    int workerPort = Integer.parseInt(workers[workerId][1]);

                    // Connect to selected worker
                    workerSocket = new Socket(workerIP, workerPort);
                    outWorker = new ObjectOutputStream(workerSocket.getOutputStream());
                    inWorker = new ObjectInputStream(workerSocket.getInputStream());

                    // Send to worker
                    outWorker.writeObject("findProduct");
                    outWorker.flush();

                    outWorker.writeObject(storeName);
                    outWorker.flush();

                    outWorker.writeObject(ProductName);
                    outWorker.flush();

                    // Receive from worker
                    String response = (String) inWorker.readObject();

                    // Send to manager
                    out.writeObject(response);
                    out.flush();

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (inWorker != null) inWorker.close();
                        if (outWorker != null) outWorker.close();
                        if (workerSocket != null) workerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }else if (role.equals("findProduct2")) {
                // Receive from manager
                String storeName = (String) in.readObject();
                String ProductName = (String) in.readObject();

                Socket workerSocket = null;
                ObjectOutputStream outWorker = null;
                ObjectInputStream inWorker = null;

                try {
                    // Select worker using hash of store name
                    int workerId = Math.abs(storeName.hashCode()) % workers.length;
                    String workerIP = workers[workerId][0];
                    int workerPort = Integer.parseInt(workers[workerId][1]);

                    // Connect to selected worker
                    workerSocket = new Socket(workerIP, workerPort);
                    outWorker = new ObjectOutputStream(workerSocket.getOutputStream());
                    inWorker = new ObjectInputStream(workerSocket.getInputStream());

                    // Send to worker
                    outWorker.writeObject("findProduct2");
                    outWorker.flush();

                    outWorker.writeObject(storeName);
                    outWorker.flush();

                    outWorker.writeObject(ProductName);
                    outWorker.flush();

                    // Receive from worker
                    String response = (String) inWorker.readObject();

                    // Send to manager
                    out.writeObject(response);
                    out.flush();

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (inWorker != null) inWorker.close();
                        if (outWorker != null) outWorker.close();
                        if (workerSocket != null) workerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }else if (role.equals("AmountInc")) {
                // Receive from manager
                String storeName = (String) in.readObject();
                String ProductName = (String) in.readObject();
                int amount = (int) in.readInt();

                Socket workerSocket = null;
                ObjectOutputStream outWorker = null;
                ObjectInputStream inWorker = null;

                try {
                    // Select worker using hash of store name
                    int workerId = Math.abs(storeName.hashCode()) % workers.length;
                    String workerIP = workers[workerId][0];
                    int workerPort = Integer.parseInt(workers[workerId][1]);

                    // Connect to selected worker
                    workerSocket = new Socket(workerIP, workerPort);
                    outWorker = new ObjectOutputStream(workerSocket.getOutputStream());
                    inWorker = new ObjectInputStream(workerSocket.getInputStream());

                    // Send to worker
                    outWorker.writeObject("AmountInc");
                    outWorker.flush();

                    outWorker.writeObject(storeName);
                    outWorker.flush();

                    outWorker.writeObject(ProductName);
                    outWorker.flush();

                    outWorker.writeInt(amount);
                    outWorker.flush();

                    // Receive from worker
                    String response = (String) inWorker.readObject();

                    // Send to manager
                    out.writeObject(response);
                    out.flush();

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (inWorker != null) inWorker.close();
                        if (outWorker != null) outWorker.close();
                        if (workerSocket != null) workerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }else if (role.equals("NewProduct")) {
                // Receive from manager
                String storeName = (String) in.readObject();
                Product pro = (Product) in.readObject();

                Socket workerSocket = null;
                ObjectOutputStream outWorker = null;
                ObjectInputStream inWorker = null;

                try {
                    // Select worker using hash of store name
                    int workerId = Math.abs(storeName.hashCode()) % workers.length;
                    String workerIP = workers[workerId][0];
                    int workerPort = Integer.parseInt(workers[workerId][1]);

                    // Connect to selected worker
                    workerSocket = new Socket(workerIP, workerPort);
                    outWorker = new ObjectOutputStream(workerSocket.getOutputStream());
                    inWorker = new ObjectInputStream(workerSocket.getInputStream());

                    // Send to worker
                    outWorker.writeObject("NewProduct");
                    outWorker.flush();

                    outWorker.writeObject(storeName);
                    outWorker.flush();

                    outWorker.writeObject(pro);
                    outWorker.flush();

                    // Receive from worker
                    String response = (String) inWorker.readObject();

                    // Send to manager
                    out.writeObject(response);
                    out.flush();

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (inWorker != null) inWorker.close();
                        if (outWorker != null) outWorker.close();
                        if (workerSocket != null) workerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }else if (role.equals("remove")) {
                // Receive from manager
                String storeName = (String) in.readObject();
                String productName = (String) in.readObject();

                Socket workerSocket = null;
                ObjectOutputStream outWorker = null;
                ObjectInputStream inWorker = null;

                try {
                    // Select worker using hash of store name
                    int workerId = Math.abs(storeName.hashCode()) % workers.length;
                    String workerIP = workers[workerId][0];
                    int workerPort = Integer.parseInt(workers[workerId][1]);

                    // Connect to selected worker
                    workerSocket = new Socket(workerIP, workerPort);
                    outWorker = new ObjectOutputStream(workerSocket.getOutputStream());
                    inWorker = new ObjectInputStream(workerSocket.getInputStream());

                    // Send to worker
                    outWorker.writeObject("remove");
                    outWorker.flush();

                    outWorker.writeObject(storeName);
                    outWorker.flush();

                    outWorker.writeObject(productName);
                    outWorker.flush();

                    // Receive from worker
                    String response = (String) inWorker.readObject();

                    // Send to manager
                    out.writeObject(response);
                    out.flush();

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (inWorker != null) inWorker.close();
                        if (outWorker != null) outWorker.close();
                        if (workerSocket != null) workerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }else if (role.equals("AmountDec")) {
                // Receive from manager
                String storeName = (String) in.readObject();
                String ProductName = (String) in.readObject();
                int amount = (int) in.readInt();

                Socket workerSocket = null;
                ObjectOutputStream outWorker = null;
                ObjectInputStream inWorker = null;

                try {
                    // Select worker using hash of store name
                    int workerId = Math.abs(storeName.hashCode()) % workers.length;
                    String workerIP = workers[workerId][0];
                    int workerPort = Integer.parseInt(workers[workerId][1]);

                    // Connect to selected worker
                    workerSocket = new Socket(workerIP, workerPort);
                    outWorker = new ObjectOutputStream(workerSocket.getOutputStream());
                    inWorker = new ObjectInputStream(workerSocket.getInputStream());

                    // Send to worker
                    outWorker.writeObject("AmountDec");
                    outWorker.flush();

                    outWorker.writeObject(storeName);
                    outWorker.flush();

                    outWorker.writeObject(ProductName);
                    outWorker.flush();

                    outWorker.writeInt(amount);
                    outWorker.flush();

                    // Receive from worker
                    String response = (String) inWorker.readObject();

                    // Send to manager
                    out.writeObject(response);
                    out.flush();

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (inWorker != null) inWorker.close();
                        if (outWorker != null) outWorker.close();
                        if (workerSocket != null) workerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }else if (role.equals("storeType")) {
                // Receive from manager
                String storeType = (String) in.readObject();  // e.g., "pizzeria"

                ArrayList<Map<String, Integer>> allResults = new ArrayList<>();

                for (int i = 0; i < workers.length; i++) { // for all the workers
                    Socket workerSocket = null;
                    ObjectOutputStream outWorker = null;
                    ObjectInputStream inWorker = null;

                    try {
                        String workerIP = workers[i][0];
                        int workerPort = Integer.parseInt(workers[i][1]);

                        // Connect to the worker
                        workerSocket = new Socket(workerIP, workerPort);
                        outWorker = new ObjectOutputStream(workerSocket.getOutputStream());
                        inWorker = new ObjectInputStream(workerSocket.getInputStream());

                        // Send to worker
                        outWorker.writeObject("storeType");
                        outWorker.flush();

                        outWorker.writeObject(storeType);
                        outWorker.flush();

                        // Receive from worker
                        Map<String, Integer> partial = (Map<String, Integer>) inWorker.readObject();
                        allResults.add(partial);

                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (inWorker != null) inWorker.close();
                            if (outWorker != null) outWorker.close();
                            if (workerSocket != null) workerSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Connect to reducer
                Socket reducerSocket = new Socket("127.0.0.1", 4325);
                ObjectOutputStream outReducer = new ObjectOutputStream(reducerSocket.getOutputStream());
                ObjectInputStream inReducer = new ObjectInputStream(reducerSocket.getInputStream());

                // Send to reducer
                outReducer.writeObject("storeType");
                outReducer.flush();

                outReducer.writeObject(workers.length); // how many workers exists
                outReducer.flush();

                for (Map<String, Integer> partial : allResults) {
                    outReducer.writeObject(partial); // sends all the partial results to the reducer
                    outReducer.flush();
                }

                // Read from reducer
                Map<String, Integer> finalResult = (Map<String, Integer>) inReducer.readObject();

                // Send to manager
                out.writeObject(finalResult);
                out.flush();

                outReducer.close();
                inReducer.close();
                reducerSocket.close();


            }else if (role.equals("productCategory")) {
                // Read from manager
                String productCategory = (String) in.readObject(); // e.g., "pizza"

                ArrayList<Map<String, Integer>> allResults = new ArrayList<>();

                for (int i = 0; i < workers.length; i++) { // for all workers
                    Socket workerSocket = null;
                    ObjectOutputStream outWorker = null;
                    ObjectInputStream inWorker = null;

                    try {
                        String workerIP = workers[i][0];
                        int workerPort = Integer.parseInt(workers[i][1]);

                        // Connect to the worker
                        workerSocket = new Socket(workerIP, workerPort);
                        outWorker = new ObjectOutputStream(workerSocket.getOutputStream());
                        inWorker = new ObjectInputStream(workerSocket.getInputStream());

                        // Send to worker
                        outWorker.writeObject("productCategory");
                        outWorker.flush();

                        outWorker.writeObject(productCategory);
                        outWorker.flush();

                        // Receive from worker
                        Map<String, Integer> partial = (Map<String, Integer>) inWorker.readObject();
                        allResults.add(partial);

                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (inWorker != null) inWorker.close();
                            if (outWorker != null) outWorker.close();
                            if (workerSocket != null) workerSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Connect to reducer
                Socket reducerSocket = new Socket("127.0.0.1", 4325);
                ObjectOutputStream outReducer = new ObjectOutputStream(reducerSocket.getOutputStream());
                ObjectInputStream inReducer = new ObjectInputStream(reducerSocket.getInputStream());

                // Send to reducer
                outReducer.writeObject("productCategory");
                outReducer.flush();

                outReducer.writeObject(workers.length); // how many workers exists
                outReducer.flush();

                for (Map<String, Integer> partial : allResults) {
                    outReducer.writeObject(partial); // sends all the partial results to the reducer
                    outReducer.flush();
                }

                // Receive from reducer
                Map<String, Integer> finalResult = (Map<String, Integer>) inReducer.readObject();

                // Send to manager
                out.writeObject(finalResult);
                out.flush();

                outReducer.close();
                inReducer.close();
                reducerSocket.close();

            }else if (role.equals("client")) {

                String responseId = null;

                // Receive from client
                String clientId = (String) in.readObject();

                MapReduceRequest request = (MapReduceRequest) in.readObject();

                Map<String, ArrayList<Store>> allResults = new HashMap<>();
                for (int i = 0; i < workers.length; i++) {
                    String workerId = "worker"; //just a random string
                    allResults.put(workerId, null);
                }

                for (int i = 0; i < workers.length; i++) { // for all workers
                    Socket workerSocket = null;
                    ObjectOutputStream outWorker = null;
                    ObjectInputStream inWorker = null;

                    try {
                        String workerIP = workers[i][0];
                        int workerPort = Integer.parseInt(workers[i][1]);

                        // Connect to the worker
                        workerSocket = new Socket(workerIP, workerPort);
                        outWorker = new ObjectOutputStream(workerSocket.getOutputStream());
                        inWorker = new ObjectInputStream(workerSocket.getInputStream());

                        // Send to worker
                        outWorker.writeObject("client");
                        outWorker.flush();

                        outWorker.writeObject(clientId);
                        outWorker.flush();

                        outWorker.writeObject(request);
                        outWorker.flush();

                        // Receive from worker
                        responseId = (String) inWorker.readObject();
                        ArrayList<Store> partialResult = (ArrayList<Store>) inWorker.readObject();
                        allResults.put(responseId,partialResult);


                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (inWorker != null) inWorker.close();
                            if (outWorker != null) outWorker.close();
                            if (workerSocket != null) workerSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }


                }
                System.out.println(allResults);
                Socket reducerSocket = null;
                ObjectOutputStream outReducer = null;
                ObjectInputStream inReducer = null;

                try {
                    // Connect to reducer
                    reducerSocket = new Socket("127.0.0.1", 4325);
                    outReducer = new ObjectOutputStream(reducerSocket.getOutputStream());
                    inReducer = new ObjectInputStream(reducerSocket.getInputStream());

                    // Send to reducer
                    outReducer.writeObject("client");
                    outReducer.flush();

                    outReducer.writeObject(clientId);
                    outReducer.flush();

                    outReducer.writeObject(workers.length); // how many workers exists
                    outReducer.flush();

                    for(Map.Entry<String, ArrayList<Store>> partial : allResults.entrySet()){ // sends all the partial results to the reducer
                        outReducer.writeObject(partial.getKey());
                        outReducer.flush();

                        outReducer.writeObject(partial.getValue());
                        outReducer.flush();
                    }

                    // Receive from reducer
                    clientId = (String) inReducer.readObject();
                    ArrayList<Store> finalResult = (ArrayList<Store>) inReducer.readObject();

                    // Send to client
                    out.writeObject(clientId);
                    out.flush();

                    out.writeObject(finalResult);
                    out.flush();

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (inReducer != null) inReducer.close();
                        if (outReducer != null) outReducer.close();
                        if (reducerSocket != null) reducerSocket.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }

            }else if (role.equals("filter")) {

                String responseId = null;

                // Receive from client
                String clientId = (String) in.readObject();

                MapReduceRequest request = (MapReduceRequest) in.readObject();

                Map<String, ArrayList<Store>> allResults = new HashMap<>();
                for (int i = 0; i < workers.length; i++) {
                    String workerId = "worker"; //just a random string
                    allResults.put(workerId, null);
                }

                for (int i = 0; i < workers.length; i++) { // for all workers
                    Socket workerSocket = null;
                    ObjectOutputStream outWorker = null;
                    ObjectInputStream inWorker = null;

                    try {
                        String workerIP = workers[i][0];
                        int workerPort = Integer.parseInt(workers[i][1]);

                        // Connect to the worker
                        workerSocket = new Socket(workerIP, workerPort);
                        outWorker = new ObjectOutputStream(workerSocket.getOutputStream());
                        inWorker = new ObjectInputStream(workerSocket.getInputStream());

                        // Send to worker
                        outWorker.writeObject("filter");
                        outWorker.flush();

                        outWorker.writeObject(clientId);
                        outWorker.flush();


                        outWorker.writeObject(request);
                        outWorker.flush();

                        // Receive from worker
                        responseId = (String) inWorker.readObject();
                        ArrayList<Store> partial = (ArrayList<Store>) inWorker.readObject();
                        allResults.put(responseId,partial);

                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (inWorker != null) inWorker.close();
                            if (outWorker != null) outWorker.close();
                            if (workerSocket != null) workerSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                Socket reducerSocket = null;
                ObjectOutputStream outReducer = null;
                ObjectInputStream inReducer = null;

                try {
                    // Connect to reducer
                    reducerSocket = new Socket("127.0.0.1", 4325);
                    outReducer = new ObjectOutputStream(reducerSocket.getOutputStream());
                    inReducer = new ObjectInputStream(reducerSocket.getInputStream());

                    // Send to reducer
                    outReducer.writeObject("filter");
                    outReducer.flush();

                    outReducer.writeObject(clientId);
                    outReducer.flush();

                    outReducer.writeObject(workers.length); // how many workers exists
                    outReducer.flush();

                    for(Map.Entry<String, ArrayList<Store>> partial : allResults.entrySet()){ // sends all the partial results to the reducer
                        outReducer.writeObject(partial.getKey());
                        outReducer.flush();

                        outReducer.writeObject(partial.getValue());
                        outReducer.flush();
                    }

                    // Receive from reducer
                    clientId = (String) inReducer.readObject();
                    ArrayList<Store> finalResult = (ArrayList<Store>) inReducer.readObject();

                    // Send to client
                    out.writeObject(clientId);
                    out.flush();

                    out.writeObject(finalResult);
                    out.flush();

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (inReducer != null) inReducer.close();
                        if (outReducer != null) outReducer.close();
                        if (reducerSocket != null) reducerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }else if (role.equals("fetchProducts")) {

                String responseId = null;

                // Receive from client
                String clientId = (String) in.readObject();

                String store = (String) in.readObject();

                ArrayList<Product> results = new ArrayList<>();

                for (int i = 0; i < workers.length; i++) { // for all workers
                    Socket workerSocket = null;
                    ObjectOutputStream outWorker = null;
                    ObjectInputStream inWorker = null;

                    try {
                        String workerIP = workers[i][0];
                        int workerPort = Integer.parseInt(workers[i][1]);

                        // Connect to the worker
                        workerSocket = new Socket(workerIP, workerPort);
                        outWorker = new ObjectOutputStream(workerSocket.getOutputStream());
                        inWorker = new ObjectInputStream(workerSocket.getInputStream());

                        // Send to worker
                        outWorker.writeObject("fetchProducts");
                        outWorker.flush();

                        outWorker.writeObject(clientId);
                        outWorker.flush();

                        outWorker.writeObject(store);
                        outWorker.flush();

                        // Receive from worker
                        responseId = (String) inWorker.readObject();
                        results = (ArrayList<Product>) inWorker.readObject();


                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (inWorker != null) inWorker.close();
                            if (outWorker != null) outWorker.close();
                            if (workerSocket != null) workerSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Send to client
                out.writeObject(responseId);
                out.flush();

                out.writeObject(results);
                out.flush();


            }else if (role.equals("purchase")) {

                String responseId = null;

                // Receive from client
                String clientId = (String) in.readObject();

                Purchase pur = (Purchase) in.readObject();

                String name = (String) in.readObject();

                String results = null;

                for (int i = 0; i < workers.length; i++) { // for all workers
                    Socket workerSocket = null;
                    ObjectOutputStream outWorker = null;
                    ObjectInputStream inWorker = null;

                    try {
                        String workerIP = workers[i][0];
                        int workerPort = Integer.parseInt(workers[i][1]);

                        // Connect to the worker
                        workerSocket = new Socket(workerIP, workerPort);
                        outWorker = new ObjectOutputStream(workerSocket.getOutputStream());
                        inWorker = new ObjectInputStream(workerSocket.getInputStream());

                        // Send to worker
                        outWorker.writeObject("purchase");
                        outWorker.flush();

                        outWorker.writeObject(clientId);
                        outWorker.flush();

                        outWorker.writeObject(pur);
                        outWorker.flush();

                        outWorker.writeObject(name);
                        outWorker.flush();

                        // Receive from worker
                        responseId = (String) inWorker.readObject();
                        results = (String) inWorker.readObject();


                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (inWorker != null) inWorker.close();
                            if (outWorker != null) outWorker.close();
                            if (workerSocket != null) workerSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Send to client
                out.writeObject(responseId);
                out.flush();
                out.writeObject(results);
                out.flush();


            }else if (role.equals("rate")) {

                String responseId = null;

                // Receive from client
                String clientId = (String) in.readObject();
                String store = (String) in.readObject();
                int rating = (int) in.readObject();

                String results = null;

                for (int i = 0; i < workers.length; i++) { // for all workers
                    Socket workerSocket = null;
                    ObjectOutputStream outWorker = null;
                    ObjectInputStream inWorker = null;

                    try {
                        String workerIP = workers[i][0];
                        int workerPort = Integer.parseInt(workers[i][1]);

                        // Connect to the worker
                        workerSocket = new Socket(workerIP, workerPort);
                        outWorker = new ObjectOutputStream(workerSocket.getOutputStream());
                        inWorker = new ObjectInputStream(workerSocket.getInputStream());

                        // Send to the worker
                        outWorker.writeObject("rate");
                        outWorker.flush();

                        outWorker.writeObject(clientId);
                        outWorker.flush();

                        outWorker.writeObject(store);
                        outWorker.flush();

                        outWorker.writeObject(rating);
                        outWorker.flush();

                        // Receive from worker
                        responseId = (String) inWorker.readObject();
                        results = (String) inWorker.readObject();


                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (inWorker != null) inWorker.close();
                            if (outWorker != null) outWorker.close();
                            if (workerSocket != null) workerSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Send to client
                out.writeObject(responseId);
                out.flush();
                out.writeObject(results);
                out.flush();

            }else if (role.equals("customerPurchasesByStore")) {
                // Receive from client
                String customerName = (String) in.readObject();
                String storeName = (String) in.readObject();

                // Βρες σε ποιον worker ανήκει το κατάστημα
                int workerId = Math.abs(storeName.hashCode()) % workers.length;
                String workerIP = workers[workerId][0];
                int workerPort = Integer.parseInt(workers[workerId][1]);

                Socket workerSocket = null;
                ObjectOutputStream outWorker = null;
                ObjectInputStream inWorker = null;

                try {
                    // Connect to the worker
                    workerSocket = new Socket(workerIP, workerPort);
                    outWorker = new ObjectOutputStream(workerSocket.getOutputStream());
                    inWorker = new ObjectInputStream(workerSocket.getInputStream());

                    // Send to worker
                    outWorker.writeObject("customerPurchasesByStore");
                    outWorker.flush();

                    outWorker.writeObject(customerName);
                    outWorker.flush();

                    outWorker.writeObject(storeName);
                    outWorker.flush();

                    // Receive from worker
                    Map<String, Integer> result = (Map<String, Integer>) inWorker.readObject();

                    // Send to client
                    out.writeObject(result);
                    out.flush();

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (inWorker != null) inWorker.close();
                        if (outWorker != null) outWorker.close();
                        if (workerSocket != null) workerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
        }


