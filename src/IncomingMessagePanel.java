import javax.swing.*;


public class IncomingMessagePanel extends JPanel {
    
    /** The panel to contain the text area for incoming messages */
    private JPanel myPanel;
    
    /** A scroll pane for the panel. */
    private JScrollPane myScrollPane;
    
    /** A text area for the other client's messages. */
    private JTextArea myTextArea;
    
    public IncomingMessagePanel(){
        
        super();
        
        
        myTextArea = new JTextArea();
        
        myPanel = new JPanel();
        
        myScrollPane = new JScrollPane(myTextArea);
        
        myPanel.add(myTextArea);
        
    }
    
}