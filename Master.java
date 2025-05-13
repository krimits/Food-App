import java.io.*;
import java.net.*;
import java.util.*;


public class Master {

    public static void main(String[] args) throws UnknownHostException {

        // Create array to store IP and port pairs of workers
        String[][] workers = new String [args.length/2][2];

        // HashMap to store output streams to worker connections
        HashMap<Integer, ObjectOutputStream> connectionsOut = new HashMap<>();

        // Populate the workers array from command-line arguments
        for (int i = 0; i < args.length/2; i++) {
            workers[i][0] = args[i*2]; // IP
            workers[i][1] = args[i*2 + 1]; // Port
        }

        // Start the Master server with worker info and output connections map
        new Master().openServer(workers, connectionsOut);
    }

    ServerSocket providerSocket;
    Socket connection = null;
    int counterID = 0; // Counter of incoming connections

    void openServer(String [][] workers, HashMap<Integer, ObjectOutputStream> connectionsOut) {
        try {
            // Create a server socket listening on port 4321, with a backlog of 10
            providerSocket = new ServerSocket(4321, 10);

            while (true) {
                // Accept incoming client connection
                connection = providerSocket.accept();
                counterID++;

                // Create a new thread to handle this connection
                Thread t = new Actions(connection, workers, connectionsOut, counterID);
                t.start();

            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                providerSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}



