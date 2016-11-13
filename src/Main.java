import javax.swing.JFrame;

import java.awt.event.ActionListener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class Main {
    
    public static void main(String[] args) {
        
        BufferedReader userInput = new BufferedReader(new InputStreamReader(
            System.in));
            
        System.out.println("Enter the client's name: ");
        
        String clientName = "";
        try {
            clientName = userInput.readLine();
            while ( clientName.length() > 10) {
                System.out.println("Please enter a valid name");
                clientName = userInput.readLine();
            }
        } catch (IOException e) {
            System.err.println("Could not read from user input.");
            e.printStackTrace();
            System.exit(1);
        }
        
        System.out.println("Enter the server to which client should connect: ");
        
        String serverIP = "";
        try {
            serverIP = userInput.readLine();
        } catch (IOException e) {
            System.err.println("Could not read from user input.");
            e.printStackTrace();
            System.exit(1);
        }
        
        Client clientModel = new Client(clientName, serverIP);
        
        ClientGUI clientView = new ClientGUI(clientName);
        
        ActionListener clientController = new Controller(
            clientModel, clientView);
        
    }
    
}