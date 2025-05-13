import java.io.*;
import java.net.*;


public class Reducer {
    public static void main(String[] args) throws UnknownHostException {

        // Start the Reducer server with shared data structures and locks
        new Reducer().openServer();
    }

    ServerSocket providerSocket;
    Socket connection = null;

    void openServer() {
        try {
            // Reducer listens on port 4325, with a backlog of 10
            providerSocket = new ServerSocket(4325, 10);

            while (true) {
                // Accept incoming connection from Master
                connection = providerSocket.accept();

                // Handle the connection in a new thread using ReducerActions
                Thread t = new ReducerActions(connection);
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
