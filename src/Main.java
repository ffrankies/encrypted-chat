import javax.swing.JFrame;
import java.awt.event.ActionListener;

public class Main {
    
    public static void main(String[] args) {
        
        Client clientModel = new Client();
        
        JFrame clientView = new ClientGUI();
        
        ActionListener clientController = new Controller();
        
    }
    
}