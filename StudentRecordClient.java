/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package za.ac.cput.tut3_219233829;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 *
 * @author zaihd
 */
public class StudentRecordClient implements ActionListener {
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private JFrame frame;
    private JTextField nameField;
    private JTextField idField;
    private JTextField scoreField;
    private JTextArea recordsTextArea;
    private JButton addButton;
    private JButton retrieveButton;
    private JButton exitButton;

    private JTextField searchField;
    private JButton searchButton;

    private Socket socket;

    public StudentRecordClient() {
        try {
            socket = new Socket("localhost", 12345);

            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            setGUI();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setGUI() {
        frame = new JFrame("Student Record Client");

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(3, 2));

        nameField = new JTextField(20);
        idField = new JTextField(20);
        scoreField = new JTextField(20);

        inputPanel.add(new JLabel("Name: "));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("ID: "));
        inputPanel.add(idField);
        inputPanel.add(new JLabel("Score: "));
        inputPanel.add(scoreField);

        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout());

        searchField = new JTextField(15);
        searchButton = new JButton("Search");

        searchPanel.add(new JLabel("Search by ID: "));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        addButton = new JButton("Add Record");
        retrieveButton = new JButton("Retrieve Records");
        exitButton = new JButton("Exit");

        addButton.addActionListener(this);
        retrieveButton.addActionListener(this);
        exitButton.addActionListener(this);
        searchButton.addActionListener(this);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(retrieveButton);
        buttonPanel.add(exitButton);

        recordsTextArea = new JTextArea(10, 30);
        recordsTextArea.setEditable(false);

        JScrollPane scroll = new JScrollPane(recordsTextArea);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(scroll, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.add(searchPanel, BorderLayout.NORTH);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setVisible(true);
    }

    private void addStudentRecord() {
    try {
        String name = nameField.getText();
        String idText = idField.getText();
        String scoreText = scoreField.getText();

        // Check if idText is a valid integer
        if (!idText.isEmpty() && idText.matches("\\d+")) {
            int id = Integer.parseInt(idText);
            double score = Double.parseDouble(scoreText);

            Student student = new Student(id, name, score);
            out.writeObject(student);
        } else {
            // Display an error message to the user
            JOptionPane.showMessageDialog(frame, "Invalid ID. Please enter a valid integer for ID.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}

    private void retrieveStudentRecords() {
        try {
            out.writeObject("retrieve");
            List<Student> receiveList = (List<Student>) in.readObject();
            displayStudentRecords(receiveList);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void displayStudentRecords(List<Student> studentRecords) {
        recordsTextArea.setText("Student Records:\n");
        for (Student student : studentRecords) {
            recordsTextArea.append(student.toString() + "\n");
        }
    }

    private void searchStudentRecord(int id) {
        try {
            out.writeObject("search:" + id);
            Student student = (Student) in.readObject();
            if (student != null) {
                recordsTextArea.setText("Search Result:\n");
                recordsTextArea.append(student.toString() + "\n");
            } else {
                recordsTextArea.setText("No student found with ID: " + id);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void closeConnection() {
        try {
            out.writeObject("Exit");
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addButton) {
            addStudentRecord();
        } else if (e.getSource() == retrieveButton) {
            retrieveStudentRecords();
        } else if (e.getSource() == exitButton) {
            closeConnection();
            System.exit(0);
        } else if (e.getSource() == searchButton) {
            String searchInput = searchField.getText();
            if (!searchInput.isEmpty()) {
                int searchId = Integer.parseInt(searchInput);
                searchStudentRecord(searchId);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StudentRecordClient());
    }
}

