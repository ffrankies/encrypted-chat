    import javax.swing.*;
    import java.awt.GridLayout;
    
    public class InputTextPanel extends JPanel {
        
        /** Panel to contain input text field and send button. */
        private JPanel inputPanel;
        
        /** Texfield for the client to input text. */
        private JTextField clientText;
        
        /** Button for sending messages to other clients. */
        private JButton send;
        
        
        public InputTextPanel(){
            super();
            
            inputPanel = new JPanel();
            
            inputPanel.setLayout(new GridLayout(1,2));
            
            clientText = new JTextField();
            
            send = new JButton("Send");
            
            inputPanel.add(send);
            
            inputPanel.add(clientText);
        }
        
        public static void main (String [] args){
            
            
        }
    }
    
    