import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Controller implements ActionListener {
    
    /** The Client which this controller controls. */
    Client client;
    
    /** The ClientGUI which this controller controls. */
    ClientGUI gui;
    
    /**************************************************************************
     * Instantiates a Controller with the given Client and GUI.
     * @param client is the Client which this class controls.
     * @param gui is the ClientGUI which this class controls.
     *************************************************************************/
    public Controller(Client client, ClientGUI gui) {
        this.client = client;
        this.gui = gui;
        client.sendName();
        System.out.println("Client connected to server.");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        // To-Do
        
    }
    
}