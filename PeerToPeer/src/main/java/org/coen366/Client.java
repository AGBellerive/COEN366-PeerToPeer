package org.coen366;


import java.net.*;
import java.util.Scanner;

public class Client {

    private static final int SERVER_PORT = 12345;

    private static final int CLIENT_PORT = 12; // UDP port number
    private static  String clientName = "Client-1";


    public static void main(String[] args) {
        // Register with the server
        registerWithServer();

    }

    /**
     * A new user must register with the server before publishing or discovering what is available
     * for share. A message “REGISTER” is sent to the server through UDP.
     * For registering a user must send his/her name (every user has a unique name), IP Address and a UDP socket# it can
     * be reached at by the server or a client.
     *
     * The server can accept or refuse the registration
     * ex) denied if the provided Name is already in use
     *
     * If the registration is accepted the following message is sent to the user.
     * accepted the following message is sent to the user.
     * REGISTERED RQ#
     * If the registration is denied, the server will send the following message and provide the
     * reason.
     * REGISTER-DENIED RQ# Reason
     * The RQ# is used to refer to which “REGISTER” message this confirmation or denial
     * corresponds to. It is the same case of all the messages where RQ# is used.
     */
    private static void registerWithServer() {
        try {
            // Create a new UDP socket
            DatagramSocket socket = new DatagramSocket(CLIENT_PORT);
            socket.setSoTimeout(10000); // 10 second timeout

            // Send a message to the server
            InetAddress clientAddress = InetAddress.getLocalHost();
            String message = "REGISTER " + clientName + " " + clientAddress.getHostAddress() + " " + CLIENT_PORT;
            byte[] sendData = message.getBytes();
            InetAddress serverAddress = InetAddress.getByName("localhost");
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, SERVER_PORT);
            socket.send(sendPacket);

            // Receive a response from the server
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);

            // Process the response
            String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("Server: " + receivedMessage);

            // Close the socket
            socket.close();
        } catch (SocketTimeoutException e) {
            System.err.println("Timeout occurred: Server did not respond.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * A user can de-register by sending the following message to the server.
     * DE-REGISTER RQ# Name
     * If the name is registered, the server will remove the name and all the
     * information related to this user.
     * If the name is not registered, the server ignores the message
     */
    private static void deRegisterWithServer() {

    }

    /**
     * Can use for later when doing multithreading
     * @return
     */
    private static String getUserInput() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter client name: ");
        String message = scanner.nextLine();
        return message;
    }
}
