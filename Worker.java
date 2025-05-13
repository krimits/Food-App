import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Worker {

    public static void main(String[] args) throws UnknownHostException{
        // Read the port number from command-line arguments
        int port = Integer.parseInt(args[0]);

        // Shared list to store all Store objects assigned to this Worker
        ArrayList<Store> stores = new ArrayList<>();

        // Lock object used for synchronizing access to the stores list
        Object lock = new Object();

        // Start the Worker server on the given port
        new Worker().openServer(port, stores, lock);
    }

    ServerSocket providerSocket;
    Socket connection = null;

    // Opens a server socket for this worker to handle incoming connections
    void openServer(int port, ArrayList<Store> stores, Object lock) {
        try {
            // Listen on the specified port with a backlog of 10 connections
            providerSocket = new ServerSocket(port, 10);

            while (true) {
                // Accept an incoming connection
                connection = providerSocket.accept();

                // Create and start a new thread to handle the connection
                Thread t = new WorkerActions(connection, stores, lock);
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