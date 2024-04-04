package org.coen366;


import java.io.IOException;
import java.net.*;
import java.util.Scanner;
import java.net.InetAddress;
public class OLD_Client {

    private static final int SERVER_PORT = 3000;

    private static final String SERVER_IP_ADDRESS = "localhost";

    private static final int CLIENT_PORT = 8080; // UDP port number

    private static int rqNum = 1;

    private final String clientName;
    private final DatagramSocket socket;

    private final InetAddress clientAddress;


    public OLD_Client(String clientName, DatagramSocket datagramSocket, InetAddress clientAddress) {

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

            OLD_Client OLDClient = new OLD_Client("CLIENT_1", socket, clientAddress);

            while (true) {
                printOptions();
                String input = getUserInput();
                switch(input) {
                    case "1":
                        OLDClient.registerWithServer();
                        break;
                    case "2":
                        OLDClient.sendRandomMessage();
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
        OLD_Client.rqNum = rqNum;
    }

    //2.4 File transfer between clients (peers)
    //File request
    private void requestFile(InetAddress IPAddressPeer, String fileName  ){
        try{
            //send file request to peer FILE-REQ | RQ# | File-name
            //string for now will change depending on how we will receive the message
            String message = "FILE-REQ" + "|" + OLD_Client.getRqNum() + "|" + fileName;

            //convert message into byte to send UDP
            byte[] transferInfo = message.getBytes();

            //to send the packet (Lab 2, slide 20...)
            DatagramPacket sendPacket = new DatagramPacket(transferInfo, transferInfo.length, IPAddressPeer, SERVER_PORT);
            socket.send(sendPacket);

            //Get answer from peer
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);
            //convert to string
            String response = new String(receivePacket.getData(),0,receivePacket.getLength());

            //confirmed or not
            //again will depend how we send the message
            //FILE-CONF | RQ# | TCP socket#
            if (response.startsWith("FILE-CONF")){
                String[] seperateInfo = response.split("|");
                int tcpSocket = Integer.parseInt(seperateInfo[2]);//3rd index (TCP socket#)
                transferFile(IPAddressPeer, tcpSocket, fileName);//still need to implement
            } else {
                System.out.println("file does not exist at destination or cannot transfer now");
            }

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    //File confirmation



    //File transfer
    private void transferFile(InetAddress IPAddressPeer, int tcpSocket, String fileName){

    }


}
