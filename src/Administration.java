    import javax.swing.JPanel;
    import javax.swing.JButton;
    
    import java.awt.BorderLayout;
    
    public class Administration extends JPanel{
        
        /** Button for kicking a user off the chat service. */
        private JButton kick; 
        
        /** Button for display a help string to the client. */
        private JButton help;
        
        public Administration() {
            
            setLayout(new BorderLayout());
            
            kick = new JButton("Kick");
            
            help = new JButton("Help");
            
            // Add the buttons to the panel
            add(kick, BorderLayout.NORTH );
            
            add(help, BorderLayout.SOUTH);
            
            setVisible(true);
        }
    }