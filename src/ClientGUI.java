import javax.swing.*;

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
     
    /*****************************************************
     * The JTextArea to display the other client's message.
     ******************************************************/
    private JTextArea otherClientMessage; 
    
    
    JTextField clientText;
     
    public ClientGUI() {
        
        super();
        
        
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        
    }
    
    // public static void main(String [] args) {
        
    //     System.out.println("This is the Client's GUI.");
        
    // }
    
}