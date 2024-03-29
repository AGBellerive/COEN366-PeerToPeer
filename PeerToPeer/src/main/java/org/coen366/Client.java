package org.coen366;


import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class Client {

    private static final int SERVER_PORT = 3000;

    private static final String SERVER_IP_ADDRESS = "localhost";

    private static final int CLIENT_PORT = 8080; // UDP port number

    private static int rqNum = 1;

    private final String clientName;
    private final DatagramSocket socket;

    private final InetAddress clientAddress;


    public Client(String clientName, DatagramSocket datagramSocket, InetAddress clientAddress) {

        // ask user for (udp socket number and ip address) of server
        // ask for clientNAme
        // ask for all info (no hardcoded)

        getUserInput();

        this.clientName = clientName;
        this.socket = datagramSocket;
        this.clientAddress = clientAddress;
    }

    private static void printOptions(){
        System.out.println("Select an Option: ");
        System.out.println("1. Register");
        System.out.println("2. Send random message");
    }

    public static void main(String[] args) {

        // Create a new UDP socket
        try {
            DatagramSocket socket = new DatagramSocket(CLIENT_PORT);
            socket.setSoTimeout(10000); // 10 second timeout
            InetAddress clientAddress = InetAddress.getLocalHost();

            Client client = new Client("CLIENT_1", socket, clientAddress);

            while (true) {
                printOptions();
                String input = getUserInput();
                switch(input) {
                    case "1":
                        client.registerWithServer();
                        break;
                    case "2":
                        client.sendRandomMessage();
                        break;
                    default:
                        System.out.println("Invalid option");
                }

            }

            // Close the socket
//            socket.close();


        } catch (SocketTimeoutException e) {
            System.err.println("Timeout occurred: Server did not respond.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * A new user must register with the server before publishing or discovering what is available
     * for share. A message “REGISTER” is sent to the server through UDP.
     * For registering a user must send his/her name (every user has a unique name), IP Address and a UDP socket# it can
     * be reached at by the server or a client.
     * <p>
     * The server can accept or refuse the registration
     * ex) denied if the provided Name is already in use
     * <p>
     * If the registration is accepted the following message is sent to the user.
     * accepted the following message is sent to the user.
     * REGISTERED RQ#
     * If the registration is denied, the server will send the following message and provide the
     * reason.
     * REGISTER-DENIED RQ# Reason
     * The RQ# is used to refer to which “REGISTER” message this confirmation or denial
     * corresponds to. It is the same case of all the messages where RQ# is used.
     */
    private void registerWithServer() throws IOException {

        // Send a message to the server
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
        String[] msgInfo = receivedMessage.split(" ");
        if(msgInfo[0].equals(Status.REGISTERED.name())){
            String rqNum = msgInfo[1];
            System.out.println("Server: RQ#= " + rqNum);
        } else {
            System.out.println("Server: " + receivedMessage);
        }


    }

    private void sendRandomMessage() throws IOException {

        // Send a message to the server
        String message = "some random message";
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
     *
     * @return
     */
    private static String getUserInput() {
        Scanner scanner = new Scanner(System.in);
        String message = scanner.nextLine();
        return message;
    }

    private void incrementRQNum() {
        rqNum++;
    }

    public static int getRqNum() {
        return rqNum;
    }

    public static void setRqNum(int rqNum) {
        Client.rqNum = rqNum;
    }
}
