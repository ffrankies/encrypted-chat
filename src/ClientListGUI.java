    import javax.swing.JPanel;
    import javax.swing.JScrollPane;
    import javax.swing.JCheckBox;
    import javax.swing.ScrollPaneConstants;
    
    import java.awt.BorderLayout;
    import java.awt.Color;
    
    public class ClientListGUI extends JPanel {
        
        
        /** Scroll pane for having too many clients for the display. */
        JScrollPane scroll; 
        
        public ClientListGUI(){
            
            scroll = new JScrollPane();
            
            scroll.setBackground(Color.WHITE);
        
            setLayout(new BorderLayout());
        
            scroll.setVerticalScrollBarPolicy(
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            
        }
        
        /****************************************************************
         * Instatiating the butttons based on the names passed by the GUI.
         * @param names the names of the other clients 
         ****************************************************************/
         public void updatePanel(String [] names) {
             
             for (int i = 0; i < names.length; ++i) {
                JCheckBox temp = new JCheckBox(names[i]);
                //i.setMnemonic(KeyEvent.VC_i);  // Set this later as it fits
                temp.setSelected(false);
                add(temp);
            }
         }
            
        
    }
