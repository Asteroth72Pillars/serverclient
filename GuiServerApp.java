
package za.ac.cput.prac3_219233829;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

public class GuiServerApp extends JFrame implements ActionListener 
{
    private ServerSocket listener;
    private String msg = "";
    private String upCaseMsg = "";
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private JButton exitBtn = new JButton("EXIT");
    private JTextArea clientTxtArea = new JTextArea(5, 40);
    private String response = "";
    private JPanel topPanel = new JPanel();
    private JPanel centerPanel = new JPanel();

    public GuiServerApp() 
    {
        super("Server");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        exitBtn.addActionListener(this);

        topPanel.add(exitBtn);
        centerPanel.add(new JScrollPane(clientTxtArea));

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        setSize(500, 300);
        setLocationRelativeTo(null);
        setVisible(true);
        
        try 
        {
            listener = new ServerSocket(12345, 100);
            while (true) {
                Socket client = listener.accept();
                getStreams(client); 
                processClient(client); 
            }
        } catch (IOException e) 
        {
            e.printStackTrace();
        }
    }

     private void getStreams(Socket client) throws IOException 
     {
        out = new ObjectOutputStream(client.getOutputStream());
        out.flush();
        in = new ObjectInputStream(client.getInputStream());
    }

    private void processClient(Socket client) throws IOException 
    {
    do {
        try {
            Object receivedObject = in.readObject();

            if (receivedObject instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> searchData = (List<String>) receivedObject;
                msg = (String) in.readObject();

                if (msg.startsWith("SEARCH:")) {
                    String searchTerm = msg.substring(7); // Extract search term from the message
                    String searchResults = performSearch(searchTerm, searchData);
                    sendData(searchResults); // Send back search results
                } else {
                    upCaseMsg = msg.toUpperCase();
                    sendData(upCaseMsg);
                }
            }
        } catch (ClassNotFoundException classNotFoundException) {
            clientTxtArea.append("\nUnknown object type received");
        }
    } while (!msg.equals("terminate"));
    }   

    
    private void performSearch(List<String> searchData, String searchTerm) 
    {
    try 
    {
        out.writeObject(searchData);
        out.flush();
        sendData("SEARCH:" + searchTerm); 
    } catch (IOException ioException) 
    {
        serverTxt.append("\nError writing object");
    }
    }
    
    private void sendData(String myMsg) 
    {
        try 
        {
            out.writeObject(myMsg);
            out.flush();
            displayMessage("\nSERVER>> " + myMsg);
        } catch (IOException ioException) 
        {
            clientTxtArea.append("\nError writing object");
        }
    }

    private void displayMessage(final String messageToDisplay) 
    {
        SwingUtilities.invokeLater(() -> clientTxtArea.append(messageToDisplay));
    }

    public void actionPerformed(ActionEvent event) 
    {
        if (event.getSource() == exitBtn) 
        {
            System.exit(0);
        }
    }

    public static void main(String[] args) 
    {
        new GuiServerApp();
    }
}
