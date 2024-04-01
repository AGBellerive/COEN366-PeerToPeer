package org.coen366;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static java.lang.System.exit;

public class ServerTwoPointOh {
    private static int SERVER_PORT = 3000;
    private static int registeredClients = 0; // Initialize the variable
    private static List<ClientInfo> clients = new ArrayList<>();


    public static void main(String[] args) {
        listenForUDP();
    }

    public static void listenForUDP(){
        System.out.println("Enter the server port you wish to start to:");
        SERVER_PORT = Integer.parseInt(getUserInput());

        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(SERVER_PORT);
            byte[] buffer = new byte[5000];

            System.out.println("Listening for client connections on server port: "+SERVER_PORT);

            //Receives initial connection message
            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
            socket.receive(request);
            String connectionMessage = new String(request.getData(), 0, request.getLength());
            System.out.println(connectionMessage);

            // Send response to client
            String messageToSend = "Connection successful. Proceed with registration";
            InetAddress clientAddress = request.getAddress();
            int clientPort = request.getPort();
            byte[] sendData = messageToSend.getBytes();
            DatagramPacket response = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
            socket.send(response);

            // Listen for incoming UDP packets
            while(true) {
                // Receive incoming UDP packet
                Message receivedMessage = receiveMessageFromClient(socket);
                handleMessage(receivedMessage,socket);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
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

    private static Message receiveMessageFromClient(DatagramSocket socket) throws IOException, ClassNotFoundException {
        //Define 5000 bytes for the message to be stored
        byte[] buffer = new byte[5000];

        DatagramPacket request = new DatagramPacket(buffer, buffer.length);
        socket.receive(request); //stores the received message in the buffer variable

        //receives the data and casts it to a Message object
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        return (Message)objectInputStream.readObject();
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
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static void handleMessage(Message receivedMessage,DatagramSocket socket) throws IOException, ClassNotFoundException {
        //This statement chooses what action to do depending on the action sent
            switch (receivedMessage.getAction()){
                case REGISTER :
                    handleRegistration(receivedMessage,socket);
                    break;
                case DE_REGISTER :
                    handleDeregistration(receivedMessage,socket);
                    break;
                case PUBLISH:
                    handlePublish(receivedMessage,socket);
                    break;
                case REMOVE:
                    break;
            }
    }

    /**
     * This method is handles when the user is trying to Register
     * @param incoming this is the message that is being sent from the client
     * @throws IOException
     */
    private static void handleRegistration(Message incoming,DatagramSocket socket) throws IOException {
        Message outgoingMessage = checkIfClientExists( incoming.getClientInfo());
        System.out.println(incoming);
        System.out.println(outgoingMessage);

        if(outgoingMessage.getAction() == Status.REGISTERED){
            clients.add(incoming.getClientInfo());
            registeredClients++;
            System.out.println("ADDED");
        }
        sendMessageToClient(incoming, socket, outgoingMessage);
    }

    /**
     * This method handles it when the user wants to deregister
     * @param incoming the user that is trying to leave
     * @param socket the socket so that we can close it
     * @throws IOException
     */
    private static void handleDeregistration(Message incoming,DatagramSocket socket) throws IOException {
        ClientInfo deregisteringClient = incoming.getClientInfo();
        for (int i =0; i < clients.size();i++) {
            ClientInfo currentClient = clients.get(i);
            if(currentClient.getName().equalsIgnoreCase(deregisteringClient.getName())){
                clients.remove(i);
                registeredClients--;
                break;
            }
        }

        Message outgoing = new Message(Status.DE_REGISTER,incoming.getRqNumber(),"Request granted");
        sendMessageToClient(incoming,socket,outgoing);
        socket.close();
    }

    private static void handlePublish(Message incoming,DatagramSocket socket) throws IOException {
        System.out.println(incoming);
        // update the user in the list "clients"
        Message outgoingMessage = checkIfFileExists(incoming.getClientInfo());

        if(outgoingMessage.getAction() == Status.PUBLISHED){
            ClientInfo searchedClient = incoming.getClientInfo();
            //If the file is approved, update the client being stored in the clients list
            for (int i = 0; i < clients.size(); i++) {
                if(clients.get(i).getName().equalsIgnoreCase(searchedClient.getName())){
                    // Instead of modifying the list of files for the searched user, Just replace the whole user
                    // with the updated field
                    clients.set(i,searchedClient);
                }
            }
        }
        sendMessageToClient(incoming,socket,outgoingMessage);
    }


    private static void sendMessageToClient(Message incoming, DatagramSocket socket, Message outgoingMessage) throws IOException {
        // Send response to client
        InetAddress clientAddress = incoming.getClientInfo().getIpAddress();

        int clientPort = incoming.getClientInfo().getClientPort();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(outgoingMessage);
        objectOutputStream.flush();

        byte [] messageToSend = byteArrayOutputStream.toByteArray();

        DatagramPacket response = new DatagramPacket(messageToSend, messageToSend.length, clientAddress, clientPort);
        socket.send(response);
    }

    /**
     * This method checks if the client exists by looping through
     * and checking if the username exists
     * @param client the client that is being registered
     * @return a message to relay back to the client on their status of their Registration
     */
    private static Message checkIfClientExists(ClientInfo client){
        //This prepares the outgoing message to be registered if
        // everything goes fine in the for loop
        Message outgoing = new Message(Status.REGISTERED,client.getRqNum());

        for (ClientInfo c :clients) {
            if(c.getName().equalsIgnoreCase(client.getName())){
                //This means that the user already exists, and we have to return a
                //registration denied
                outgoing = new Message(Status.REGISTER_DENIED,client.getRqNum(),"This username is taken");
            }
        }
        return outgoing;
    }

    private static Message checkIfFileExists(ClientInfo client){
        String newFile = client.getFiles().get(client.getFiles().size() -1 );
        //Gets the last file in the file list

        Message outgoing = new Message(Status.PUBLISHED,client.getRqNum());
        //If the file is not there, we will return this message, if not the foreach loop will change it

        for(ClientInfo selectedClient : clients){
            if(selectedClient.getName().equalsIgnoreCase(client.getName())){ // Finds specific user by looping all the users
                if(selectedClient.getFiles().contains(newFile))
                    outgoing = new Message(Status.PUBLISH_DENIED,client.getRqNum(),"This file already exists in your list");
            }
        }
        return outgoing;
    }
}
