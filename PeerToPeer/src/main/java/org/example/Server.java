package org.example;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private static final int SERVER_PORT = 12345;
    private static int registeredClients = 0; // Initialize the variable
    private static List<String> clients = new ArrayList<>();

    public static void main(String[] args) {

        // Start the server
        listenForUDP();


//        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
//            System.out.println("Server listening on port " + SERVER_PORT);
//
//            while (true) {
//                Socket clientSocket = serverSocket.accept();
//                Thread client = new Thread(new TCPClientHandler(clientSocket));
//                client.start();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public static void listenForUDP() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(SERVER_PORT);
            byte[] buffer = new byte[1024];

            // Listen for incoming UDP packets
            while(true) {
                // Receive incoming UDP packet
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);

                // Process the packet
                String message = new String(request.getData(), 0, request.getLength());
                System.out.println("Client: " + message);

                // Send response to client
                InetAddress clientAddress = request.getAddress();
                int clientPort = request.getPort();
                byte[] sendData = message.getBytes();
                DatagramPacket response = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
                socket.send(response);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

    // Getter method for registeredClients (optional)
    public static int getRegisteredClients() {
        return registeredClients;
    }

    // Increment method for registeredClients (optional)
    public static void incrementRegisteredClients() {
        registeredClients++;
    }

    public static List<String> getClients() {
        return clients;
    }

    public static void setClients(List<String> clients) {
        Server.clients = clients;
    }

}

class TCPClientHandler implements Runnable {
    private Socket clientSocket;

    public TCPClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            String message = in.readLine(); // Read client message for file transfer (FILE-REQ and FILE-CONF are through UDP and FILE is through TCP)

            // TODO: Replace with enum
            if ("FILE".equals(message)) {
                // Register the client
                out.println("BEGIN-FILE-TRANSFER");
                String client = in.readLine();
                Server.getClients().add(client);

            }

            // Close resources
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}


