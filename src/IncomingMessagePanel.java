import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.ScrollPaneConstants;

import java.awt.Color;
import java.awt.Dimension;


public class IncomingMessagePanel extends JPanel {
    
    /** A scroll pane for the panel. */
    private JScrollPane myScrollPane;
    
    /** A text area for the other client's messages. */
    private JPanel myTextArea;
    
    /****************************************
     * Constructor the incoming message panel
     ****************************************/
    public IncomingMessagePanel(){
        
        super();
        
        myTextArea = new JPanel();
        
        myScrollPane = new JScrollPane(myTextArea);
        
        myScrollPane.setBackground(Color.WHITE);
        
        myScrollPane.setVerticalScrollBarPolicy(
            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        
        add(myScrollPane);
        
        setPreferredSize(new Dimension(400, 400));
        setPreferredSize(new Dimension(400, 400));
        
        setVisible(true);
        
    }
    
    public void addLabel(String text, int align) {
        JLabel label = new JLabel(text, align);
        label.setSize(myTextArea.getWidth(), label.getHeight());
        myTextArea.add(label);
    }
    
}