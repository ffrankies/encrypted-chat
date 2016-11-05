import java.net.ServerSocket;
import java.net.Socket;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.DataOutputStream;

import java.nio.channels.IllegalBlockingModeException;

//import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Enumeration;

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
         
    /** The broadcast code - sends a message to all clients. */
    private static final String BROADCAST = "@broadcast";
     
    /** The send code - sends a message to some or all clients. */
    private static final String SEND = "@send";
    
    /** The kick code - kicks a specified client off the chat. */
    private static final String KICK = "@kick";
    
    /** The client list code - sends list of clients to all clients. */
    private static final String CLIENTLIST = "@clientlist";
    
    /** The exit code - tells the server that a client is disconnecting. */
    private static final String EXIT = "@exit";
    
    /**
     * A map using client names as keys and client sockets as values.
     * Contains all currently connected clients.
     */
    private static ConcurrentHashMap<String,Socket> clientSockets = 
        new ConcurrentHashMap<String,Socket>();
    
    /** 
     * A map using client names as keys and client output streams as values.
     * Contains all the currently connected clients.
     */
    private static ConcurrentHashMap<String,DataOutputStream> clientOutputs = 
        new ConcurrentHashMap<String,DataOutputStream>();
        
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
        
        /**********************************************************************
         * Sends a list of all currently connected clients to all Clients.
         *********************************************************************/
        private void sendClientList() {
            
            // Get all currently connected client names
            String clientList = "";
            for (Enumeration<String> clients = clientOutputs.keys(); 
                 clients.hasMoreElements(); ) {
                clientList += clients.nextElement() + ",";         
            }
            clientList = CLIENTLIST + " " + clientList;
            
            for (Enumeration<DataOutputStream> outputs = 
                 clientOutputs.elements(); outputs.hasMoreElements(); ) {
                DataOutputStream thisOutput = outputs.nextElement();
                try {
                    thisOutput.writeBytes(clientList + "\n");
                } catch (IOException e) {
                    System.err.println("Could not send client list to client.");
                    e.printStackTrace();
                } 
            }
            
            System.out.println("Sent clientList to all clients.");
            
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
            
            DataOutputStream output = null;
            try {
                output = new DataOutputStream(clientSocket.getOutputStream());
            } catch (IOException e) {
                System.err.println("Couldn't create output strea for client "
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
            clientOutputs.put(clientName, output);
            clientSockets.put(clientName, clientSocket);
            
            sendClientList();
            
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
                String command = message.substring(0, message.indexOf(" "));
                if (command.equals(BROADCAST)) {
                    for (Enumeration<DataOutputStream> outputs = 
                        clientOutputs.elements(); outputs.hasMoreElements(); ) {
                        DataOutputStream thisOutput = outputs.nextElement();
                        if (!output.equals(thisOutput)) {
                            System.out.println("Sending message.");
                            try {
                                thisOutput.writeBytes(message + "\n");
                            } catch (IOException e) {
                                System.err.println("Could not send data to "
                                    + "client.");
                                e.printStackTrace();
                            }
                        }
                    }
                    System.out.println("Done looping through enumeration.");
                } else if (command.equals(SEND)) {
                    // Cut out SEND code
                    message = message.substring(message.indexOf(" ") + 1);
                    String destination = message.substring(
                        0, message.indexOf(" "));
                    // Cut out DESTINATION name
                    message = message.substring(message.indexOf(" ") + 1);
                    // Reinsert SEND code
                    message = SEND + " " + message + "\n";
                    DataOutputStream thisOutput = 
                        clientOutputs.get(destination.substring(1));
                    try {
                        thisOutput.writeBytes(message);
                    } catch (IOException e) {
                        System.err.println("Could not send data to " + 
                            destination);
                        e.printStackTrace();
                    }
                } else if (command.equals(KICK)) {
                    // Take kick command out of message
                    message = message.substring(message.indexOf(" ") + 1);
                    String[] clients = message.split(",");
                    for(int i = 0; i < clients.length; ++i){
                        DataOutputStream thisOutput = clientOutputs.get(
                            clients[i]);
                        Thread thisThread = clientThreads.get(clients[i]);
                        Socket thisSocket = clientSockets.get(clients[i]);
                        try{
                            thisThread.interrupt();
                            thisSocket.close();
                            thisOutput.close();
                            sendClientList();
                        }
                        catch (SecurityException e ){
                            System.err.println("Could not close the thread");
                            e.printStackTrace();
                        }
                        catch(IOException e){
                            System.err.println("Could not close"+ 
                            " DataOutputStream");
                            e.printStackTrace();
                        }
                    }
                    
                } else if (command.equals(EXIT)) {
                    message = SEND + " " + clientName + " has disconnected.\n";
                    for (Enumeration<DataOutputStream> outputs = 
                        clientOutputs.elements(); outputs.hasMoreElements(); ) {
                        DataOutputStream thisOutput = outputs.nextElement();
                        if (!output.equals(thisOutput)) {
                            System.out.println("Sending exit notice.");
                            try {
                                thisOutput.writeBytes(message);
                            } catch (IOException e) {
                                System.err.println("Could not send exit notice"
                                    + " to client.");
                                e.printStackTrace();
                            }
                        }
                    }
                    try {
                        clientSocket.close();
                    } catch (IOException e) {
                        System.err.println("Couldn't close client socket.");
                        e.printStackTrace();
                    }
                    clientOutputs.remove(clientName);
                    clientThreads.remove(clientName);
                    // Completes while loop and ends this thread
                    break;
                } // if statement
                
            }  // while loop 
            
            System.out.println("Client has exited gracefully: " + clientName);
        }
        
    }
    
}