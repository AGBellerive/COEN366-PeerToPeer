package org.coen366;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.exit;

public class ServerTwoPointOh {
    private static int SERVER_PORT = 3000;
    private static int registeredClients = 0; // Initialize the variable
    private static List<ClientInfo> clients = new ArrayList<>();


    public static void main(String[] args) {
        listenForUDP();
    }

    public static void listenForUDP() {
        ServerSocket serverSocket=null;
        try {
            while(true) {
                // Listen for incoming UDP packets
                System.out.println("Listening for client connections on server port: "+SERVER_PORT);
                //This creates a new connection at the specified server port
                serverSocket = new ServerSocket(SERVER_PORT);
                // Receive incoming UDP packet
                Socket clientSocket = serverSocket.accept();

                System.out.println("Connection from the client "+clientSocket.getInetAddress().getHostAddress());
                SERVER_PORT++; // so if another client tries to connect, it will not crash the server

                handleMessage(clientSocket);
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Socket is closed. Exiting");
            exit(0);
            throw new RuntimeException(e);
        }
    }

    /**
     * This method receives the message that is being sent by the client
     * Decides what method to execute
     * @param clientSocket is the connection the client established
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static void handleMessage(Socket clientSocket) throws IOException, ClassNotFoundException {
        while(true){
            //Writing out to the client
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            //Taking in what the client wrote
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
            //This reads the message that is sent from the client and deserializes it and the server waits here
            Message incoming = (Message) in.readObject();
            //Probably can be a case statement
            //This statement chooses what action to do depending on the action sent
            switch (incoming.getAction()){
                case REGISTER :
                        handleRegistration(incoming,out);
                    break;
                case DE_REGISTER :
                    handleDeregistration(incoming,out,clientSocket);
                    break;
            }

        }
    }

    /**
     * This method is handles when the user is trying to Register
     * @param incoming this is the message that is being sent from the client
     * @param out this is the output streams so this function can send a message back to client
     * @throws IOException
     */
    private static void handleRegistration(Message incoming,ObjectOutputStream out) throws IOException {
        Message outgoing = checkIfClientExists( incoming.getClientInfo());
        System.out.println(outgoing);

        if(outgoing.getAction() == Status.REGISTERED){
            clients.add(incoming.getClientInfo());
            registeredClients++;
            System.out.println("ADDED");
        }
        out.writeObject(outgoing);
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

    /**
     * This method handles it when the user wants to deregister
     * @param incoming the user that is trying to leave
     * @param out the output stream to tell the user that they checked out
     * @param clientSocket the socket so that we can close it
     * @throws IOException
     */
    private static void handleDeregistration(Message incoming,ObjectOutputStream out,Socket clientSocket) {
        System.out.println("Dereg");
        clients.remove(incoming.getClientInfo());
        registeredClients--;
        Message outgoing = new Message(Status.DE_REGISTER,incoming.getRqNumber(),"Request granted");
        try {
            out.writeObject(outgoing);
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("Closing socket with "+ clientSocket.getInetAddress().getHostAddress());
            //throw new RuntimeException(e);
        }
    }
}
