import javax.swing.*;

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
     
    
    /** Panel for other client's message */
    private IncomingMessagePanel incoming;
    
    /** Panel for entering local client's message */
    private InputTextPanel input;
     
    public ClientGUI(String name) {
        
        super();
        
        incoming = new IncomingMessagePanel();
        
        input = new InputTextPanel();
        
        setTitle("Encrypted Chat - " + name);
        
        setSize(500, 500);
        
        setLayout(new BorderLayout());
        
        add(incoming, BorderLayout.CENTER);
        add(input, BorderLayout.SOUTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        
    }
    
    /** Allows access to send button. */
    public JButton getSendButton() {
        return this.input.getSendButton();
    }
    
    /** Allows access to text field. */
    public JTextField getClientText() {
        return this.input.getClientText();
    }
    // public static void main(String [] args) {
        
    //     System.out.println("This is the Client's GUI.");
        
    // }
    
}