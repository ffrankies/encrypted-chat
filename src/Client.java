import javax.swing.*;

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
    
    public static void main(String[] args) {
        
        JFrame frame = new ClientGUI();
        System.out.println("This is the client.");
        
    }
    
}