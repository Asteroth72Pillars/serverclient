/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package za.ac.cput.tut3_219233829;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import za.ac.cput.tut3_219233829.Student;
import za.ac.cput.tut3_219233829.StudentRecordClient;
/**
 *
 * @author zaihd
 */
public class StudentRecordServer {
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private List<Student> studentRecords;
    private final Object lock = new Object();

    public StudentRecordServer() {
        studentRecords = Collections.synchronizedList(new ArrayList<>());
    }

    public void getStreams() {
        try {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void searchStudentRecord(int id) {
        try {
            Student result = null;
            synchronized (lock) {
                for (Student student : studentRecords) {
                    if (student.getId() == id) {
                        result = student;
                        break;
                    }
                }
            }
            out.writeObject(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void processClient() {
        try {
            Object receivedObject;
            while (true) {
                receivedObject = in.readObject();
                if (receivedObject instanceof Student) {
                    Student student = (Student) receivedObject;
                    synchronized (lock) {
                        studentRecords.add(student);
                    }
                    System.out.println("Added student: " + student);
                } else if (receivedObject instanceof String) {
                    String request = (String) receivedObject;
                    if (request.equals("Exit")) {
                        break;
                    } else if (request.equals("retrieve")) {
                        synchronized (lock) {
                            out.writeObject(new ArrayList<>(studentRecords));
                        }
                    } else if (request.startsWith("search:")) {
                        int searchId = Integer.parseInt(request.substring(7));
                        searchStudentRecord(searchId);
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void closeConnection() {
        try {
            out.close();
            in.close();
            clientSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        StudentRecordServer server = new StudentRecordServer();
        try {
            server.serverSocket = new ServerSocket(12345);
            System.out.println("Server is waiting for connections...");
            server.clientSocket = server.serverSocket.accept();
            System.out.println("Client connected: " + server.clientSocket.getInetAddress().getHostAddress());

            server.getStreams();
            server.processClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}