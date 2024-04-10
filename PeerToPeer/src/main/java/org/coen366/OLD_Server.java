package org.coen366;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class OLD_Server {

    private static final int SERVER_PORT = 3000;
    private static int registeredClients = 0; // Initialize the variable
    private static List<String> clients = new ArrayList<>();



    public static void main(String[] args) {

        // ask user for socket number

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
                System.out.println("Listening for client connections on server port: "+SERVER_PORT);
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);

                // Process the packet
                String receivedMessage = new String(request.getData(), 0, request.getLength());
                String messageToSend = handleMessage(receivedMessage, request);

                // Send response to client
                InetAddress clientAddress = request.getAddress();
                int clientPort = request.getPort();
                byte[] sendData = messageToSend.getBytes();
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

    private static String handleMessage(String message, DatagramPacket request) throws IOException {

        String msgToSend;
        if(message.startsWith(Status.REGISTER.name())) {

            String[] info = message.split(" ");
            String clientName = info[1];
            String clientAddress = info[2];
            String clientPort = info[3];

            // Check if client is already registered
            if (clients.contains(clientName)) {
                msgToSend = Status.REGISTER_DENIED.name() + " " + clientName + " already registered";
            } else {
                clients.add(clientName);
                incrementRegisteredClients();
                msgToSend = Status.REGISTERED.name() + " SomeRQ#";
                System.out.println("Client registered: " + clientName + " " + clientAddress + " " + clientPort);
            }

        } else {
            msgToSend = "Unknown message received: " + message;
            System.out.println(msgToSend);
        }

        return msgToSend;

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
        OLD_Server.clients = clients;
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
                OLD_Server.getClients().add(client);

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


