import java.net.ServerSocket;
import java.net.Socket;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import java.nio.channels.IllegalBlockingModeException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Enumeration;

import javax.crypto.SecretKey;
import javax.crypto.KeyGenerator;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.BadPaddingException;
import javax.crypto.AEADBadTagException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.InvalidKeyException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.KeyFactory;
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
     *  Part 1
     * - Allow multiple clients
     * - Must send list of clients to all other clients
     * - Support sending to individual clients
     * - Support sending to all clients
     * - Support Administrative commands
     *   - Kick another user
     *   - More administrative commands?
     * Part2
     * - Read public/private keys from file
     * - Receive and decrypt symmetric key from client
     * - Decrypt messages from a client
     * - Re-encrypt messages and send them to a destination
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
    
    /** The key code - tells the client they are receiving the public key. */
    private static final String KEY = "@key";
    
    /** The iv code - sends initialization vector to Server. */
    private static final String IV = "@iv";
    
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
        
    /**
     * A map using client names as keys and symmetric keys as values. 
     * Contains all the currently connected clients.
     */
    private static ConcurrentHashMap<String,SecretKey> clientKeys = 
        new ConcurrentHashMap<String,SecretKey>();
        
    /**
     * A map using client names as keys and Initialization Vectors as values. 
     * Contains all the currently connected clients.
     */
    private static ConcurrentHashMap<String,IvParameterSpec> clientIVs = 
        new ConcurrentHashMap<String,IvParameterSpec>();
    
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
    private static byte[] RSAencrypt(byte[] plainText) {
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
    private static byte[] RSAdecrypt(byte[] cipherText) {
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
    
    /************************************************************************
     * Sets up the server's private key. Used here for decrypting client keys
     * @param filename the local file containing the private key
     ************************************************************************/
    private static void setPrivateKey(String filename){
        try {
            File f = new File(filename);
            FileInputStream fs = new FileInputStream(f);
            byte[] keybytes = new byte[(int)f.length()];
            fs.read(keybytes);
            fs.close();
            PKCS8EncodedKeySpec keyspec = new PKCS8EncodedKeySpec(keybytes);
            KeyFactory rsafactory = KeyFactory.getInstance("RSA");
            privateKey = rsafactory.generatePrivate(keyspec);
        } catch (Exception e){
            System.out.println("Private Key Exception");
            e.printStackTrace(System.out);
            System.exit(1);
        }
    }
    
    /**************************************************************************
     * Sets up the server's public key. Used for communicating with all clients
     * @param filename is the local file containing the public key
     * ***********************************************************************/
    private static void setPublicKey(String filename){
        try {
            File f = new File(filename);
            FileInputStream fs = new FileInputStream(f);
            byte[] keybytes = new byte[(int)f.length()];
            fs.read(keybytes);
            fs.close();
            X509EncodedKeySpec keyspec = new X509EncodedKeySpec(keybytes);
            KeyFactory rsafactory = KeyFactory.getInstance("RSA");
            publicKey = rsafactory.generatePublic(keyspec);
        } catch(Exception e) {
            System.out.println("Public Key Exception");
            System.exit(1);
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
        
        // Set up the private and public keys
        setPrivateKey("RSApriv.der");
        setPublicKey("RSApub.der");
        
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
        private static Socket clientSocket;
        
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
        private static void sendClientList() {
            
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
                System.err.println("Couldn't create output stream for client "
                    + "socket.");
                e.printStackTrace();
                System.exit(1);
            }
            
            String clientName = getClientName(input);
            
            // Receive and decrypt the symmetric key from the Client
            SecretKey clientKey = getSecretKey(input);
            
            IvParameterSpec iv = getIV(input);
            
            System.out.println("Client " + clientName + " connected to the "
                + "server.");
            
            clientOutputs.put(clientName, output);
            clientKeys.put(clientName, clientKey);
            clientIVs.put(clientName, iv);
            
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
                // Watch for a condition if the message is the key before we 
                // can start decrypting and figuring out the message
                // This condition will mostly likely be hit once when the
                // first message is received from the client
                // After figuring out the message, encrypt it, then send to its
                // destination
                if (command.equals(BROADCAST)) {
                    broadcast(message, output);
                } else if (command.equals(SEND)) {
                    send(message);
                } else if (command.equals(KICK)) {
                    kick(message);
                } else if (command.equals(EXIT)) {
                    exit(message, output, clientName);
                    // Completes while loop and ends this thread
                    break;
                } // if statement
                
            }  // while loop 
            
            System.out.println("Client has exited gracefully: " + clientName);
        }
            
        /**********************************************************************
         * Obtains client's name from a client message to the server.
         * @param input is the BufferedReader that reads data from the client
         * @return a String containing the client name
         **********************************************************************/
        private static String getClientName(BufferedReader input) {
            String clientName = receiveMessage(input);
            while (clientName.isEmpty()) {
                System.err.println(
                    "Failed in getting the client name. Retrying.");
                clientName = receiveMessage(input);
            }
            return clientName;
        }
        
        /**********************************************************************
         * Obtains the secret key from a client message to the server.
         * @param input is the BufferedReader that reads data from the client
         * @return the client's secret key as a SecretKey object
         **********************************************************************/
        private static SecretKey getSecretKey(BufferedReader input) {
            // String clientKeyStr = receiveMessage(input);
            // String code = clientKeyStr.substring(0, clientKeyStr.indexOf(" "));
            // if (!code.equals(KEY)) {
            //     System.err.println("Got something else instead of the key.");
            //     System.err.println(clientKeyStr);
            //     System.exit(1);
            // }
            // System.out.println("Secret key:" + clientKeyStr);
            // clientKeyStr = clientKeyStr.substring(
            //     clientKeyStr.indexOf(" ") + 1);
            // System.out.println("Secret key:" + clientKeyStr);
            char[] encryptedSecret = new char[256];
            try {
                int n = input.read(encryptedSecret, 0, 256);
                System.out.println("Received: " + n);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // try {
            //     encryptedSecret = clientKeyStr.getBytes("ISO-8859-1");
            //     System.out.println("Encrypted length: " + encryptedSecret.length);
            // } catch (UnsupportedEncodingException e) {
            //     System.err.println("Unsupported Encoding supplied");
            //     e.printStackTrace();
            //     System.exit(1);
            // }
            return new SecretKeySpec(RSAdecrypt(new String(encryptedSecret).getBytes()),
            "AES/CBC/PKCS5Padding");
        }
        
        /**********************************************************************
         * Obtains the initialization vector for encryption/decryption from a 
         * Client's message to the Server.
         * @param input is the BufferedReader that reads data from the client
         * @return the initialization vector as an IvParameterSpec object
         *********************************************************************/
        private static IvParameterSpec getIV(BufferedReader input) {
            String ivStr = receiveMessage(input);
            String code = ivStr.substring(0, ivStr.indexOf(" "));
            if (!code.equals(IV)) {
                System.err.println("Got something else instead of the IV.");
                System.err.println(ivStr);
                System.exit(1);
            }
            ivStr = ivStr.substring(ivStr.indexOf(" ") + 1);
            return new IvParameterSpec(ivStr.getBytes());
        }
        /**********************************************************************
         * Sends a message to all clients except the one that requested the 
         * broadcast.
         * @param message is the message to be sent.
         * @param output is the outputstream of the sender.
         **********************************************************************/
        private static void broadcast(String message, OutputStream output) {
            for (Enumeration<DataOutputStream> outputs = 
                clientOutputs.elements(); outputs.hasMoreElements(); ) {
                DataOutputStream thisOutput = outputs.nextElement();
                if (!output.equals(thisOutput)) {
                    System.out.println("Sending message.");
                    //message = new String(encrypt(message.getBytes()));
                    try {
                        thisOutput.writeBytes(message + "\n");
                    } catch (IOException e) {
                        System.err.println("Could not send data to "
                            + "client.");
                        e.printStackTrace();
                    }
                }
            }
        }
        
        /***********************************************************************
         * Sends a message addressed to a particular Client.
         * @param message is the message to be sent.
         **********************************************************************/
        private static void send(String message) {
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
            // Alternative way to send the message with cleaner code
            // Not sure it would work, but it would be worth a try
            // String[] messageStr = message.split(" ");
            // destination = messageStr[1];
            // message = SEND + " " + messageStr[2] + "\n";
            // DataOutputStream thisOutput = 
            //     clientOutputs.get(destination.substring(1));
            // try {
            //     thisOutput.writeBytes(message);
            // } catch (IOException e) {
            //     System.err.println("Could not send data to " + 
            //         destination);
            //     e.printStackTrace();
            // }
        }
        
        /**********************************************************************
         * Kicks a particular Client from the conversation.
         * @param message is a String that contains the kick message.
         **********************************************************************/
        private static void kick(String message) {
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
        }
        
        /**********************************************************************
         * Allows a Client to exit from the conversation.
         * @param message is a String containing the exit message
         * @param output is the Client's output stream
         * @param clientName is the name of the exiting client
         *********************************************************************/
        private static void exit(String message, DataOutputStream output, 
            String clientName) {
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
        }
        
        /***********************************************************************
         * Receives a message from a Client.
         * @param input is the BufferedReader that reads data from the client
         * @return the message as a String object
         **********************************************************************/
        private static String receiveMessage(BufferedReader input) {
            String message = "";
            try {
                message = input.readLine();
            } catch (IOException e) {
                System.err.println("Couldn't read message from the client.");
                e.printStackTrace();
                return "";
            }
            return message;
        }
        
    }

}