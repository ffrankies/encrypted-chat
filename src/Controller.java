import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.swing.JButton;

public class Controller implements ActionListener {
    
    /** The Client which this controller controls. */
    private Client client;
    
    /** The ClientGUI which this controller controls. */
    private ClientGUI gui;
    
    /** Send button from GIU. */
    private JButton send; 
    
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
                gui.addLabel(message, false);
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
        addListeners();
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
     * Adds this controller class as the buttonListener for the GUI buttons.
     *************************************************************************/
    private void addListeners() {
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
        
        if ((JButton) e.getSource() == send) {
            sendMessage();
        }
        
    }
    
}