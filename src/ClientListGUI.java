    import javax.swing.JPanel;
    import javax.swing.JScrollPane;
    import javax.swing.JCheckBox;
    import javax.swing.ScrollPaneConstants;
    
    import java.awt.BorderLayout;
    import java.awt.Color;
    import java.awt.Component;
    
    import java.util.List;
    
    public class ClientListGUI extends JPanel {
        
        
        /** Scroll pane for having too many clients for the display. */
        JScrollPane scroll; 
        
        /** A list of checkboxes, one for each other connected client. */
        JCheckBox[] clients = new JCheckBox[0];
        
        public ClientListGUI(){
            
            scroll = new JScrollPane(this);
            
            scroll.setBackground(Color.WHITE);
        
            setLayout(new BorderLayout());
        
            scroll.setVerticalScrollBarPolicy(
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            
        }
        
        /****************************************************************
         * Instatiating the buttons based on the names passed by the GUI.
         * @param names the names of the other clients 
         * @return a list of the current JCheckBoxes
         ****************************************************************/
        public JCheckBox[] updatePanel(List<String> names) {
            
            // Remove all JCheckBoxes on panel.
            for (Component component: getComponents()) {
                if (component instanceof JCheckBox) {
                    remove(component);
                }
            }
            
            clients = new JCheckBox[names.size()];
            
            // Add new checkboxes
            for (int i = 0; i < names.size(); ++i) {
                JCheckBox temp = new JCheckBox(names.get(i));
                //i.setMnemonic(KeyEvent.VC_i);  // Set this later as it fits
                temp.setSelected(false);
                add(temp);
                clients[i] = temp;
            }
            
            scroll.revalidate();
            scroll.repaint();
            
            revalidate();
            repaint();
            
            return clients;
        }
        
    }
