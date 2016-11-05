    import javax.swing.JPanel;
    import javax.swing.JButton;
    import javax.swing.BoxLayout;
    
    import java.awt.BorderLayout;
    
    public class Administration extends JPanel{
        
        /** Button for kicking a user off the chat service. */
        private JButton kick; 
        
        /** Button for display a help string to the client. */
        private JButton help;
        
        /** Button for broadcasting messages. */
        private JButton broadcast;
        
        /** Button for exiting chat. */
        private JButton exit;
        
        public Administration() {
            
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            
            exit = new JButton("Exit");
            
            kick = new JButton("Kick");
            
            help = new JButton("Help");
            
            broadcast = new JButton("Broadcast");
            
            // Add the buttons to the panel
            
            add(exit);
            
            add(kick);
            
            add(help);
            
            add(broadcast);
            
            
            setVisible(true);
        }
        
        /**********************************************************************
         * Provides access to all administrative buttons.
         * @return an array of buttons in this order:
         * 0 - the exit button
         * 1 - the kick button
         * 2 - the help button
         * 3 - the broadcast button
         *********************************************************************/
        public JButton[] getButtons() {
            JButton[] buttons = new JButton[4];
            buttons[0] = exit;
            buttons[1] = kick;
            buttons[2] = help;
            buttons[3] = broadcast;
            return buttons;
        }
        
    }