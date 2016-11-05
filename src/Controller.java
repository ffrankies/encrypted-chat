import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.swing.JButton;
import javax.swing.JCheckBox;

import java.util.List;

/******************************************************************************
 * The controller part of the client MVC. Listens for actions on the Client 
 * GUI, calls Client methods, listens for messages that the Client receives,
 * updates the GUI when needed.
 * @author Frank Derry Wanye
 * @author Gloire Rubambiza
 * @since 10/02/2016
 *****************************************************************************/
public class Controller implements ActionListener {
    
    /** The Client which this controller controls. */
    private Client client;
    
    /** The ClientGUI which this controller controls. */
    private ClientGUI gui;
    
    /** Send button from GUI. */
    private JButton send; 
    
    /** The checkboxes from the clientList panel in the GUI. */
    private JCheckBox[] checkboxes = new JCheckBox[0];
    
    /**************************************************************************
     * Helper class that listens to incoming messages to a Client within its 
     * own thread.
     *************************************************************************/
    private class ClientListener implements Runnable {
        
        /** The client that receives the messages. */
        private Client client;
        
        /**********************************************************************
         * Constructs a ClientListener with that listens on the given client 
         * object. 
         * @param client is the Client on which this ClientListener listens for
         * messages.
         *********************************************************************/
        public ClientListener(Client client) {
            super();
            this.client = client;
        }
        
        @Override
        public void run() {
            while (true) {
                String message = client.receiveMessage();
                if (message.equals("")) {
                    // for (String name: client.getOtherClients())
                    //     System.out.println(name);
                    checkboxes = gui.updateClients(client.getOtherClients());
                    addClientListeners();
                } else {
                    gui.addLabel(message, false);
                }
            }
            
        }
        
    }
    
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
        addSendListener();
        // Creates a thread that listens to messages from the Server.
        new Thread(new ClientListener(client)).start();
        
        // while (true) {
        //     System.out.println("Enter a message: ");
        //     BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        //     String message = "";
        //     try {message = in.readLine();} catch (Exception e) {System.exit(1);}
        //     client.sendMessage(message);
        // }
        
    }
    
    /**************************************************************************
     * Adds this controller class as the buttonListener for the other client
     * checkboxes.
     *************************************************************************/
    private void addClientListeners() {
        for (JCheckBox box: checkboxes) {
            box.addActionListener(this);
        }
    }
    
    /**************************************************************************
     * Adds this controller class as the buttonListener for the GUI buttons.
     *************************************************************************/
    private void addSendListener() {
        send = gui.getSendButton();
        send.addActionListener(this);
    }
    
    /**************************************************************************
     * Sends message from Client to all other Clients.
     *************************************************************************/
    private void sendMessage() {
        String message = gui.getClientText();
        gui.clearInput();
        gui.addLabel(message, true);
        client.sendMessage(message);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        // Sends message if user clicks on send
        if (e.getSource() instanceof JButton && e.getSource() == send) {
            sendMessage();
        }
        
        // Toggles checkbox if user clicks on it
        for (JCheckBox box: checkboxes) {
            if (e.getSource() instanceof JCheckBox && e.getSource() == box) {
                if (box.isSelected()) {
                    box.setSelected(false);
                } else {
                    box.setSelected(true);
                }
            }
        }
        
    }
    
}