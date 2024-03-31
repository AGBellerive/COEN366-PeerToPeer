package org.coen366;

import java.io.*;
import java.net.*;
import java.util.Scanner;

import static java.lang.System.exit;

public class ClientTwoPointOh {
    private static DatagramSocket clientSocket;
    private static ClientInfo storedClient;

    private static int CLIENT_PORT = 8080;
    private static int SERVER_PORT = 3000;

    public static void main(String[] args) {
        System.out.println("Enter the server port you wish to connect to:");
        SERVER_PORT = Integer.parseInt(getUserInput());

        System.out.println("Enter the port you wish to connect to: ");
        CLIENT_PORT = Integer.parseInt(getUserInput());

        byte[] buffer = new byte[5000];
        try {
            //Connects to the port specified by the user
            clientSocket = new DatagramSocket(CLIENT_PORT);
            clientSocket.setSoTimeout(10000); // 10 second timeout
            InetAddress clientAddress = InetAddress.getLocalHost();

            //Will send this message to the server to display
            String connectionMessage = "I am client " + clientAddress.getHostAddress() + " connecting on the port "+ CLIENT_PORT ;

            //Prepares the message to be sent in a byte array
            byte[] sendData = connectionMessage.getBytes();

            //Sends message to server
            InetAddress serverAddress = InetAddress.getByName("localhost");
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, SERVER_PORT);
            clientSocket.send(sendPacket);

            //Receives response from server
            DatagramPacket serverResponse = new DatagramPacket(buffer, buffer.length);
            clientSocket.receive(serverResponse);
            String responseMessage = new String(serverResponse.getData(), 0, serverResponse.getLength());
            System.out.println(responseMessage);

            while (true) {
                printOptions();
                String input = getUserInput();
                switch(input) {
                    case "1":
                        printRegisterOptions();
                        break;
                    case "2":
                        //client.sendRandomMessage();
                        break;
                    default:
                        System.out.println("Invalid option");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("The connection has an error. Exiting...");
            exit(0);
            throw new RuntimeException(e);
        }
        finally {
            if(clientSocket != null){
                clientSocket.close();
            }
        }
    }

    /**
     * This offers the user options to register or return to main menu
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static void printRegisterOptions() throws IOException, ClassNotFoundException {
        while (true){
            System.out.println("Select a Register Option: ");
            System.out.println("1.REGISTER");
            System.out.println("2.DE_REGISTER");
            System.out.println("3.Return");
            String input = getUserInput();

            switch(input) {
                case "1":
                    registerWithServerTwoPointOh(ClientTwoPointOh.clientSocket);
                    break;
                case "2":
                    if(storedClient != null){
                        deregisterWithServer(ClientTwoPointOh.clientSocket);
                        exit(0);
                        return;
                    }
                    else{
                        System.out.println("You have not registered. Returning to main menu");
                    }
                break;
                case "3":
                    return;
                default:
                    System.out.println("Invalid option");
            }
        }
    }

    /**
     * This method deals with any registration the user does
     * @param clientSocket the current connection
     * @return the clientInfo and stores it into a static field
     * @throws IOException
     * @throws ClassNotFoundException
     * @author Alex
     */
    private static void registerWithServerTwoPointOh(DatagramSocket clientSocket) throws IOException, ClassNotFoundException {
        System.out.println("Enter your name");
        String name = getUserInput();

        InetAddress clientAddress = InetAddress.getLocalHost();

        //Creates a client with the entered name and with their ipaddress
        ClientInfo clientInfo = new ClientInfo(name,clientAddress,CLIENT_PORT);
        Message outgoingRegister = new Message(Status.REGISTER,clientInfo.getRqNum(),clientInfo);

        //Sends a message to the server, in this case, it sends a register message
        sendMessageToServer(outgoingRegister);

        //Creates a message of registration
        Message incoming = getMessageFromServer(clientSocket);

        // System.out.println(incoming);
        switch (incoming.getAction()){
            case REGISTER_DENIED:
                System.out.println("DENIED: "+incoming.getReason());
                break;
            case REGISTERED:
                System.out.println("Registration Successful");
                storedClient = clientInfo;
                break;
        }
    }

    /**
     * This method sends a message object to the server
     * @param outgoingMessage this is the message we are sending
     * @throws IOException
     * @author Alex & Sunil
     */
    private static void sendMessageToServer(Message outgoingMessage) throws IOException {
        //Prepares the message to be sent
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(outgoingMessage);
        objectOutputStream.flush();

        byte [] sendMessage = byteArrayOutputStream.toByteArray();

        //Sends message to server
        InetAddress serverAddress = InetAddress.getByName("localhost");
        DatagramPacket sendPacket = new DatagramPacket(sendMessage, sendMessage.length, serverAddress, SERVER_PORT);
        clientSocket.send(sendPacket);
    }

    /**
     * This method receives the message back from the server
     * @param clientSocket
     * @return the message sent from the server
     * @throws IOException
     * @throws ClassNotFoundException
     * @author Alex & Sunil
     */
    private static Message getMessageFromServer(DatagramSocket clientSocket) throws IOException, ClassNotFoundException {
        //Prepares the space for the message to be stored in
        byte[] buffer = new byte[5000];

        DatagramPacket request = new DatagramPacket(buffer, buffer.length);
        //waits for the message to be recieved
        clientSocket.receive(request);

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);

        //converts the buffer into a Message object
        return (Message)objectInputStream.readObject();
    }

    private static void deregisterWithServer(DatagramSocket clientSocket) throws IOException, ClassNotFoundException {
        Message deRegisterMessage = new Message(Status.DE_REGISTER,storedClient.getRqNum(),storedClient);
        sendMessageToServer(deRegisterMessage);

        Message incoming = getMessageFromServer(clientSocket);
        System.out.println(incoming);
        System.out.println("Deregestration complete");
    }

    /**
     * @author Sunil
     * @return The users input
     */
    private static String getUserInput() {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

    /**
     * @author Sunil
     */
    private static void printOptions(){
        System.out.println("Select an Option: ");
        System.out.println("1. Register");
        System.out.println("2. Send random message");
        System.out.println("3. Publish");
        System.out.println("4. Update");
    }


}
