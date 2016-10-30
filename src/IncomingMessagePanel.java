import javax.swing.*;


public class IncomingMessagePanel extends JPanel {
    
    /** A scroll pane for the panel. */
    private JScrollPane myScrollPane;
    
    /** A text area for the other client's messages. */
    private JTextArea myTextArea;
    
    public IncomingMessagePanel(){
        
        super();
        
        myTextArea = new JTextArea();
        
        myScrollPane = new JScrollPane(myTextArea);
        
        add(myTextArea);
        
        add(myScrollPane);
        
        setVisible(true);
        
    }
    
}