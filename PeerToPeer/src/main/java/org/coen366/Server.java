package org.coen366;

import java.io.*;
import java.net.*;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Server {
    private static int SERVER_PORT = 3000;
    private static int registeredClients = 0; // Initialize the variable
    private static List<ClientInfo> clients = new ArrayList<>();
    private static HashMap<String,ClientInfo> clientHashmap = new HashMap<>();

    private static List<String> files = new ArrayList<>();

    private static DatagramSocket serverSocket = null;

    public static void main(String[] args) {
        try {
            InetAddress serverAddress = InetAddress.getLocalHost();
            System.out.println("The server address is "+serverAddress);
            listenForUDP();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

    }

    public static void listenForUDP() {
        System.out.println("Enter the server port you wish to start to:");
        SERVER_PORT = Integer.parseInt(getUserInput());

        try {
            serverSocket = new DatagramSocket(SERVER_PORT);
            byte[] buffer = new byte[5000];

            System.out.println("Listening for client connections on server port: " + SERVER_PORT);

            // Listen for incoming UDP packets
            while (true) {
                // Receive incoming UDP packet
                Message receivedMessage = receiveMessageFromClient(serverSocket);
                handleMessage(receivedMessage, serverSocket);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        }
    }

    /**
     * @return The users input
     * @author Sunil
     */
    private static String getUserInput() {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

    private static Message receiveMessageFromClient(DatagramSocket socket) throws IOException, ClassNotFoundException {
        //Define 5000 bytes for the message to be stored
        byte[] buffer = new byte[5000];

        DatagramPacket request = new DatagramPacket(buffer, buffer.length);
        socket.receive(request); //stores the received message in the buffer variable

        //receives the data and casts it to a Message object
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        return (Message) objectInputStream.readObject();
    }

//    public static void listenForUDP() {
//        ServerSocket serverSocket=null;
//        try {
//            while(true) {
//                // Listen for incoming UDP packets
//                System.out.println("Listening for client connections on server port: "+SERVER_PORT);
//                //This creates a new connection at the specified server port
//                serverSocket = new ServerSocket(SERVER_PORT);
//                // Receive incoming UDP packet
//                Socket clientSocket = serverSocket.accept();
//
//                System.out.println("Connection from the client "+clientSocket.getInetAddress().getHostAddress());
//                SERVER_PORT++; // so if another client tries to connect, it will not crash the server
//
//                handleMessage(clientSocket);
//            }
//
//        } catch (IOException | ClassNotFoundException e) {
//            System.out.println("Socket is closed. Exiting");
//            exit(0);
//            throw new RuntimeException(e);
//        }
//    }

    /**
     * This method receives the message that is being sent by the client
     * Decides what method to execute
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static void handleMessage(Message receivedMessage, DatagramSocket socket) throws IOException, ClassNotFoundException {
        //This statement chooses what action to do depending on the action sent
        switch (receivedMessage.getAction()) {
            case REGISTER:
                handleRegistration(receivedMessage, socket);
                break;
            case DE_REGISTER:
                handleDeregistration(receivedMessage, socket);
                break;
            case PUBLISH:
                handlePublish(receivedMessage, socket);
                break;
            case REMOVE:
                handleRemove(receivedMessage,socket);
                break;
        }
    }

    /**
     * This method is handles when the user is trying to Register
     *
     * @param incoming this is the message that is being sent from the client
     * @throws IOException
     */
    private static void handleRegistration(Message incoming, DatagramSocket socket) throws IOException {
        ClientInfo clientInfo = incoming.getClientInfo();
        System.out.println("CLIENT INCOMING: "+incoming);

        Message outgoingMessage = checkIfClientExists(clientInfo);

        if (outgoingMessage.getAction() == Status.REGISTERED) {
            clients.add(clientInfo);
            clientHashmap.put(clientInfo.getName(),clientInfo);

            registeredClients++;
            System.out.println("CLIENT ADDED");
        }
        System.out.println("SERVER OUTGOING: "+outgoingMessage);

        sendMessageToClient(clientInfo, socket, outgoingMessage);
        handleUpdate();
    }

    /**
     * This method handles it when the user wants to deregister
     * even if the user is not registered, the server will still send a message back
     *
     * @param incoming the user that is trying to leave
     * @param socket   the socket so that we can close it
     * @throws IOException
     */
    private static void handleDeregistration(Message incoming, DatagramSocket socket) throws IOException {
        System.out.println("CLIENT INCOMING: "+incoming);
        ClientInfo deregisteringClient = incoming.getClientInfo();

        if(clientHashmap.containsKey(deregisteringClient.getName().toLowerCase()) && !deregisteringClient.getName().isBlank()){ // if the user exists
            clientHashmap.remove(deregisteringClient.getName().toLowerCase());
            registeredClients--;
            System.out.println("CLIENT REMOVED");
            Message outgoing = new Message(Status.DE_REGISTER, incoming.getRqNumber(), "Request granted");

            System.out.println("SERVER OUTGOING: "+ outgoing);
            sendMessageToClient(deregisteringClient, socket, outgoing);
        }
        else{
            //In case Name is not registered, for instance, the message is just ignored by the server. No
            //further action is taken by the server.
            System.out.println("Unregistered client tried to deregister");
        }
    }

    private static void handlePublish(Message incoming, DatagramSocket socket) throws IOException {
        ClientInfo clientInfo = incoming.getClientInfo();
        System.out.println("CLIENT INCOMING :" +incoming);

        Message outgoingMessage;

        if(clientHashmap.containsKey(clientInfo.getName().toLowerCase())){
            //The client is found, but we have to check if the file exists in the users files first
            outgoingMessage = checkIfFileExists(clientInfo);

            if (outgoingMessage.getAction() == Status.PUBLISHED) {
                //If the file is approved, update the client being stored in the clients list
                // update the user in the hashmap
                clientHashmap.replace(clientInfo.getName().toLowerCase(),clientInfo);
                System.out.println("NEW FILE ADDED");
            }
            //else the file exists in the users list, it has returned publish denied
            // and will send that message instead
        }
        else{
            //The name is not found in the hashmap so the PUBLISH is denied
            outgoingMessage = new Message(Status.PUBLISH_DENIED,clientInfo.getRqNum(),"Name does not exist. You must register");
        }
        System.out.println("SERVER OUTGOING: "+outgoingMessage);
        sendMessageToClient(clientInfo, socket, outgoingMessage);
    }

    private static void handleRemove(Message incoming, DatagramSocket socket) throws IOException {
        // might need to implement this split the file string into an array to take in multiple files to remove
        ClientInfo clientInfo = incoming.getClientInfo();
        String fileToRemove = incoming.getFile();

        System.out.println("CLIENT INCOMING :" +incoming);

        Message outgoingMessage = new Message(Status.REMOVED_DENIED,clientInfo.getRqNum(),"File does not exist");;
        //If the file is not found in the loop, that means it does not exist, and we will return this message

        if(clientHashmap.containsKey(clientInfo.getName().toLowerCase())) {
            //Going to loop through the users files to verify that the file provided is real
            for(String file : clientHashmap.get(clientInfo.getName()).getFiles()){
                if(file.contains(fileToRemove)){ // if it is real, we will remove it
                    clientHashmap.get(clientInfo.getName()).getFiles().remove(fileToRemove);
                    outgoingMessage = new Message(Status.REMOVED,clientInfo.getRqNum());
                    System.out.println("FILE REMOVED");
                    break;
                }
            }
        }
        else{
            //The name is not found therefore it must be denied
            outgoingMessage = new Message(Status.REMOVED_DENIED,clientInfo.getRqNum(),"Name does not exist. You must register");
        }
        System.out.println("SERVER OUTGOING: "+outgoingMessage);
        sendMessageToClient(clientInfo, socket, outgoingMessage);
    }

    /**
     * Anytime something happens on the server, the server must update all the registered clients with an UDPATE message
     * containing the list of currently registered clients and the files currently proposed to share
     * <p>
     * TODO(sunil): this method must be called after every action that changes the list of clients or the list of files and if no action like that occurs, then at max 5 minutes after its last call
     */
    private static void handleUpdate() throws IOException {
        Message messageToSend = new Message(Status.UPDATE, 0);
        messageToSend.setListOfClientsForUpdate(clients);
        for (ClientInfo client : clients) {
            // Send an update message to all clients
            sendMessageToClient(client, serverSocket, new Message(Status.UPDATE, client.getRqNum()));
        }
    }

    private static void sendMessageToClient(ClientInfo clientInfo, DatagramSocket socket, Message outgoingMessage) throws IOException {
        // Send response to client
        InetAddress clientAddress = clientInfo.getIpAddress();

        int clientPort = clientInfo.getClientPort();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(outgoingMessage);
        objectOutputStream.flush();

        byte[] messageToSend = byteArrayOutputStream.toByteArray();

        DatagramPacket response = new DatagramPacket(messageToSend, messageToSend.length, clientAddress, clientPort);
        socket.send(response);
    }

    /**
     * This method checks if the client exists by looping through
     * and checking if the username exists
     *
     * @param client the client that is being registered
     * @return a message to relay back to the client on their status of their Registration
     */
    private static Message checkIfClientExists(ClientInfo client) {
        //This prepares the outgoing message to be registered if
        // everything goes fine in the for loop
        Message outgoing = new Message(Status.REGISTERED, client.getRqNum());

        if(clientHashmap.containsKey(client.getName().toLowerCase())){
            outgoing = new Message(Status.REGISTER_DENIED, client.getRqNum(), "This username is taken");
        }

        return outgoing;
    }

    private static Message checkIfFileExists(ClientInfo client) {
        String newFile = client.getFiles().get(client.getFiles().size() - 1);
        //Gets the last file in the file list

        Message outgoing = new Message(Status.PUBLISHED, client.getRqNum());
        //If the file is not there, we will return this message, if not the foreach loop will change it

        ClientInfo searchedClient = clientHashmap.get(client.getName().toLowerCase());
        if(searchedClient.getFiles().contains(newFile)){
            outgoing = new Message(Status.PUBLISH_DENIED, client.getRqNum(), "This file already exists in your list");
        }

//        for (ClientInfo selectedClient : clients) {
//            if (selectedClient.getName().equalsIgnoreCase(client.getName())) { // Finds specific user by looping all the users
//                if (selectedClient.getFiles().contains(newFile))
//                    outgoing = new Message(Status.PUBLISH_DENIED, client.getRqNum(), "This file already exists in your list");
//            }
//        }
        return outgoing;
    }

    private static boolean fileSearch(String file){

        return false;
    }
}
