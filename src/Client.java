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
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.io.InputStream;
import java.io.DataInputStream;

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
    private static final String SEND = "@send";
    
    /** The kick code - kicks a specified client off the chat. */
    private static final String KICK = "@kick";
    
    /** The client list code - sends list of clients to all clients. */
    private static final String CLIENTLIST = "@list";
        
    /** The exit code - tells the server that a client is disconnecting. */
    private static final String EXIT = "@exit";
    
    /** The key code - lets the server know that message contains secret key. */
    private static final String KEY = "@pkey";
    
    /** The iv code - sends initialization vector to Server. */
    private static final String IV = "@ivec";
    
    /** The public key used to encrypt our symmetric key. */
    private PublicKey publicKey;
    
    /** 
     * This client's secret key, used to encrypt and decrypt messages to and
     * from the Server.
     */
    private SecretKey secretKey;
    
    /** Reads data from the server. */
    private  DataInputStream input;
    
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
            input = new DataInputStream(socket.getInputStream());
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
     * Generates an initialization vector for message encryption.
     * @return an initialization vector
     *************************************************************************/
    private IvParameterSpec generateIV() {
        // Generate an initialization vector
        SecureRandom r = new SecureRandom();
    	byte ivbytes[] = new byte[16];
    	r.nextBytes(ivbytes);
    	return new IvParameterSpec(ivbytes);
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
         * @send (5 bytes)
         * iv (16 bytes)
         * receiverName (10 bytes)
         * SenderName (10 bytes)
         * size (10 bytes)
         * message (1024 bytes)
         */
        byte[] buffer = new byte[1024 + 51];
        try {
            byte[] send = SEND.getBytes("ISO-8859-1");
            byte[] iv = generateIV().getIV();
            byte[] receiver = Arrays.copyOf(
                otherClient.getBytes("ISO-8859-1"), 10);
            byte[] sender = Arrays.copyOf(name.getBytes("ISO-8859-1"), 10);
            byte[] msg = message.getBytes("ISO-8859-1");
            msg = encrypt(msg, iv);
            byte[] size = 
                String.format("%10d", msg.length).getBytes("ISO-8859-1");
            System.arraycopy(send, 0, buffer, 0, 5);
            System.arraycopy(iv, 0, buffer, 5, 16);
            System.arraycopy(receiver, 0, buffer, 21, 10);
            System.arraycopy(sender, 0, buffer, 31, 10);
            System.arraycopy(size, 0, buffer, 41, 10);
            System.arraycopy(msg, 0, buffer, 51, msg.length);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            System.exit(1);
        }
        try {
            output.write(buffer, 0, 1024 + 35);
        } catch (IOException e) {
            System.err.println("Couldn't send encrypted message.");
            e.printStackTrace();
            System.exit(1);
        }
        
    }
    
    /**************************************************************************
     * Sends a message to all other Client
     * @param message is the message sent to the other Clients
     *************************************************************************/
    public void sendMessage(String message) {
        
        /*
         * Message format:
         * @bcst (5 bytes)
         *       (10 bytes)
         * SenderName (10 bytes)
         * size (10 bytes)
         * message (1024 bytes)
         */
        byte[] buffer = new byte[1024 + 51];
        try {
            byte[] broadcast = BROADCAST.getBytes("ISO-8859-1");
            byte[] iv = generateIV().getIV();
            byte[] sender = Arrays.copyOf(name.getBytes("ISO-8859-1"), 10);
            byte[] msg = message.getBytes("ISO-8859-1");
            msg = encrypt(msg, iv);
            byte[] size = 
                String.format("%10d", msg.length).getBytes("ISO-8859-1");
            System.arraycopy(broadcast, 0, buffer, 0, 5);
            System.arraycopy(iv, 0, buffer, 5, 16);
            System.arraycopy(sender, 0, buffer, 31, 10);
            System.arraycopy(size, 0, buffer, 41, 10);
            System.arraycopy(msg, 0, buffer, 51, msg.length);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            System.exit(1);
        }
        try {
            output.write(buffer, 0, 1024 + 51);
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
    public void sendKick(String users) {
        
        /*
         * Message format:
         * @kick (5 bytes)
         *       (10 bytes)
         * SenderName (10 bytes)
         * size (10 bytes)
         * users (1024 bytes)
         */
        byte[] buffer = new byte[1024 + 51];
        try {
            byte[] kick = KICK.getBytes("ISO-8859-1");
            byte[] iv = generateIV().getIV();
            byte[] sender = Arrays.copyOf(name.getBytes("ISO-8859-1"), 10);
            byte[] msg = users.getBytes("ISO-8859-1");
            msg = encrypt(msg, iv);
            byte[] size = 
                String.format("%10d", msg.length).getBytes("ISO-8859-1");
            System.arraycopy(kick, 0, buffer, 0, 5);
            System.arraycopy(iv, 0, buffer, 5, 16);
            System.arraycopy(sender, 0, buffer, 31, 10);
            System.arraycopy(size, 0, buffer, 41, 10);
            System.arraycopy(msg, 0, buffer, 51, msg.length);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            System.exit(1);
        }
        try {
            output.write(buffer, 0, 1024 + 51);
        } catch (IOException e) {
            System.err.println("Couldn't send encrypted message.");
            e.printStackTrace();
            System.exit(1);
        }
        
    }
    
    /**************************************************************************
     * Alerts the server that this Client is disconnecting.
     *************************************************************************/
    public void alertExit() {
        
        /*
         * Message format:
         * @exit (5 bytes)
         *       (10 bytes)
         * SenderName (10 bytes)
         * size (10 bytes)
         * users (1024 bytes)
         */
        byte[] buffer = new byte[1024 + 51];
        try {
            byte[] exit = EXIT.getBytes("ISO-8859-1");
            byte[] iv = generateIV().getIV();
            byte[] sender = Arrays.copyOf(name.getBytes("ISO-8859-1"), 10);
            byte[] msg = "Nothing To See Here".getBytes("ISO-8859-1");
            msg = encrypt(msg, iv);
            byte[] size = 
                String.format("%10d", msg.length).getBytes("ISO-8859-1");
            System.arraycopy(exit, 0, buffer, 0, 5);
            System.arraycopy(iv, 0, buffer, 5, 16);
            System.arraycopy(sender, 0, buffer, 31, 10);
            System.arraycopy(size, 0, buffer, 41, 10);
            System.arraycopy(msg, 0, buffer, 51, msg.length);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            System.exit(1);
        }
        try {
            output.write(buffer, 0, 1024 + 51);
        } catch (IOException e) {
            System.err.println("Couldn't send encrypted message.");
            e.printStackTrace();
            System.exit(1);
        }
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
        byte[] message = receiveBytes(input);
        String[] parsedMessage = parseMessage(message);
        if (parsedMessage == null)
            return "";
        String code = parsedMessage[0];
        String sender = parsedMessage[2].trim();
        String size = parsedMessage[3].trim();
        byte[] decoded = decode(message, size);
        String messageStr = new String(decoded);
        if (code.equals(CLIENTLIST)) {
            System.out.println("\"" + messageStr + "\"");
            processClientList(messageStr);
            return "";
        } else if (code.equals(EXIT)) {
            return EXIT;
        } else if (code.equals(KICK)){
            alertExit();
            return "You have been kicked by: " + sender;
        }
        return sender.toUpperCase() + ": " + messageStr;
    }
    
    /**********************************************************************
     * Parses the first x bytes from a message obtained from the Client and
     * returns the message parameters formatted as Strings. In parsing
     * the bytes, the ISO-8859-1 charset is used because one byte matches
     * to one character.
     * @param buffer is the buffer of bytes from the Client
     * @return an array of Strings comprised of:
     * [0] the message Code (@send, @kick, etc)
     * [1] optional parameter (destination/source)
     * [2] optional parameter (source)
     * [3] the size of the message, if there is one one
     * [4] the encrypted message - most likely not going to be used
     *********************************************************************/
    public String[] parseMessage(byte[] buffer) {
        if (buffer == null)
            return null;
        String[] parsed = new String[5];
        try {
            parsed[0] = new String(buffer, 0, 5, "ISO-8859-1");
            parsed[1] = new String(buffer, 21, 10, "ISO-8859-1").trim();
            parsed[2] = new String(buffer, 31, 10, "ISO-8859-1").trim();
            parsed[3] = new String(buffer, 41, 10, "ISO-8859-1");
            parsed[4] = new String(buffer, 51, 1024, "ISO-8859-1");
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
    private byte[] decode(byte[] buffer, String sizeStr) {
        int size = 0;
        System.out.println("size of the message to decode: " + sizeStr);
        try {
            size = Integer.parseInt(sizeStr.trim());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            System.exit(1);
        }
        byte[] iv = new byte[16];
        System.arraycopy(buffer, 5, iv, 0, 16);
        byte[] cipherText = new byte[size];
        for (int i = 51; i < size + 51; ++i) {
            cipherText[i - 51] = buffer[i];
        }
        byte[] decoded = decrypt(cipherText, iv);
        System.out.println("Decoded message: " + new String(decoded));
        return decoded;
    }
        
    /**********************************************************************
     * Gets the byte form of the message sent from the client.
     * @param input is the InputStream reading the data
     * @return a byte array containing the message
     *********************************************************************/
    private static byte[] receiveBytes(InputStream input) {
        byte[] buffer = new byte[1024 + 51];
        try {
            int n = input.read(buffer, 0, 1024 + 51);
            if (n == -1) {
                System.err.println("Couldn't read bytes from Server.");
                System.exit(1);
            }
            System.out.println("Read " + n + " bytes from Server.");
        } catch (IOException e) {
            System.err.println("Couldn't read bytes sent from the Server.");
            e.printStackTrace();
        }
        return buffer;
    }
        
    /************************************************************
     * Sends the symmetric key to the server after encrypting it
    ************************************************************/
    public void sendSymmetricKey() {
        byte encryptedsecret[] = RSAEncrypt(secretKey.getEncoded());
        try {
            output.write(encryptedsecret, 0, 256);
        } catch (IOException e) {
            System.err.println("Couldn't send symmetric key to server.");
            e.printStackTrace();
        }
    }
    
    /*************************************************************************
     * Sends the initialization vector to the Server.
     ************************************************************************/
    // public void sendInitializationVector() {
    // 	String ivStr = new String(iv.getIV());
    // 	try {
    // 	    output.write(iv.getIV(), 0, iv.getIV().length);
    // 	    System.out.println(iv.getIV().length + " length of IV.");
    // 	} catch (IOException e) {
    // 	    System.err.println("Couldn't send initialization vector.");
    // 	    e.printStackTrace();
    // 	    System.exit(1);
    // 	}
    // }
    
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
    private byte[] encrypt(byte[] plainText, byte[] iv) {
        try {
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
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
    private byte[] decrypt(byte[] cipherText, byte[] iv) {
        try {
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
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