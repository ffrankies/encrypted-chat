import javax.swing.JFrame;

import java.util.ArrayList;

import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.PrintWriter;
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
    private ArrayList<String> otherClients;
    
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
    
    /** Reads data from the server. */
    private  BufferedReader input;
    
    /** Sends data to the server. */
    private  PrintWriter output;
    
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
            output = new PrintWriter(socket.getOutputStream());
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
        
        // To-Do
        
    }
    
    /**************************************************************************
     * Sends a message to all other Client
     * @param message is the message sent to the other Clients
     *************************************************************************/
    public void sendMessage(String message) {
        
        // To-Do
        // For testing purposes ONLY - this code should be rewritten
        output.println(message);
        
    }
    
    /**************************************************************************
     * Sends a command to the Server.
     * @param command is the name of the command to be sent to the server
     *************************************************************************/
    public void sendCommand(String command) {
        
        // To-Do
        
    }
    
    /**************************************************************************
     * Sends out the name of this Client.
     *************************************************************************/
    public void sendName() {
        output.println(name);
    }
    
    // public static void main(String[] args) {
        
    //     System.out.println("This is the client.");
        
    // }
    
}