    import javax.swing.JPanel;
    import javax.swing.JScrollPane;
    import javax.swing.JCheckBox;
    
    public class ClientListGUI extends JPanel {
        
        
        /** Scroll pane for having too many clients for the display. */
        JScrollPane scroll; 
        
        public ClientListGUI(){
            
            scroll = new JScrollPane();
            
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
