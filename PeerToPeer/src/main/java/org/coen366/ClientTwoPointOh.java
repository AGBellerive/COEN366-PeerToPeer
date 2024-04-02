package org.coen366;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Scanner;

import static java.lang.System.exit;

public class ClientTwoPointOh {
    private static DatagramSocket clientSocket;
    private static ClientInfo storedClient;

    private List<ClientInfo> listOfClientInformations;

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
                        printPublishingOptions();
                        break;
                    default:
                        System.out.println("Invalid option");
                }
            }
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
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
     * 2.1 Registration and De-registration
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static void printRegisterOptions() throws IOException, ClassNotFoundException, InterruptedException {
        while (true){
            System.out.println("Select a Register Option: ");
            System.out.println("1. REGISTER");
            System.out.println("2. DE_REGISTER");
            System.out.println("3. Return");
            int input = Integer.parseInt(getUserInput());

            switch(input) {
                case 1:
                    registerWithServer(ClientTwoPointOh.clientSocket);
                    break;
                case 2:
                    if(storedClient != null){
                        deregisterWithServer(ClientTwoPointOh.clientSocket);
                        exit(0);
                        return;
                    }
                    else{
                        System.out.println("You have not registered. Returning to main menu");
                    }
                break;
                case 3:
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
    private static void registerWithServer(DatagramSocket clientSocket) throws IOException, ClassNotFoundException, InterruptedException {
        System.out.println("Enter your name");
        String name = getUserInput();

        InetAddress clientAddress = InetAddress.getLocalHost();

        //Creates a client with the entered name and with their ipaddress
        ClientInfo clientInfo = new ClientInfo(name,clientAddress,CLIENT_PORT);
        Message outgoingRegister = new Message(Status.REGISTER,clientInfo.getRqNum(),clientInfo);

        //Sends a message to the server, in this case, it sends a register message
        sendMessageToServer(outgoingRegister);

        //Creates a message of registration
        Message incoming = getMessageFromServer(clientSocket,clientInfo);

        // System.out.println(incoming);
        switch (incoming.getAction()){
            case REGISTER_DENIED:
                System.out.println(incoming.getAction() + " Request Number: " + clientInfo.getRqNum()+ " " + incoming.getReason());
                break;
            case REGISTERED:
                System.out.println("Registration Successful");
                storedClient = clientInfo;
                Thread.sleep(1000);
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
     * @param client
     * @return the message sent from the server
     * @throws IOException
     * @throws ClassNotFoundException
     * @author Alex & Sunil
     */
    private static Message getMessageFromServer(DatagramSocket clientSocket,ClientInfo client) throws IOException, ClassNotFoundException {
        //Prepares the space for the message to be stored in
        byte[] buffer = new byte[5000];

        DatagramPacket request = new DatagramPacket(buffer, buffer.length);
        //waits for the message to be received
        clientSocket.receive(request);

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);

        //Increments the rqNum because a rqNum is associated to a specific message
        client.incrementRqNum();

        //converts the buffer into a Message object
        return (Message)objectInputStream.readObject();
    }


    /**
     * This method deregisters the user from the server
     * @param clientSocket this is passed to close the socket
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static void deregisterWithServer(DatagramSocket clientSocket) throws IOException, ClassNotFoundException {
        Message deRegisterOutgoingMessage = new Message(Status.DE_REGISTER,storedClient.getRqNum(),storedClient);
        sendMessageToServer(deRegisterOutgoingMessage);

        Message incoming = getMessageFromServer(clientSocket,storedClient);
        System.out.println(incoming);
        System.out.println("Deregestration complete");
        clientSocket.close();
        exit(0);
    }

    /**
     * This method prints out the options for publishing
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    private static void printPublishingOptions() throws IOException, ClassNotFoundException, InterruptedException {
        if(storedClient == null){
            System.out.println("You must register first before publishing");
            Thread.sleep(1000);
            return;
        }
        while (true){
            System.out.println("Select a Publishing Option");
            System.out.println("1. PUBLISH");
            System.out.println("2. REMOVE");
            int input = Integer.parseInt(getUserInput());

            switch (input){
                case 1:
                    publishFileToServer(ClientTwoPointOh.clientSocket);
                    break;
                case 2:
                    System.out.println("Not implemented yet relax");
                    //removeFileFromServer();
                    break;
                default:
                    System.out.println("Invalid Option");
            }
        }
    }

    /**
     * This method will send the server the file that the user wants to put available
     * on the server. If the file exists it will send the required information to the
     * server so the server can retain it
     * @param clientSocket
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static void publishFileToServer(DatagramSocket clientSocket) throws IOException, ClassNotFoundException {
        System.out.println("Enter the file path you want to publish to the server");
        String filePath = getUserInput();
        File publishedFile = new File(filePath);

        if(!(publishedFile.exists())){
            //The file does not exist therefore must tell the user and return back to main
            System.out.println("The file path you entered is not found. Try again. Rerouting you back...");
            return;
        }

        System.out.println("File found.");
        storedClient.addToFiles(filePath);

        //In stored client, there is a list that contains all the files that the user has. This has to be Extracted and stored on the server side
        Message publishOutgoingMessage = new Message(Status.PUBLISH, storedClient.getRqNum(),storedClient);
        sendMessageToServer(publishOutgoingMessage);
        Message incoming = getMessageFromServer(clientSocket,storedClient);

        switch (incoming.getAction()){
            case PUBLISHED:
                System.out.println("Publish successful.");
                break;
            case PUBLISH_DENIED:
                storedClient.getFiles().remove(filePath);
                System.out.println(incoming.getAction() + " Request Number: " + storedClient.getRqNum()+ " " + incoming.getReason());
                break;
        }
    }

    private static void removeFileFromServer(DatagramSocket clientSocket) throws IOException, ClassNotFoundException {
//TODO
        Message removeOutgoingMessage = new Message(Status.REMOVE, storedClient.getRqNum(),storedClient);
        sendMessageToServer(removeOutgoingMessage);
        Message incoming = getMessageFromServer(clientSocket,storedClient);
        switch (incoming.getAction()){
            case REMOVED:
                break;
            case REMOVED_DENIED:
                break;
        }
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
        System.out.println("2. Publish");
        System.out.println("3. Update");
        System.out.println("4. File transfer between clients");
        System.out.println("5. Update contact information");
    }


}
