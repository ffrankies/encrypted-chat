    import javax.swing.*;
    import java.awt.GridLayout;
    
    public class InputTextPanel extends JPanel {
        
        
        /** Texfield for the client to input text. */
        private JTextField clientText;
        
        /** Button for sending messages to other clients. */
        private JButton send;
        
        
        public InputTextPanel(JButton send){
            super();
            
            setLayout(new GridLayout(1,2));
            
            clientText = new JTextField();
            
            this.send = send;
            
            add(send);
            
            add(clientText);
            
            setVisible(true);
        }
        
        /** Allows access to send button. */
        public JButton getSendButton() {
            return this.send;
        }
        
        /** Allows access to text field. */
        public String getClientText() {
            return this.clientText.getText();
        }
        
        /** Clears text area. */
        public void clearText() {
            this.clientText.setText("");
        }
        
    }
    
    