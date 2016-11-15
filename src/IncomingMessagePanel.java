import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.ScrollPaneConstants;
import javax.swing.BoxLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.BorderLayout;

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
        
        setLayout(new BorderLayout());
        
        myScrollPane.setVerticalScrollBarPolicy(
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        myTextArea.setLayout(new BoxLayout(myTextArea, BoxLayout.Y_AXIS));
        
        add(myScrollPane, BorderLayout.CENTER);
        
        // setPreferredSize(new Dimension(400, 400));
        // setPreferredSize(new Dimension(400, 400));
        
        setVisible(true);
        
    }
    
    public void addLabel(String text, int align) {
        JLabel label = new JLabel(text, align);
        myTextArea.add(label);
        label.setSize(myTextArea.getWidth(), label.getHeight());
        myTextArea.revalidate();
        myTextArea.repaint();
        revalidate();
        repaint();
    }
    
    public void displayHelp(){
        JLabel send, kick, exit, broadcast;
        send = new JLabel("Send - use to send messages to specific" + 
        "clients");
        kick = new JLabel("Kick - use to kick unlikable users off the chat");
        exit = new JLabel("Exit - use to notify the server before exiting");
        broadcast = new JLabel("Broadcast - use to broadcast messages to all" +
        "clients in the chat");
        myTextArea.add(send);
        myTextArea.add(kick);
        myTextArea.add(exit);
        myTextArea.add(broadcast);
        myTextArea.revalidate();
        myTextArea.repaint();
        revalidate();
        repaint();
        
    }
    
}