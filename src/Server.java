import java.net.ServerSocket;
import java.net.Socket;

import java.io.IOException;

import java.nio.channels.IllegalBlockingModeException;

/******************************************************************************
 * A simple chat server.
 * Accepts connections from multiple clients.
 * Sends encrypted packets to other clients.
 * Supports some administrative actions (kicking other users).
 * Uses the port # 48700 and sets up on whatever machine is running it.
 * @author Frank Derry Wanye
 * @author Gloire Rubambiza
 * @since 10/27/2016
 *****************************************************************************/
public class Server {
    
    /**
     * TO-DO
     * 
     * - Allow multiple clients
     * - Must send list of clients to all other clients
     * - Support sending to individual clients
     * - Support sending to all clients
     * - Support Administrative commands
     *   - Kick another user
     *   - More administrative commands?
     * 
     */
     
    /** The port number used by the server */
    private static final int port = 48700;
    
    /** The Server's IP address */
    //private static final String ipAddress = "127.0.0.1";
    
    public static void main(String[] args) {
        
        // Create the server's socket
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Couldn't create server socket.");
            e.printStackTrace();
            System.exit(1);
        } catch (SecurityException e) {
            System.err.println("Couldn't create server socket.");
            e.printStackTrace();
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.err.println("Couldn't create server socket.");
            e.printStackTrace();
            System.exit(1);
        }
        
        // Accepts client connections
        while (true) {
            System.out.println("Waiting for a client to connect.");
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Couldn't accept client socket.");
                e.printStackTrace();
                System.exit(1);
            } catch (SecurityException e) {
                System.err.println("Couldn't accept client socket.");
                e.printStackTrace();
                System.exit(1);
            } catch (IllegalArgumentException e) {
                System.err.println("Couldn't accept client socket.");
                e.printStackTrace();
                System.exit(1);
            } catch (IllegalBlockingModeException e) {
                System.err.println("Couldn't accept client socket.");
                e.printStackTrace();
                System.exit(1);
            }
            Thread thread = new Thread(new ConnectionHandler(
                clientSocket));
            thread.start();
            System.out.println("New client connected to server.");
        }
        
    }
    
    /**************************************************************************
     * Private class handles connections from clients.
     *************************************************************************/
    private static class ConnectionHandler implements Runnable {
        
        /** The client socket whose connection this class is handling */
        private Socket clientSocket;
        
        /**********************************************************************
         * Constructs a ConnectionHandler class with the given client socket.
         *********************************************************************/
        public ConnectionHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }
        
        @Override
        public void run() {
            // Implement Later
        }
        
    }
    
}