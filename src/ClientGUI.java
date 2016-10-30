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
     
    
    /** Panel for other client's message */
    private IncomingMessagePanel incoming;
    
    /** Panel for entering local client's message */
    private InputTextPanel input;
    
    /** Frame to display everything */
    private JFrame clientFrame;
    
    
     
    public ClientGUI() {
        
        super();
        
        incoming = new IncomingMessagePanel();
        
        input = new InputTextPanel();
        
        clientFrame = new JFrame("Encrypted Chat");
        
        clientFrame.add(incoming);
        clientFrame.add(input);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        
    }
    
    // public static void main(String [] args) {
        
    //     System.out.println("This is the Client's GUI.");
        
    // }
    
}