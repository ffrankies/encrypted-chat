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
        send = new JLabel("Send - send messages to specific clients");
        kick = new JLabel("Kick - kick unlikable users off the chat");
        exit = new JLabel("Exit - notify the server before exiting");
        broadcast = new JLabel("Broadcast - broadcast messages to all " +
        "clients");
        myTextArea.add(send);
        send.setSize(myTextArea.getWidth(), send.getHeight());
        myTextArea.add(kick);
        kick.setSize(myTextArea.getWidth(), kick.getHeight());
        myTextArea.add(exit);
        exit.setSize(myTextArea.getWidth(), exit.getHeight());
        myTextArea.add(broadcast);
        broadcast.setSize(myTextArea.getWidth(), broadcast.getHeight());
        myTextArea.revalidate();
        myTextArea.repaint();
        revalidate();
        repaint();
        
    }
    
}