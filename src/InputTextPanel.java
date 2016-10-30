    import javax.swing.*;
    import java.awt.GridLayout;
    
    public class InputTextPanel extends JPanel {
        
        
        /** Texfield for the client to input text. */
        private JTextField clientText;
        
        /** Button for sending messages to other clients. */
        private JButton send;
        
        
        public InputTextPanel(){
            super();
            
            setLayout(new GridLayout(1,2));
            
            clientText = new JTextField();
            
            send = new JButton("Send");
            
            add(send);
            
            add(clientText);
            
            setVisible(true);
        }
        
    }
    
    