import java.net.ServerSocket;
import java.net.Socket;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.nio.channels.IllegalBlockingModeException;

//import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

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
    
    /* 
     * A list of codes to be inserted in front of the message, so the Server
     * knows what to do with each message.
     */
    
    /*
     * The message structure
     * @code @param [@message]
     * @code tells the Server how to process the message
     * @param is usually the ID/name of other client
     * @message is not used in all codes
     */
     
    /** The send code - sends a message to some or all clients. */
    private static final String SEND = "@send";
    
    /** The kick code - kicks a specified client off the chat. */
    private static final String KICK = "@kick";
    
    /** 
     * A map using client names as keys and client sockets as values.
     * Contains all the currently connected clients.
     */
    private static ConcurrentHashMap<String,Socket> clientSockets = 
        new ConcurrentHashMap<String,Socket>();
        
    /**
     * A map using client names as keys and threads as values.
     * Contains all a thread per connected client.
     */
    private static ConcurrentHashMap<String,Thread> clientThreads =
        new ConcurrentHashMap<String,Thread>();
    
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
            ConnectionHandler myHandler = new ConnectionHandler(clientSocket);
            Thread thread = new Thread(myHandler);
            myHandler.setThread(thread);
            thread.start();
        }
        
    }
    
    /**************************************************************************
     * Private class handles connections from clients.
     *************************************************************************/
    private static class ConnectionHandler implements Runnable {
        
        /** The client socket whose connection this class is handling */
        private Socket clientSocket;
        
        /** The thread on which this connection is running */
        private Thread thread;
        
        /**********************************************************************
         * Constructs a ConnectionHandler class with the given client socket.
         *********************************************************************/
        public ConnectionHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }
        
        /**********************************************************************
         * Sets the value of this thread variable.
         *********************************************************************/
        public void setThread(Thread thread) {
            this.thread = thread;
        }
        
        @Override
        public void run() {
            
            BufferedReader input = null;
            try {
                input = new BufferedReader(new InputStreamReader(
                    clientSocket.getInputStream()));
            } catch (IOException e) {
                System.err.println("Couldn't create a reader for client "
                    + "socket.");
                e.printStackTrace();
                System.exit(1);
            }
            
            String clientName = "";
            try {
                clientName = input.readLine();
            } catch (IOException e) {
                System.err.println("Could not read from client socket.");
                e.printStackTrace();
                System.exit(1);
            }
            
            System.out.println("Client " + clientName + " connected to the "
                + "server.");
            
            clientThreads.put(clientName, thread);
            clientSockets.put(clientName, clientSocket);
            
            while (true) {
                String message = "";
                try {
                    message = input.readLine();
                } catch (IOException e) {
                    System.err.println("Could not read from client socket.");
                    e.printStackTrace();
                    System.exit(1);
                }
                System.out.println(message);
            }
            
        }
        
    }
    
}