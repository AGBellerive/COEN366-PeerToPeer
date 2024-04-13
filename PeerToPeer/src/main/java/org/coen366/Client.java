package org.coen366;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Scanner;

import static java.lang.System.exit;

public class Client {
    private static DatagramSocket clientSocket;
    private static ClientInfo storedClient;

    private List<ClientInfo> listOfClientInformations;

    private static int CLIENT_PORT = 8080; // is set in code
    private static int SERVER_PORT = 3000; // is set in code

    private static InetAddress clientAddress;

    private static String currentFilePathToPublish;

    private static final Object lock = new Object();

    public static void main(String[] args) {
        System.out.println("Enter the server port you wish to connect to:");
        SERVER_PORT = Integer.parseInt(getUserInput());

        System.out.println("Enter the port you wish to use as the client: ");
        CLIENT_PORT = Integer.parseInt(getUserInput());

        ClientSender clientSender = new ClientSender();
        Thread clientThread = new Thread(clientSender);
        clientThread.start();

        ClientReceiver clientReceiver = new ClientReceiver();
        Thread clientReceiverThread = new Thread(clientReceiver);
        clientReceiverThread.start();
    }

    /**
     * Should handle all logic related to sending messages to the server where user input is required
     */
    static class ClientSender implements Runnable {
        @Override
        public void run() {
            try {
                //Connects to the port specified by the user
                clientSocket = new DatagramSocket(CLIENT_PORT);
//                clientSocket.setSoTimeout(10000); // 10 second timeout (verify if we need to timeout or not)(note: when we timeout, the program crashes after the timeout is reached)
                clientAddress = InetAddress.getLocalHost();


                while (true) {
                    printOptions();
                    String input = getUserInput();
                    switch (input) {
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
            } finally {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            }
        }


        /**
         * @author Sunil
         */
        private static void printOptions() {
            System.out.println("Select an Option: ");
            System.out.println("1. Register");
            System.out.println("2. Publish");
            System.out.println("3. Update");
            System.out.println("4. File transfer between clients");
            System.out.println("5. Update contact information");
            System.out.println("6. Wait to receive a message");
        }

        /**
         * This offers the user options to register or return to main menu
         * 2.1 Registration and De-registration
         *
         * @throws IOException
         * @throws ClassNotFoundException
         */
        private static void printRegisterOptions() throws IOException, ClassNotFoundException, InterruptedException {
            while (true) {
                System.out.println("Select a Register Option: ");
                System.out.println("1. REGISTER");
                System.out.println("2. DE_REGISTER");
                System.out.println("3. Return");
                int input = Integer.parseInt(getUserInput());

                switch (input) {
                    case 1:
                            registerWithServer(Client.clientSocket);
                        break;
                    case 2:
                        if (storedClient != null) {
                            deregisterWithServer();
                            return;
                        } else {
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
         *
         * @param clientSocket the current connection
         * @return the clientInfo and stores it into a static field
         * @throws IOException
         * @throws ClassNotFoundException
         * @author Alex
         */
        private static void registerWithServer(DatagramSocket clientSocket) throws IOException, ClassNotFoundException, InterruptedException {
            System.out.println("Enter your name");
            String name = getUserInput();

            //Creates a client with the entered name and with their ipaddress
            storedClient = new ClientInfo(name, clientAddress, CLIENT_PORT);
            Message outgoingRegister = new Message(Status.REGISTER, storedClient.getRqNum(), storedClient);

            //Sends a message to the server, in this case, it sends a register message
            sendMessageToServer(outgoingRegister);

        }

        /**
         * This method deregisters the user from the server
         *
         * @throws IOException
         * @throws ClassNotFoundException
         */
        private static void deregisterWithServer() throws IOException, ClassNotFoundException {
            Message deRegisterOutgoingMessage = new Message(Status.DE_REGISTER, storedClient.getRqNum(), storedClient);
            sendMessageToServer(deRegisterOutgoingMessage);

        }

        /**
         * This method prints out the options for publishing
         *
         * @throws IOException
         * @throws ClassNotFoundException
         * @throws InterruptedException
         */
        private static void printPublishingOptions() throws IOException, ClassNotFoundException, InterruptedException {
            if (storedClient == null) {
                System.out.println("You must register first before publishing");
                Thread.sleep(1000);
                return;
            }
            while (true) {
                System.out.println("Select a Publishing Option");
                System.out.println("1. PUBLISH");
                System.out.println("2. REMOVE");
                int input = Integer.parseInt(getUserInput());

                switch (input) {
                    case 1:
                        publishFileToServer();
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
         *
         * @throws IOException
         * @throws ClassNotFoundException
         */
        private static void publishFileToServer() throws IOException, ClassNotFoundException {
            System.out.println("Enter the file path you want to publish to the server");
            currentFilePathToPublish = getUserInput();
            File publishedFile = new File(currentFilePathToPublish);

            if (!(publishedFile.exists())) {
                //The file does not exist therefore must tell the user and return back to main
                System.out.println("The file path you entered is not found. Try again. Rerouting you back...");
                return;
            }

            System.out.println("File found.");
            storedClient.addToFiles(currentFilePathToPublish);

            //In stored client, there is a list that contains all the files that the user has. This has to be Extracted and stored on the server side
            Message publishOutgoingMessage = new Message(Status.PUBLISH, storedClient.getRqNum(), storedClient);
            sendMessageToServer(publishOutgoingMessage);
        }

        private static void removeFileFromServer(DatagramSocket clientSocket) throws IOException, ClassNotFoundException {
//TODO
            Message removeOutgoingMessage = new Message(Status.REMOVE, storedClient.getRqNum(), storedClient);
            sendMessageToServer(removeOutgoingMessage);
        }

    }

    /**
     * Should handle all logic related to receiving messages from the server
     */
    static class ClientReceiver implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                        while (storedClient == null) {
                            // don't do anything
                        }
                        Message receivedMessage = getMessageFromServer(storedClient);
                        handleMessage(receivedMessage);
                    }
            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                System.out.println("The connection has an error. Exiting...");
                exit(0);
                throw new RuntimeException(e);
            } finally {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            }
        }

        private static void handleMessage(Message receivedMessage) throws IOException, InterruptedException {
            switch (receivedMessage.getAction()) {
                case REGISTERED:
                    System.out.println("Registration Successful");
                    break;
                case DE_REGISTER:
                    System.out.println(receivedMessage);
                    System.out.println("Deregestration complete");
                    clientSocket.close();
                    exit(0);
                case REGISTER_DENIED:
                    System.out.println(receivedMessage.getAction() + " Request Number: " + storedClient.getRqNum() + " " + receivedMessage.getReason());
                    storedClient = null; // set it back to null since it failed
                    break;
                case PUBLISHED:
                    System.out.println("Publish successful.");
                    break;
                case PUBLISH_DENIED:
                    storedClient.getFiles().remove(currentFilePathToPublish);
                    System.out.println(receivedMessage.getAction() + " Request Number: " + storedClient.getRqNum() + " " + receivedMessage.getReason());
                    break;
                case REMOVED:
                    break;
                case REMOVED_DENIED:
                    break;
                case UPDATE:
                    break;
                case FILE_REQ:
                    break;
                case FILE_CONF:
                    break;
                case FILE_END:
                    break;
                case UPDATE_DENIED:
                    break;
                default:
                    System.out.println("Invalid option");

            }
        }


        /**
         * This method receives the message back from the server
         *
         * @param client
         * @return the message sent from the server
         * @throws IOException
         * @throws ClassNotFoundException
         * @author Alex & Sunil
         */
        private static Message getMessageFromServer(ClientInfo client) throws IOException, ClassNotFoundException {
            //Prepares the space for the message to be stored in
            byte[] buffer = new byte[5000];

            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
            //waits for the message to be received
            clientSocket.receive(request); // will timeout after timeout is reached

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);

            //Increments the rqNum because a rqNum is associated to a specific message
            client.incrementRqNum();

            //converts the buffer into a Message object
            return (Message) objectInputStream.readObject();
        }

    }


    /**
     * This method sends a message object to the server
     *
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

        byte[] sendMessage = byteArrayOutputStream.toByteArray();

        //Sends message to server
        InetAddress serverAddress = InetAddress.getByName("localhost");
        DatagramPacket sendPacket = new DatagramPacket(sendMessage, sendMessage.length, serverAddress, SERVER_PORT);
        clientSocket.send(sendPacket);
    }


    /**
     * @return The users input
     * @author Sunil
     */
    private static String getUserInput() {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }


    //2.4 File transfer between clients (peers)
    //File request
    private void requestFile(InetAddress IPAddressPeer, String fileName  ){
        try{
            //send file request to peer FILE-REQ | RQ# | File-name

            //get current client by using a new client object in the function
            ClientInfo clientInfo = new ClientInfo("client", InetAddress.getLocalHost(),CLIENT_PORT);

            //message object
            Message reqMessage =  new Message(Status.FILE_REQ,clientInfo.getRqNum(), fileName);

            //convert message into byte to send UDP
            //object into byte array, just used to store the serialized message
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            //serialize it
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

            objectOutputStream.writeObject(reqMessage);
            objectOutputStream.flush(); //to make sure the data is immediately sent over

            byte[] transferInfo = byteArrayOutputStream.toByteArray();

            //to send the packet (Lab 2, slide 20...)
            DatagramPacket sendPacket = new DatagramPacket(transferInfo, transferInfo.length, IPAddressPeer, CLIENT_PORT);
            clientSocket.send(sendPacket);

            //Get answer from peer
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);

            //deserialize
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(receivePacket.getData());
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            Message responseMessage = (Message) objectInputStream.readObject();

            // Check if the response confirms the file transfer
            if (responseMessage.getAction() == Status.FILE_CONF) {
                int tcpSocket = Integer.parseInt(responseMessage.getReason()); // TCP socket number
                transferFile(IPAddressPeer, tcpSocket, fileName);
            } else {
                System.out.println("File does not exist at destination or cannot transfer now");
            }

        } catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    //File confirmation



    //File transfer
    private void transferFile(InetAddress IPAddressPeer, int tcpSocket, String fileName){

    }


}