import javax.swing.JFrame;
import java.util.ArrayList;

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
    
    /** The name of this Client */
    private String name;
    
    /** The names of all the other connected clients */
    private ArrayList<String> otherClients;
    
    /**************************************************************************
     * Sends a message to one other Client 
     * @param message is the message sent to the Client
     * @param otherClient is the name of the other Client receiving the message
     *************************************************************************/
    private void sendMessage(String message, String otherClient) {
        
        // To-Do
        
    }
    
    /**************************************************************************
     * Sends a message to all other Client
     * @param message is the message sent to the other Clients
     *************************************************************************/
    private void sendMessage(String message) {
        
        // To-Do
        
    }
    
    /**************************************************************************
     * Sends a command to the Server
     * @param command is the name of the command to be sent to the server
     *************************************************************************/
    private void sendCommand(String command) {
        
        // To-Do
        
    }
    
    public static void main(String[] args) {
        
        JFrame frame = new ClientGUI();
        System.out.println("This is the client.");
        
    }
    
}