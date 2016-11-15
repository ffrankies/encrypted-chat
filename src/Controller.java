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
    
    /** The administrative buttons from the GUI. */
    private JButton exit, kick, help, broadcast;
    
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
                    checkboxes = gui.updateClients(client.getOtherClients());
                    addClientListeners();
                } else if (message.equals("@exit")) {
                    client.closeConnection();
                    System.exit(0);
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
        client.sendSymmetricKey();
        //client.sendInitializationVector();
        client.sendName();
        System.out.println("Client connected to server.");
        addButtonListeners();
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
    private void addButtonListeners() {
        send = gui.getSendButton();
        send.addActionListener(this);
        JButton[] admin = gui.getAdministrativeButtons();
        exit = admin[0];
        exit.addActionListener(this);
        kick = admin[1];
        kick.addActionListener(this);
        help = admin[2];
        help.addActionListener(this);
        broadcast = admin[3];
        broadcast.addActionListener(this);
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
    
    /**************************************************************************
     * Sends message from Client to a specific Client.
     * @param destination is the name of the Client to which this message is 
     * to be sent.
     *************************************************************************/
    private void sendMessage(String destination) {
        String message = gui.getClientText();
        gui.addLabel(message, true);
        client.sendMessage(message, destination);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        // Broacasts the message on the network
        if (e.getSource() instanceof JButton && e.getSource() == broadcast) {
            
            sendMessage();
            
        } else if (e.getSource() instanceof JButton && e.getSource() == send) {
            
            for (JCheckBox box: checkboxes) {
                if (box.isSelected()) {
                    sendMessage(box.getText());
                }
            }
            gui.clearInput();
            
        } else if (e.getSource() instanceof JButton && e.getSource() == exit) {
            
            client.alertExit();
            //client.closeConnection();
            // System.out.println("Reached the system.exit line.");
            // System.exit(0);
            
        } else if (e.getSource() instanceof JButton && e.getSource() == kick) {
            String namesToKick = "";
            for (int i = 0; i < checkboxes.length; ++i){
                if(checkboxes[i].isSelected()){
                    namesToKick += checkboxes[i].getText()+",";
                }
            }
            namesToKick = namesToKick.substring(0, namesToKick.length());
            client.sendKick(namesToKick);
        } else if (e.getSource() instanceof JButton && e.getSource() == help) {
            gui.displayHelp();
        }
    }
    
}