import javax.swing.JFrame;

import java.util.ArrayList;
import java.util.List;

import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

/******************************************************************************
 * A Client in an encrypted chat program.
 * @author Frank Wanye
 * @author Gloire Rubambiza
 * @since 10/26/2016
 *****************************************************************************/
public class Client {
    
    /**
     * TO-DO
     * 
     * - Connect to Server
     * - Must request and receive list of clients to all other clients
     * - Support sending to individual clients
     * - Support sending to all clients
     * - Support sending Administrative commands
     *   - Kick another user
     *   - More administrative commands?
     * - Have a GUI for all this to be done
     * - Should have a name
     * 
     */
    
    /** The name of this Client. */
    private String name;
    
    /** The IP of hte server to which the Client connects. */
    private String serverIP;
    
    /** The socket used by this Cleint. */
    private Socket socket;
    
    /** The port number used by the server */
    private static final int port = 48700;
    
    /** The names of all the other connected Clients. */
    private List<String> otherClients = new ArrayList<String>();
    
    /* 
     * A list of codes to be inserted in front of the message, so the Server
     * knows what to do with each message.
     */
    
    /*
     * The message structure
     * @code @param(s) [@message]
     * @code tells the Server how to process the message
     * @param is usually the ID/name of other client
     * @message is not used in all codes
     */
     
    /** The broadcast code - sends a message to all clients. */
    private static final String BROADCAST = "@broadcast ";
    
    /** The send code - sends a message to some or one client(s). */
    private static final String SEND = "@send ";
    
    /** The kick code - kicks a specified client off the chat. */
    private static final String KICK = "@kick ";
    
    /** The client list code - sends list of clients to all clients. */
    private static final String CLIENTLIST = "@clientlist";
        
    /** The exit code - tells the server that a client is disconnecting. */
    private static final String EXIT = "@exit ";
    
    /** Reads data from the server. */
    private  BufferedReader input;
    
    /** Sends data to the server. */
    private  DataOutputStream output;
    
    /**************************************************************************
     * Instantiates a Client object connecting to a particular IP and having
     * a given name.
     * @param name is the name of the client.
     * @param serverIP is the IP address of the server to which this client
     * connects.
     *************************************************************************/
    public Client(String name, String serverIP) {
        
        this.name = name;
        this.serverIP = serverIP;
        
        try {
            socket = new Socket(InetAddress.getByName(serverIP), port);
        } catch (UnknownHostException e) {
            System.err.println("Could not resolve server IP to a host.");
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Could not resolve server IP to a host.");
            e.printStackTrace();
            System.exit(1);
        }
        
        System.out.println("Client is connected to the host.");
        
        try {
            input = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
        } catch (IOException e) {
            System.err.println("Couldn't create a reader for client "
                + "socket.");
            e.printStackTrace();
            System.exit(1);
        }
        
        try {
            output = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.err.println("Couldn't create a writer to server "
                + "socket.");
            e.printStackTrace();
            System.exit(1);
        }
        
    }
    
    /**************************************************************************
     * Sends a message to one other Client.
     * @param message is the message sent to the Client
     * @param otherClient is the name of the other Client receiving the message
     *************************************************************************/
    public void sendMessage(String message, String otherClient) {
        
        /*
         * Message format:
         * @send @DestionationName/ID @SenderName/ID message \n
         */
        try {
            output.writeBytes(SEND + "@" + otherClient + " @" + name + " " + 
                message + "\n");
        } catch (IOException e) {
            System.err.println("Could not send message to server.");
            e.printStackTrace();
            return;
        }
        
    }
    
    /**************************************************************************
     * Sends a message to all other Client
     * @param message is the message sent to the other Clients
     *************************************************************************/
    public void sendMessage(String message) {
        
        /*
         * Message format:
         * @broadcast @SenderName/ID message \n
         */
        try {
            output.writeBytes(BROADCAST + "@" + name + " " + message + "\n");
        } catch (IOException e) {
            System.err.println("Could not send message to server.");
            e.printStackTrace();
            return;
        }
        
    }
    
    /**************************************************************************
     * Sends a command to the Server.
     * @param command is the name of the command to be sent to the server
     *************************************************************************/
    public void sendKick( String users) {
        
        try{
            output.writeBytes(KICK + users + "\n");
            
        } catch (IOException e){
            System.err.println("Could not send kick message to server.");
            e.printStackTrace();
            return;
        }
        
    }
    
    /**************************************************************************
     * Alerts the server that this Client is disconnecting.
     *************************************************************************/
    public void alertExit() {
        System.out.println("Alerting server of exit");
        try {
            output.writeBytes(EXIT + "@" + name);
        } catch (IOException e) {
            System.err.println("Could not send Exit alert to the server.");
            e.printStackTrace();
            return;
        }
        System.out.println("Done alerting server of exit");
    }
    
    /**************************************************************************
     * Sends out the name of this Client.
     *************************************************************************/
    public void sendName() {
        try {
            output.writeBytes(name + "\n");
        } catch (IOException e) {
            System.err.println("Could not send Client name to the server.");
            e.printStackTrace();
            return;
        }
    }
    
    /**************************************************************************
     * Receives a message sent from the server/other Client.
     * @return a String containing the message sent
     *************************************************************************/
    public String receiveMessage() {
        String message = "";
        try {
            System.out.println("Waiting for incoming message.");
            message = input.readLine();
            System.out.println("Received message: " + message);
        } catch (IOException e) {
            System.err.println("Could not get message from Server/other "
                + "Client.");
            e.printStackTrace();
        }
        String code = message.substring(0, message.indexOf(" "));
        if (code.equals(CLIENTLIST)) {
            processClientList(message.substring(message.indexOf(" ") + 1));
            return "";
        }
        return message.substring(message.indexOf(" ") + 1);
    }
    
    /**************************************************************************
     * Processes a message containing list of clients
     *************************************************************************/
    private void processClientList(String message) {
        String[] clients = message.split(",");
        for (int i = 0; i < clients.length; ++i) {
            if (!otherClients.contains(clients[i]) 
                && !clients[i].equals(name)) {
                otherClients.add(clients[i]);
            }
        }
    }
    
    /**************************************************************************
     * Returns a List containing the names of the other connected clients.
     * @return a List<String> where each member is another connected client.
     *************************************************************************/
    public List<String> getOtherClients() {
        return this.otherClients;
    }
    
    /**************************************************************************
     * Closes the connection between client and server.
     *************************************************************************/
    public void closeConnection() {
        System.out.println("Closing the input and output.");
        try {
            input.close();
            output.flush();
            output.close();
        } catch (IOException e) {
            System.err.println("Couldn't close input and output streams.");
            e.printStackTrace();
        }
    }
    
}