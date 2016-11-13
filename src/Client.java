import javax.swing.JFrame;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;

import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.BadPaddingException;
import javax.crypto.AEADBadTagException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.*;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;

/******************************************************************************
 * A Client in an encrypted chat program.
 * @author Frank Wanye
 * @author Gloire Rubambiza
 * @since 10/26/2016
 *****************************************************************************/
public class Client {
    
    /**
     * TO-DO
     * Part 1
     * - Connect to Server
     * - Must request and receive list of clients to all other clients
     * - Support sending to individual clients
     * - Support sending to all clients
     * - Support sending Administrative commands
     *   - Kick another user
     *   - More administrative commands?
     * - Have a GUI for all this to be done
     * - Should have a name
     * Part 2
     * - Load RSA public key from local file at connection 
     * - Send the server our generated symmetric key
     * - Decrypt the messages sent by the server
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
    private CopyOnWriteArrayList<String> otherClients = 
        new CopyOnWriteArrayList<String>();
    
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
    private static final String BROADCAST = "@bcst";
    
    /** The send code - sends a message to some or one client(s). */
    private static final String SEND = "@send ";
    
    /** The kick code - kicks a specified client off the chat. */
    private static final String KICK = "@kick";
    
    /** The client list code - sends list of clients to all clients. */
    private static final String CLIENTLIST = "@clientlist";
        
    /** The exit code - tells the server that a client is disconnecting. */
    private static final String EXIT = "@exit";
    
    /** The key code - lets the server know that message contains secret key. */
    private static final String KEY = "@key";
    
    /** The iv code - sends initialization vector to Server. */
    private static final String IV = "@iv ";
    
    /** The public key used to encrypt our symmetric key. */
    private PublicKey publicKey;
    
    /** 
     * This client's secret key, used to encrypt and decrypt messages to and
     * from the Server.
     */
    private SecretKey secretKey;
    
    /* The initialization vector used in message encryption/decryption. */
    private IvParameterSpec iv;
    
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
        setPublicKey("RSApub.der");
        secretKey = generateAESKey();
        
        // Generate an initialization vector
        SecureRandom r = new SecureRandom();
    	byte ivbytes[] = new byte[16];
    	r.nextBytes(ivbytes);
    	iv = new IvParameterSpec(ivbytes);
    	
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
     * Reads the server's public key. Used for decrypting the secret key
     * @param filename is the local file containing the public key
     * ***********************************************************************/
    private void setPublicKey(String filename){
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
        // try {
        //     output.writeBytes(BROADCAST + "@" + name + " " + message + "\n");
        // } catch (IOException e) {
        //     System.err.println("Could not send message to server.");
        //     e.printStackTrace();
        //     return;
        // }
        byte[] buffer = new byte[1024 + 35];
        try {
            byte[] broadcast = BROADCAST.getBytes("ISO-8859-1");
            byte[] sender = Arrays.copyOf(name.getBytes("ISO-8859-1"), 10);
            byte[] msg = message.getBytes("ISO-8859-1");
            msg = encrypt(msg, secretKey, iv);
            byte[] size = 
                String.format("%10d", msg.length).getBytes("ISO-8859-1");
            System.arraycopy(broadcast, 0, buffer, 0, 5);
            System.arraycopy(sender, 0, buffer, 5, 10);
            System.arraycopy(size, 0, buffer, 25, 10);
            System.arraycopy(msg, 0, buffer, 35, msg.length);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            System.exit(1);
        }
        try {
            output.write(buffer, 0, 1024);
        } catch (IOException e) {
            System.err.println("Couldn't send encrypted message.");
            e.printStackTrace();
            System.exit(1);
        }
        
    }
    
    /**************************************************************************
     * Sends a command to the Server.
     * @param command is the name of the command to be sent to the server
     *************************************************************************/
    public void sendKick( String users) {
        
        try{
            output.writeBytes(KICK + " " + users + "\n");
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
            output.writeBytes(EXIT + " @" + name + "\n");
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
            // Maybe send the name and the symmetric at the same time?
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
            System.out.println("\"" + message + "\"");
            processClientList(message.substring(message.indexOf(" ") + 1));
            return "";
        } else if (code.equals(EXIT)) {
            return EXIT;
        } else if (code.equals(KICK)){
            alertExit();
        }
        return message.substring(message.indexOf(" ") + 1);
    }
    
    /************************************************************
     * Sends the symmetric key to the server after encrypting it
    ************************************************************/
    public void sendSymmetricKey() {
        byte encryptedsecret[] = RSAEncrypt(secretKey.getEncoded());
        // String keyStr = "";
        // try {
        //     keyStr = new String(encryptedsecret, "ISO-8859-1");
        // } catch (UnsupportedEncodingException e) {
        //     System.err.println("Unsupported Encoding supplied.");
        //     e.printStackTrace();
        // }
        try {
            output.write(encryptedsecret, 0, 256);
        } catch (IOException e) {
            System.err.println("Couldn't send symmetric key to server.");
            e.printStackTrace();
        }
        /*
          TO-DO STILL
          - Start decrypting client messages on the server side with this key
        */
    }
    
    /*************************************************************************
     * Sends the initialization vector to the Server.
     ************************************************************************/
    public void sendInitializationVector() {
    	String ivStr = new String(iv.getIV());
    	try {
    	    output.write(iv.getIV(), 0, iv.getIV().length);
    	    System.out.println(iv.getIV().length + " length of IV.");
    	} catch (IOException e) {
    	    System.err.println("Couldn't send initialization vector.");
    	    e.printStackTrace();
    	    System.exit(1);
    	}
    }
    
    /*******************************************************
     * Generate a symmetric to share with the server
     * Have to figure out where this goes in the logic later
     *******************************************************/
    public SecretKey generateAESKey(){
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            SecretKey secKey = keyGen.generateKey();
            return secKey;
        } catch (NoSuchAlgorithmException e){
            System.out.println("Encryption algorithm doesn't exist.");
            System.exit(1);
            return null;
        }
    }
    
    /**********************************************************
     * Encrypts the symmetric key using the server's public key
     * @param plaintext the key to be encrypted
     * @return an encrypted byte of data to be sent to the server
     ************************************************************/
    public byte[] RSAEncrypt(byte[] plaintext){
        try {
            Cipher c = Cipher.getInstance(
                "RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
            c.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] ciphertext = c.doFinal(plaintext);
            System.out.println("Encrypted length: " + ciphertext.length);
            return ciphertext;
        } catch(Exception e) {
            System.out.println("RSA Encrypt Exception");
            System.exit(1);
            return null;
        }
    }
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
        for (String cname: otherClients) {
            if (!Arrays.asList(clients).contains(cname)) {
                otherClients.remove(cname);   
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
            System.out.println("Input closed.");
            output.flush();
            System.out.println("Output flushed.");
            output.close();
            System.out.println("Output closed.");
        } catch (IOException e) {
            System.err.println("Couldn't close input and output streams.");
            e.printStackTrace();
        }
        System.out.println("Done closing the input and output.");
    }
    
    
     
}