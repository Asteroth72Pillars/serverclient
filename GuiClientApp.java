
package za.ac.cput.prac3_219233829;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class GuiClientApp extends JFrame implements ActionListener 
{
    private Socket server;
    private JButton exitBtn = new JButton("EXIT");
    private JLabel clientLbl = new JLabel("Enter text here");
    private JTextField clientTxt = new JTextField(20);
    private JTextArea serverTxt = new JTextArea(5, 40);
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String response = "";
    private JPanel topPanel = new JPanel();
    private JPanel bottomPanel = new JPanel();
    //search
    private JTextField searchField = new JTextField(20);
    private JButton btnSearch = new JButton("Search");
    
    
    public GuiClientApp() 
    {
        super("Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        exitBtn.addActionListener(this);

        topPanel.add(exitBtn);
        bottomPanel.add(clientLbl);
        bottomPanel.add(clientTxt);
        bottomPanel.add(searchField);
        bottomPanel.add(btnSearch);
        bottomPanel.add(new JScrollPane(serverTxt));

        add(topPanel, BorderLayout.NORTH);
        add(bottomPanel, BorderLayout.CENTER);
        
        btnSearch.addActionListener(this);
        
        setSize(500, 300);
        setLocationRelativeTo(null);
        setVisible(true);

        try 
        {
            connectToServer();
            getStreams();
            communicate();
        } catch (IOException e) 
        {
            e.printStackTrace();
        }
    }

    private void connectToServer() throws IOException 
    {
        server = new Socket(InetAddress.getByName("127.0.0.1"), 12345);
    }

    private void getStreams() throws IOException 
    {
        out = new ObjectOutputStream(server.getOutputStream());
        out.flush();
        in = new ObjectInputStream(server.getInputStream());
    }

    private void communicate() 
    {
        clientTxt.addActionListener(this);

        do {
            try {
                response = (String) in.readObject();
                displayMessage("\nCLIENT>> " + response);
            } catch (ClassNotFoundException classNotFoundException) 
            {
                serverTxt.append("\nUnknown object type received");
            } catch (IOException ioException) 
            {
                ioException.printStackTrace();
            }
        } while (!response.equals("terminate"));
    }

    private void sendData(String myMsg) 
    {
        try 
        {
            out.writeObject(myMsg);
            out.flush();
        } catch (IOException ioException) 
        {
            serverTxt.append("\nError writing object");
        }
    }

    private void displayMessage(final String messageToDisplay) 
    {
        SwingUtilities.invokeLater(() -> serverTxt.append(messageToDisplay));
    }
    
    private void performSearch(String searchTerm) 
    {
    try 
    {
        out.writeObject(searchTerm);         
        out.flush();
    } catch (IOException ioException) 
    {
        serverTxt.append("\nError writing object");
    }
    }
    
    
    public void actionPerformed(ActionEvent event) 
    {
        if (event.getSource() == exitBtn) 
        {
            System.exit(0);
        } else if (event.getSource() == clientTxt) 
        {
            sendData(clientTxt.getText());
            clientTxt.setText("");
        } else if (event.getSource() == btnSearch)
        {
            String searchTerm = searchField.getText();
            performSearch(searchTerm);
            searchField.setText("");
        }
    }

    public static void main(String[] args) 
    {
        new GuiClientApp();
    }
}


