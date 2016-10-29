import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;

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
        
        while (true) {
            System.out.println("Enter a message: ");
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String message = "";
            try {message = in.readLine();} catch (Exception e) {System.exit(1);}
            client.sendMessage(message);
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        // To-Do
        
    }
    
}