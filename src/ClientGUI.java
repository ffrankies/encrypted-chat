import javax.swing.JFrame;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.SwingConstants;

import java.util.List;

import java.awt.BorderLayout;

public class ClientGUI extends JFrame {
    
    /**
     * TO-DO
     * 
     * - Have a textArea/Field to display other client's messages
     * - Have a textArea/Field to enter text
     * - Display a list of clients, with checkboxes or something 
     * - Have a panel with administrative commands
     * - A submit button
     * 
     */
    
    /** Panel for other client's message. */
    private IncomingMessagePanel incoming;
    
    /** Panel for entering local client's message. */
    private InputTextPanel input;
    
    /** Panel for the admin commands. */
    private Administration admin;
    
    /** Panel for the client list. */
    private ClientListGUI clientList;
    
    //JButton send = new JButton("Send");
     
    public ClientGUI(String name) {
        
        super();
        
        incoming = new IncomingMessagePanel();
        
        input = new InputTextPanel();
        
        admin = new Administration();
        
        clientList = new ClientListGUI();
        
        setTitle("Encrypted Chat - " + name);
        
        setSize(500, 500);
        
        setLayout(new BorderLayout());
        
        add(incoming, BorderLayout.CENTER);
        add(input, BorderLayout.SOUTH);
        add(admin, BorderLayout.EAST);
        add(clientList, BorderLayout.WEST);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        
    }
    
    /** Allows access to send button. */
    public JButton getSendButton() {
        return this.input.getSendButton();
    }
    
    /** Allows access to text field. */
    public String getClientText() {
        return this.input.getClientText();
    }
    
    /** Clears input text. */
    public void clearInput() {
        input.clearText();
    }
    
    /**************************************************************************
     * Adds a Label containing sent text to the IncomingMessagePanel 
     * @param text is the text to be displayed
     * @param self is true when the message was sent by this client, false 
     * when coming from another client.
     *************************************************************************/
    public void addLabel(String text, boolean self) {
        if (self) {
            incoming.addLabel(text, SwingConstants.RIGHT);
        } else {
            incoming.addLabel(text, SwingConstants.LEFT);
        }
    }
    
    /**************************************************************************
     * Updates the Client list in the clientList panel with the new list of 
     * other Clients.
     * @param otherClients is the new list of other connected Clients.
     * @return a JCheckBox[] of buttons created from the list of Clients.
     *************************************************************************/
    public JCheckBox[] updateClients(List<String> otherClients) {
        return clientList.updatePanel(otherClients);
    }
}