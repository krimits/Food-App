import java.io.*;
import java.net.*;
import java.text.ParseException;
import java.util.*;

public class Actions extends Thread {
    ObjectInputStream in;
    ObjectOutputStream out;
    String[][] workers; // Stores IP and port info for worker nodes
    //THE HASHMAP IS UNUSED
    HashMap<Integer, ObjectOutputStream> connectionsOut; // Map for output streams to different connections
    int counterID;

    public Actions(Socket connection, String[][] workers, HashMap<Integer, ObjectOutputStream> connectionsOut, int counterID) {
        try {
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
            this.workers = workers;
            this.connectionsOut = connectionsOut;
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

                        if ("Store(s) added successfully".equals(response)) {
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
                // REceive from manager
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
                // Receive from client
                MapReduceRequest request = (MapReduceRequest) in.readObject();

                ArrayList<ArrayList<Store>> allResults = new ArrayList<>();

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

                        outWorker.writeObject(request);
                        outWorker.flush();

                        // Receive from worker
                        ArrayList<Store> partialResult = (ArrayList<Store>) inWorker.readObject();
                        allResults.add(partialResult);

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
                    reducerSocket = new Socket("127.0.0.1", 4325);  // υποθέτουμε ότι η πόρτα του Reducer είναι 4325
                    outReducer = new ObjectOutputStream(reducerSocket.getOutputStream());
                    inReducer = new ObjectInputStream(reducerSocket.getInputStream());

                    // Send to reducer
                    outReducer.writeObject("client");
                    outReducer.flush();

                    outReducer.writeObject(workers.length); // how many workers exists
                    outReducer.flush();

                    for (ArrayList<Store> partial : allResults) {
                        outReducer.writeObject(partial); // sends all the partial results to the reducer
                        outReducer.flush();
                    }

                    // Receive from reducer
                    ArrayList<Store> finalResult = (ArrayList<Store>) inReducer.readObject();

                    // Send to client
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
                // Receive from client
                MapReduceRequest request = (MapReduceRequest) in.readObject();

                ArrayList<ArrayList<Store>> allResults = new ArrayList<>();

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

                        outWorker.writeObject(request);
                        outWorker.flush();

                        // Receive from worker
                        ArrayList<Store> partial = (ArrayList<Store>) inWorker.readObject();
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

                    outReducer.writeObject(workers.length); // how many workers exists
                    outReducer.flush();

                    for (ArrayList<Store> partial : allResults) {
                        outReducer.writeObject(partial); // sends all the partial results to the reducer
                        outReducer.flush();
                    }

                    // Receive from reducer
                    ArrayList<Store> finalResult = (ArrayList<Store>) inReducer.readObject();

                    // Send to client
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
                // Receive from client
                String store = (String) in.readObject();

                ArrayList<Product> results = new ArrayList<>();

                // Βρες τον worker που διαχειρίζεται το κατάστημα
                int workerId = Math.abs(store.hashCode()) % workers.length;
                String workerIP = workers[workerId][0];
                int workerPort = Integer.parseInt(workers[workerId][1]);
                Socket workerSocket = null;
                ObjectOutputStream outWorker = null;
                ObjectInputStream inWorker = null;
                try {
                    workerSocket = new Socket(workerIP, workerPort);
                    outWorker = new ObjectOutputStream(workerSocket.getOutputStream());
                    inWorker = new ObjectInputStream(workerSocket.getInputStream());

                    // Send to worker
                    outWorker.writeObject("fetchProducts");
                    outWorker.flush();

                    outWorker.writeObject(store);
                    outWorker.flush();

                    // Receive from worker
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

                // Send to client
                out.writeObject(results);
                out.flush();

            }else if (role.equals("purchase")) {
                // Receive from client
                Purchase pur = (Purchase) in.readObject();
                String name = (String) in.readObject();

                String results = null;

                // Βρες τον worker που διαχειρίζεται το κατάστημα
                int workerId = Math.abs(name.hashCode()) % workers.length;
                String workerIP = workers[workerId][0];
                int workerPort = Integer.parseInt(workers[workerId][1]);
                Socket workerSocket = null;
                ObjectOutputStream outWorker = null;
                ObjectInputStream inWorker = null;
                try {
                    workerSocket = new Socket(workerIP, workerPort);
                    outWorker = new ObjectOutputStream(workerSocket.getOutputStream());
                    inWorker = new ObjectInputStream(workerSocket.getInputStream());

                    // Send to worker
                    outWorker.writeObject("purchase");
                    outWorker.flush();

                    outWorker.writeObject(pur);
                    outWorker.flush();

                    outWorker.writeObject(name);
                    outWorker.flush();

                    // Receive from worker
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

                // Send to client
                out.writeObject(results);
                out.flush();

            }else if (role.equals("rate")) {
                // Receive from client
                String store = (String) in.readObject();
                int rating = (int) in.readObject();

                String results = null;

                // Βρες τον worker που διαχειρίζεται το κατάστημα
                int workerId = Math.abs(store.hashCode()) % workers.length;
                String workerIP = workers[workerId][0];
                int workerPort = Integer.parseInt(workers[workerId][1]);
                Socket workerSocket = null;
                ObjectOutputStream outWorker = null;
                ObjectInputStream inWorker = null;
                try {
                    workerSocket = new Socket(workerIP, workerPort);
                    outWorker = new ObjectOutputStream(workerSocket.getOutputStream());
                    inWorker = new ObjectInputStream(workerSocket.getInputStream());

                    // Send to worker
                    outWorker.writeObject("rate");
                    outWorker.flush();

                    outWorker.writeObject(store);
                    outWorker.flush();

                    outWorker.writeObject(rating);
                    outWorker.flush();

                    // Receive from worker
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

                // Send to client
                out.writeObject(results);
                out.flush();
            // Προσθήκη για fetchLastUserPurchase
            }else if (role.equals("fetchLastUserPurchase")) {
                String email = (String) in.readObject();
                Purchase lastPurchase = null;
                Date lastDate = null;

                for (int i = 0; i < workers.length; i++) { // για κάθε worker
                    Socket workerSocket = null;
                    ObjectOutputStream outWorker = null;
                    ObjectInputStream inWorker = null;
                    try {
                        String workerIP = workers[i][0];
                        int workerPort = Integer.parseInt(workers[i][1]);
                        workerSocket = new Socket(workerIP, workerPort);
                        outWorker = new ObjectOutputStream(workerSocket.getOutputStream());
                        inWorker = new ObjectInputStream(workerSocket.getInputStream());

                        outWorker.writeObject("fetchLastUserPurchase");
                        outWorker.flush();
                        outWorker.writeObject(email);
                        outWorker.flush();

                        Purchase workerPurchase = (Purchase) inWorker.readObject();
                        if (workerPurchase != null) {
                            // Αν βρεις πιο πρόσφατη, κράτα αυτή (σύγκριση με βάση το toString ή αν έχεις ημερομηνία)
                            if (lastPurchase == null) {
                                lastPurchase = workerPurchase;
                            } else {
                                // Αν το Purchase έχει ημερομηνία, κάνε σύγκριση εδώ
                                // Διαφορετικά, κράτα το πρώτο που βρεις
                            }
                        }
                    } catch (Exception e) {
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
                out.writeObject(lastPurchase);
                out.flush();
            }else if (role.equals("customerPurchasesByStore")) {
                String customerName = (String) in.readObject();
                String storeName = (String) in.readObject();

                int workerId = Math.abs(storeName.hashCode()) % workers.length;
                String workerIP = workers[workerId][0];
                int workerPort = Integer.parseInt(workers[workerId][1]);
                Socket workerSocket = null;
                ObjectOutputStream outWorker = null;
                ObjectInputStream inWorker = null;
                try {
                    workerSocket = new Socket(workerIP, workerPort);
                    outWorker = new ObjectOutputStream(workerSocket.getOutputStream());
                    inWorker = new ObjectInputStream(workerSocket.getInputStream());

                    outWorker.writeObject("customerPurchasesByStore");
                    outWorker.flush();
                    outWorker.writeObject(customerName);
                    outWorker.flush();
                    outWorker.writeObject(storeName);
                    outWorker.flush();

                    // Receive HashMap<String, Integer> from worker
                    HashMap<String, Integer> purchases = (HashMap<String, Integer>) inWorker.readObject();

                    // Send to client
                    out.writeObject(purchases);
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
