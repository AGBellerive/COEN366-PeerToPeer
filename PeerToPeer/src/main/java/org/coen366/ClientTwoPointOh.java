package org.coen366;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

import static java.lang.System.exit;

public class ClientTwoPointOh {
    private static Socket socket;
    private static ClientInfo storedClient;

    public static void main(String[] args) {
        System.out.println("Enter the port you wish to connect to (Starting at 3000): ");
        int enteredPort = Integer.parseInt(getUserInput());

        try {
            //This connects the user to the specified port ngl idk if it should work like this
            socket = new Socket("localhost",enteredPort);
            socket.setSoTimeout(10000);

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
            if(socket != null){
                try {
                    socket.close();
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * When the user chooses the option 1, they will be placed in loop of options
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
                    storedClient = registerWithServerTwoPointOh(ClientTwoPointOh.socket);
                    break;
                case "2":
                    if(storedClient != null){
                        deregisterWithServer(ClientTwoPointOh.socket);
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
     * @param socket the current connection
     * @return the clientInfo and stores it into a static field
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static ClientInfo registerWithServerTwoPointOh(Socket socket) throws IOException, ClassNotFoundException {
        System.out.println("Enter your name");
        String name = getUserInput();

        InetAddress clientAddress = InetAddress.getLocalHost();

        //Creates a client with the entered name and with their ipaddress
        ClientInfo clientInfo = new ClientInfo(name,clientAddress);
        //Creates a message of registration
        Message incoming = getMessage(socket, clientInfo);

        System.out.println(incoming);
        return clientInfo;
    }

    private static Message getMessage(Socket socket, ClientInfo clientInfo) throws IOException, ClassNotFoundException {
        Message registerMessage = new Message(Status.REGISTER, clientInfo.getRqNum(), clientInfo);

        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        //Sends out the register message to the server
        out.writeObject(registerMessage);

        //This waits for the server to respond back with the confirmation
        return (Message) in.readObject();
    }

    private static void deregisterWithServer(Socket socket) throws IOException, ClassNotFoundException {
        Message deRegisterMessage = new Message(Status.DE_REGISTER,storedClient.getRqNum(),storedClient);

        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        out.writeObject(deRegisterMessage);

        Message incoming = (Message) in.readObject();
        System.out.println(incoming);
        socket.close();
    }

    private static String getUserInput() {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

    private static void printOptions(){
        System.out.println("Select an Option: ");
        System.out.println("1. Register");
        System.out.println("2. Send random message");
        System.out.println("3. Publish");
        System.out.println("4. Update");
    }


}
