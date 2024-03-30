package org.coen366;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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
            // Listen for incoming UDP packets
            while(true) {
                // Receive incoming UDP packet
                System.out.println("Listening for client connections on server port: "+SERVER_PORT);

                serverSocket = new ServerSocket(SERVER_PORT);

                Socket clientSocket = serverSocket.accept();
                System.out.println("Connection from the client "+clientSocket.getInetAddress().getHostAddress());
                SERVER_PORT++;

                handleMessageTwoPointOh(clientSocket);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private static void handleMessageTwoPointOh(Socket clientSocket) throws IOException, ClassNotFoundException {
        while(true){
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
            Message incoming = (Message) in.readObject();

            //Probably can be a case statement
            System.out.println(incoming.getAction().toString());

            if(incoming.getAction().equals(Status.REGISTER)){
                handleRegistration(incoming,out);
            }
            else if(incoming.getAction().equals(Status.DE_REGISTER)){
                clients.remove(incoming.getClientInfo());
                registeredClients--;
                Message outgoing = new Message(Status.DE_REGISTER,incoming.getRqNumber(),"Request granted");
                out.writeObject(outgoing);
                System.out.println("Closing socket with "+ clientSocket.getInetAddress().getHostAddress());
                clientSocket.close();
            }
        }
    }

    private static void handleRegistration(Message incoming,ObjectOutputStream out) throws IOException {
        Message outgoing = checkIfClientExsists( incoming.getClientInfo());
        System.out.println("64");
        System.out.println(outgoing);

        if(outgoing.getAction() == Status.REGISTERED){
            clients.add(incoming.getClientInfo());
            registeredClients++;
            System.out.println("ADDED");
        }
        out.writeObject(outgoing);
    }

    private static Message checkIfClientExsists(ClientInfo client){
        Message outgoing = new Message(Status.REGISTERED,client.getRqNum());
        for (ClientInfo c :clients) {
            if(c.getName().equals(client.getName())){
                outgoing = new Message(Status.REGISTER_DENIED,client.getRqNum(),"This username is taken");
            }
        }
        return outgoing;
    }
}
