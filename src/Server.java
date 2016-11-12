import java.net.ServerSocket;
import java.net.Socket;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.DataOutputStream;

import java.nio.channels.IllegalBlockingModeException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Enumeration;

import javax.crypto.SecretKey;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.BadPaddingException;
import javax.crypto.AEADBadTagException;
import javax.crypto.spec.IvParameterSpec;

import java.security.InvalidKeyException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.spec.*;

// import java.lang.IllegalStateException;


import javax.xml.bind.DatatypeConverter;

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
    
    /** The public key used for RSA encryption. */
    private static PublicKey publicKey;
    
    /** The private key used for RSA decryption. */
    private static PrivateKey privateKey;
    
    /** 
     * A map using client names as keys and client output streams as values.
     * Contains all the currently connected clients.
     */
    private static ConcurrentHashMap<String,DataOutputStream> clientOutputs = 
        new ConcurrentHashMap<String,DataOutputStream>();
    
    /**************************************************************************
     * Encrypts plaintext into ciphertext, given a provided secret key and 
     * IvParameterSpec.
     * @param plainText is the plaintext to be encrypted
     * @param secretKey is the secret key used to encrypt the plaintext data
     * @param iv is the initialization vector used to initialize the encryption
     * cipher
     * @return a byte array containing the encrypted data
     *************************************************************************/
    private byte[] encrypt(byte[] plainText, SecretKey secretKey, 
        IvParameterSpec iv) {
        try {
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            byte[] cipherText = c.doFinal(plainText);
            return cipherText;
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Specified algorithm doesn't exist.");
            e.printStackTrace();
            return plainText;
        } catch (NoSuchPaddingException e) {
            System.err.println("Specified padding doesn't exist.");
            e.printStackTrace();
            return plainText;
        } catch (InvalidKeyException e) {
            System.err.println("The encryption key doesn't exist.");
            e.printStackTrace();
            return plainText;
        } catch (InvalidAlgorithmParameterException e) {
            System.err.println("The algorithm parameter isn't valid.");
            e.printStackTrace();
            return plainText;
        } catch (IllegalStateException e) {
            System.err.println("The cipher is in the wrong state.");
            e.printStackTrace();
            return plainText;
        } catch (IllegalBlockSizeException e) {
            System.err.println("Unable to process input data.");
            e.printStackTrace();
            return plainText;
        } catch (AEADBadTagException e) {
            System.err.println("Authentication tag doesn't match calculated "
                + "value.");
            e.printStackTrace();
            return plainText;
        } catch (BadPaddingException e) {
            System.err.println("Data not bounded by the appropriate padding.");
            e.printStackTrace();
            return plainText;
        }
    }
    
    /**************************************************************************
     * Decrypts ciphertext into plaintext, given a provided secret key and 
     * IvParameterSpec.
     * @param cipherText is the ciphertext to be decrypted
     * @param secretKey is the secret key used to decrypt the ciphertext data
     * @param iv is the initialization vector used to initialize the decryption
     * cipher
     * @return a byte array containing the decrypted data
     *************************************************************************/
    private byte[] decrypt(byte[] cipherText, SecretKey secretKey, 
        IvParameterSpec iv) {
        try {
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.DECRYPT_MODE, secretKey, iv);
            byte[] plainText = c.doFinal(cipherText);
            return plainText;
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Specified algorithm doesn't exist.");
            e.printStackTrace();
            return cipherText;
        } catch (NoSuchPaddingException e) {
            System.err.println("Specified padding doesn't exist.");
            e.printStackTrace();
            return cipherText;
        } catch (InvalidKeyException e) {
            System.err.println("The encryption key doesn't exist.");
            e.printStackTrace();
            return cipherText;
        } catch (InvalidAlgorithmParameterException e) {
            System.err.println("The algorithm parameter isn't valid.");
            e.printStackTrace();
            return cipherText;
        } catch (IllegalStateException e) {
            System.err.println("The cipher is in the wrong state.");
            e.printStackTrace();
            return cipherText;
        } catch (IllegalBlockSizeException e) {
            System.err.println("Unable to process input data.");
            e.printStackTrace();
            return cipherText;
        } catch (AEADBadTagException e) {
            System.err.println("Authentication tag doesn't match calculated "
                + "value.");
            e.printStackTrace();
            return cipherText;
        } catch (BadPaddingException e) {
            System.err.println("Data not bounded by the appropriate padding.");
            e.printStackTrace();
            return cipherText;
        }
    }
    
    /**************************************************************************
     * Uses RSA to encrypt plaintext data. Used here only for establishing a 
     * private key pair between client and server.
     * @param plainText is a byte array containing the plaintext to be encrypted
     * @return a byte array containing the encrypted ciphertext
     *************************************************************************/
    private byte[] RSAencrypt(byte[] plainText) {
        try {
            Cipher c = Cipher.getInstance(
                "RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
            c.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] cipherText = c.doFinal(plainText);
            return cipherText;
        }catch (NoSuchAlgorithmException e) {
            System.err.println("Specified algorithm doesn't exist.");
            e.printStackTrace();
            return plainText;
        } catch (NoSuchPaddingException e) {
            System.err.println("Specified padding doesn't exist.");
            e.printStackTrace();
            return plainText;
        } catch (InvalidKeyException e) {
            System.err.println("The encryption key doesn't exist.");
            e.printStackTrace();
            return plainText;
        } catch (IllegalStateException e) {
            System.err.println("The cipher is in the wrong state.");
            e.printStackTrace();
            return plainText;
        } catch (IllegalBlockSizeException e) {
            System.err.println("Unable to process input data.");
            e.printStackTrace();
            return plainText;
        } catch (AEADBadTagException e) {
            System.err.println("Authentication tag doesn't match calculated "
                + "value.");
            e.printStackTrace();
            return plainText;
        } catch (BadPaddingException e) {
            System.err.println("Data not bounded by the appropriate padding.");
            e.printStackTrace();
            return plainText;
        }
    }
    
    /**************************************************************************
     * Uses RSA to decrypt ciphertext data. Used here only for establishing a 
     * private key pair between client and server.
     * @param cipherText is a byte array containing the ciphertext to be 
     * decrypted
     * @return a byte array containing the decrypted plaintext
     *************************************************************************/
    private byte[] RSAdecrypt(byte[] cipherText) {
        try {
            Cipher c = Cipher.getInstance(
                "RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
            c.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] plainText = c.doFinal(cipherText);
            return plainText;
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Specified algorithm doesn't exist.");
            e.printStackTrace();
            return cipherText;
        } catch (NoSuchPaddingException e) {
            System.err.println("Specified padding doesn't exist.");
            e.printStackTrace();
            return cipherText;
        } catch (InvalidKeyException e) {
            System.err.println("The encryption key doesn't exist.");
            e.printStackTrace();
            return cipherText;
        } catch (IllegalStateException e) {
            System.err.println("The cipher is in the wrong state.");
            e.printStackTrace();
            return cipherText;
        } catch (IllegalBlockSizeException e) {
            System.err.println("Unable to process input data.");
            e.printStackTrace();
            return cipherText;
        } catch (AEADBadTagException e) {
            System.err.println("Authentication tag doesn't match calculated "
                + "value.");
            e.printStackTrace();
            return cipherText;
        } catch (BadPaddingException e) {
            System.err.println("Data not bounded by the appropriate padding.");
            e.printStackTrace();
            return cipherText;
        }
    }
    
     /**************************************************************************
     * Uses RSA to encrypt plaintext data. Used here only for establishing a 
     * private key pair between client and server.
     * @param plainText is a byte array containing the plaintext to be encrypted
     * @return a byte array containing the encrypted ciphertext
     *************************************************************************/
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
            
            clientOutputs.put(clientName, output);
            
            sendClientList();
            
            while (true) {
                String message = "";
                try {
                    message = input.readLine();
                } catch (IOException e) {
                    System.err.println("Could not read from client socket. " 
                        + clientName);
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
                    for (String c: clients) 
                        System.out.println(c);
                    for(int i = 0; i < clients.length; ++i){
                        System.out.println("Trying to kick: " + clients[i]);
                        DataOutputStream thisOutput = clientOutputs.get(
                            clients[i]);
                        try{
                            // Tells client it is being kicked
                            thisOutput.writeBytes("@kick \n");
                        } catch(IOException e){
                            System.err.println("Could not close"+ 
                            " DataOutputStream");
                            e.printStackTrace();
                        }
                    }
                    sendClientList();
                    
                } else if (command.equals(EXIT)) {
                    // Confirm to Client that it can disconnect
                    try {
                        output.writeBytes("@exit \n");
                    } catch (IOException e) {
                        System.err.println("Could not send exit notice back"
                            + " to client.");
                        e.printStackTrace();
                    }
                    // Alert other users that client is disconnecting 
                    message = SEND + " " + clientName + " has disconnected " 
                        + "gracefully.\n";
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
                    // Close client socket, remove client from maps
                    try {
                        clientSocket.close();
                    } catch (IOException e) {
                        System.err.println("Couldn't close client socket.");
                        e.printStackTrace();
                    }
                    clientOutputs.remove(clientName);
                    sendClientList();
                    // Completes while loop and ends this thread
                    break;
                } // if statement
                
            }  // while loop 
            
            System.out.println("Client has exited gracefully: " + clientName);
        }
        
    }
    
}