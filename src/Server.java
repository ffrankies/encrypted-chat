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
import java.io.DataInputStream;
import java.io.InputStream;

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
    private static final String BROADCAST = "@bcst";
     
    /** The send code - sends a message to some or all clients. */
    private static final String SEND = "@send";
    
    /** The kick code - kicks a specified client off the chat. */
    private static final String KICK = "@kick";
    
    /** The client list code - sends list of clients to all clients. */
    private static final String CLIENTLIST = "@list";
    
    /** The exit code - tells the server that a client is disconnecting. */
    private static final String EXIT = "@exit";
    
    /** The key code - tells the client they are receiving the public key. */
    private static final String KEY = "@pkey";
    
    /** The iv code - sends initialization vector to Server. */
    private static final String IV = "@ivec";
    
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
    private static byte[] encrypt(byte[] plainText, SecretKey secretKey, 
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
    private static byte[] decrypt(byte[] cipherText, SecretKey secretKey, 
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
            byte[] buffer = new byte[1024 + 35];
            System.arraycopy(CLIENTLIST.getBytes(), 0, buffer, 0, 5);
            for (Enumeration<String> clients = clientOutputs.keys(); 
                 clients.hasMoreElements(); ) {
                String client = clients.nextElement();
                byte[] encoded = encrypt(clientList.getBytes(), 
                    clientKeys.get(client), clientIVs.get(client));
                byte[] size = new byte[10];
                try {
                    size = String.format("%10d", encoded.length).getBytes(
                        "ISO-8859-1");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
                System.arraycopy(size, 0, buffer, 25, 10);
                System.arraycopy(encoded, 0, buffer, 35, encoded.length);
                try {
                    clientOutputs.get(client).write(buffer);
                } catch (IOException e) {
                    System.err.println("Could not send client list to: " + 
                        client);
                    e.printStackTrace();
                } 
            }
            
            System.out.println("Sent clientList to all clients.");
            
        }
        
        @Override
        public void run() {
            
            DataInputStream secretInput = null;
            try {
                secretInput = new DataInputStream(
                    clientSocket.getInputStream());
            } catch (IOException e) {
                System.err.println("Couldn't establish D.O.Stream.");
                e.printStackTrace();
                System.exit(1);
            }
            
            // Receive and decrypt the symmetric key from the Client
            SecretKey clientKey = getSecretKey(secretInput);
            
            IvParameterSpec iv = getIV(secretInput);
            
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
            
            System.out.println("Client " + clientName + " connected to the "
                + "server.");
            
            clientOutputs.put(clientName, output);
            clientKeys.put(clientName, clientKey);
            clientIVs.put(clientName, iv);
            
            sendClientList();
            
            try {
                secretInput = new DataInputStream(
                    clientSocket.getInputStream());
            } catch (IOException e) {
                System.err.println("Couldn't establish D.O.Stream.");
                e.printStackTrace();
                System.exit(1);
            }
            
            while (true) {
                byte[] message = receiveBytes(secretInput, clientName);
                String[] parsedMessage = parseMessage(message);
                String command = parsedMessage[0];
                String receiver = parsedMessage[1].trim();
                String sender = parsedMessage[2].trim();
                String size = parsedMessage[3].trim();
                
                // Perform operations based on the command from the Client
                if (command.equals(BROADCAST)) {
                    broadcast(message, sender, size);
                } else if (command.equals(SEND)) {
                    send(message, sender, receiver, size);
                } else if (command.equals(KICK)) {
                    kick(message, sender, size);
                } else if (command.equals(EXIT)) {
                    exit(message, sender);
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
        private static SecretKey getSecretKey(InputStream input) {
            byte[] encryptedSecret = new byte[256];
            try {
                int n = input.read(encryptedSecret, 0, 256);
                System.out.println("Received secret: " + n);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new SecretKeySpec(RSAdecrypt(encryptedSecret), "AES");
        }
        
        /**********************************************************************
         * Obtains the initialization vector for encryption/decryption from a 
         * Client's message to the Server.
         * @param input is the BufferedReader that reads data from the client
         * @return the initialization vector as an IvParameterSpec object
         *********************************************************************/
        private static IvParameterSpec getIV(InputStream input) {
            byte[] ivBuffer = new byte[16];
            try {
                int n = input.read(ivBuffer, 0, 16);
                System.out.println(n + "bytes of iv read.");
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
            return new IvParameterSpec(ivBuffer);
        }
        
        /**********************************************************************
         * Sends a message to all clients except the one that requested the 
         * broadcast.
         * @param message is the decoded message to be sent.
         * @param sender is the sender of the message.
         * @param sizeStr is the size of the encoded message
         *********************************************************************/
        private static void broadcast(byte[] message, String sender, 
            String sizeStr) {
            byte[] decoded = decode(sender, message, sizeStr);
            // Loop through connected clients
            for (Enumeration<String> clients = clientOutputs.keys(); 
                 clients.hasMoreElements(); ) {
                String clientName = clients.nextElement();
                if (!sender.equals(clientName)) {
                    //Encode the data
                    byte[] encoded = encrypt(decoded, 
                        clientKeys.get(clientName), clientIVs.get(clientName));
                    String size = String.format("%10d", encoded.length);
                    try {
                        System.arraycopy(size.getBytes("ISO-8859-1"), 0, 
                            message, 25, 10);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                    System.arraycopy(encoded, 0, message, 35, encoded.length);
                    try {
                        clientOutputs.get(clientName).write(
                            message, 0, 1024 + 35);
                    } catch (IOException e) {
                        System.err.println("Couldn't send broadcast message.");
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
            }
        }
        
        /***********************************************************************
         * Sends a message addressed to a particular Client.
         * @param message is the message to be sent, in byte format.
         * @param sender is the message's source Client
         * @param receiver is the message's destination Client
         * @param sizeStr is the size of the encoded message, in String format
         **********************************************************************/
        private static void send(byte[] message, String sender, String receiver, 
            String sizeStr) {
            byte[] decoded = decode(sender, message, sizeStr);
            // Find the destination client
            for (Enumeration<String> clients = clientOutputs.keys(); 
                clients.hasMoreElements(); ) {
                String client = clients.nextElement();
                if (client.equals(receiver)) {
                    byte[] encoded = encrypt(decoded, clientKeys.get(client), 
                        clientIVs.get(client));
                    String size = String.format("%10d", encoded.length);
                    try {
                        System.arraycopy(size.getBytes("ISO-8859-1"), 0, 
                            message, 25, 10);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                    System.arraycopy(encoded, 0, message, 35, encoded.length);
                    try {
                        clientOutputs.get(client).write(
                            message, 0, 1024 + 35);
                    } catch (IOException e) {
                        System.err.println("Couldn't send broadcast message.");
                        e.printStackTrace();
                        System.exit(1);
                    }
                    break; // No need to loop through others
                }
            }
        }
        
        /**********************************************************************
         * Kicks a particular Client from the conversation.
         * @param message is a byte array that contains the kick message.
         * @param sender is the person who sent the kick command
         * @sizeStr is the size of the kick message, formatted as a String
         **********************************************************************/
        private static void kick(byte[] message, String sender, 
            String sizeStr) {
            // Take kick command out of message
            byte[] decoded = decode(sender, message, sizeStr);
            String msg = "";
            try {
                msg = new String(decoded, "ISO-8859-1");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                System.exit(1);
            }
            String[] clients = msg.split(",");
            for(int i = 0; i < clients.length; ++i){
                System.out.println("Trying to kick: " + clients[i]);
                byte[] encoded = encrypt(decoded, clientKeys.get(clients[i]), 
                    clientIVs.get(clients[i]));
                String size = String.format("%10d", encoded.length);
                try {
                    System.arraycopy(size.getBytes("ISO-8859-1"), 0, 
                        message, 25, 10);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
                System.arraycopy(encoded, 0, message, 35, encoded.length);
                try {
                    clientOutputs.get(clients[i]).write(
                        message, 0, 1024 + 35);
                } catch (IOException e) {
                    System.err.println("Couldn't send broadcast message.");
                    e.printStackTrace();
                    System.exit(1);
                }
            }
            //sendClientList();
        }
        
        /**********************************************************************
         * Allows a Client to exit from the conversation.
         * @param message is a String containing the exit message
         * @param sender is the name of the source Client
         *********************************************************************/
        private static void exit(byte[] message, String sender) {
            // Confirm to Client that it can disconnect
            try {
                clientOutputs.get(sender).write(message, 0, 1024 + 35);
            } catch (IOException e) {
                System.err.println("Could not send exit notice back"
                    + " to client.");
                e.printStackTrace();
            }
            // Alert other users that client is disconnecting 
            String msg = sender + " has disconnected gracefully.";
            for (Enumeration<String> clients = clientOutputs.keys(); 
                clients.hasMoreElements(); ) {
                String client = clients.nextElement(); 
                byte[] decoded = new byte[1024];
                try {
                    decoded = msg.getBytes("ISO-8859-1");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
                if (!sender.equals(client)) {
                    //Encode the data
                    byte[] encoded = encrypt(decoded, clientKeys.get(client), 
                        clientIVs.get(client));
                    String size = String.format("%10d", encoded.length);
                    try {
                        byte[] send = SEND.getBytes("ISO-8859-1");
                        System.arraycopy(send, 0, message, 0, 5);
                        System.arraycopy(size.getBytes("ISO-8859-1"), 0, 
                            message, 25, 10);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                    System.arraycopy(encoded, 0, message, 35, encoded.length);
                    try {
                        clientOutputs.get(client).write(
                            message, 0, 1024 + 35);
                    } catch (IOException e) {
                        System.err.println("Couldn't send exit notice.");
                        e.printStackTrace();
                        System.exit(1);
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
            if (null == clientOutputs.remove(sender))
                System.err.println(sender + "'s output not removed.");
            if (null == clientKeys.remove(sender))
                System.err.println(sender + "'s key not removed.");
            if (null == clientIVs.remove(sender))
                System.err.println(sender + "'s iv not removed.");
            sendClientList();
        }
        
        /**********************************************************************
         * Gets the byte form of the message sent from the client.
         * @param input is the InputStream reading the data
         * @return a byte array containing the message
         *********************************************************************/
        private static byte[] receiveBytes(InputStream input, String client) {
            byte[] buffer = new byte[1024 + 35];
            try {
                int n = input.read(buffer, 0, 1024 + 35);
                System.out.println("Read " + n + " bytes from client.");
            } catch (IOException e) {
                System.err.println("Couldn't read bytes sent from: " + client);
                e.printStackTrace();
                System.exit(1);
            }
            return buffer;
        }
        
        /**********************************************************************
         * Parses the first x bytes from a message obtained from the Client and
         * returns the message parameters formatted as Strings. In parsing
         * the bytes, the ISO-8859-1 charset is used because one byte matches
         * to one character.
         * @param buffer is the buffer of bytes from the Client
         * @return an array of Strings comprised of:
         * [0] the message Code (@send, @kick, etc)
         * [1] optional parameter (destination)
         * [2] optional parameter (source)
         * [3] the size of the message, if there is one one
         * [4] the encrypted message - most likely not going to be used
         *********************************************************************/
        public static String[] parseMessage(byte[] buffer) {
            String[] parsed = new String[5];
            try {
                parsed[0] = new String(buffer, 0, 5, "ISO-8859-1");
                parsed[1] = new String(buffer, 5, 10, "ISO-8859-1").trim();
                parsed[2] = new String(buffer, 15, 10, "ISO-8859-1").trim();
                parsed[3] = new String(buffer, 25, 10, "ISO-8859-1");
                parsed[4] = new String(buffer, 35, 1024, "ISO-8859-1");
            } catch (UnsupportedEncodingException e) {
                System.err.println("Encoding specified is unsupported.");
                e.printStackTrace();
                System.exit(1);
            }
            for (String s: parsed) {
                if (!s.isEmpty())
                    System.out.println(s.trim());
            }
            return parsed;
        }
        
        /**********************************************************************
         * Obtains the decoded message given a certain clientName and 
         * ciphertext.
         * @param clientName is the name of the sending Client.
         * @param buffer contains the cipherText.
         * @param sizeStr is a String containing the size of the message to be
         * decoded
         * @return a byte buffer containing the decoded text.
         *********************************************************************/
        private static byte[] decode(String clientName, byte[] buffer, 
            String sizeStr) {
            int size = 0;
            try {
                size = Integer.parseInt(sizeStr.trim());
            } catch (NumberFormatException e) {
                e.printStackTrace();
                System.exit(1);
            }
            byte[] cipherText = new byte[size];
            for (int i = 35; i < size + 35; ++i) {
                cipherText[i - 35] = buffer[i];
            }
            byte[] decoded = decrypt(cipherText, clientKeys.get(clientName), 
                clientIVs.get(clientName));
            System.out.println("Decoded message: " + new String(decoded));
            return decoded;
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