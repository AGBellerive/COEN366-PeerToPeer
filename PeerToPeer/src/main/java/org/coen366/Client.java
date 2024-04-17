package org.coen366;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static java.lang.System.exit;

public class Client {
    private static  InetAddress serverAddress;
    private static DatagramSocket clientSocket;
    private static ClientInfo storedClient;

    private static List<ClientInfo> listOfClientInformationsFromServer = new ArrayList<>();

    private static int CLIENT_PORT = 8080; // is set in code
    private static int SERVER_PORT = 3000; // is set in code
    private static String currentFilePathToPublish;
    private static String currentFilePathToDelete;

    private static final Object lock = new Object();

    public static void main(String[] args) {

        System.out.println("Enter the server port you wish to connect to:");
        SERVER_PORT = Integer.parseInt(getUserInput());

        getServerAddress();

        System.out.println("Enter the port you wish to use as the client: ");
        CLIENT_PORT = Integer.parseInt(getUserInput());

        //Connects to the port specified by the user

        try {
            clientSocket = new DatagramSocket(CLIENT_PORT);
            // clientSocket.setSoTimeout(10000); // 10 second timeout (verify if we need to timeout or not)(note: when we timeout, the program crashes after the timeout is reached)

            InetAddress clientAddress = InetAddress.getLocalHost();
            storedClient = new ClientInfo("", clientAddress, CLIENT_PORT);
        } catch (SocketException | UnknownHostException e) {
            System.out.println("The connection has an error. Exiting...main");
            if (clientSocket != null) {
                clientSocket.close();
            }
            exit(0);
            throw new RuntimeException(e);
        }

        // one thread for sending messages to the server
        ClientSender clientSender = new ClientSender();
        Thread clientThread = new Thread(clientSender);
        clientThread.start();

        // one thread for receiving messages from the server
        ClientReceiver clientReceiver = new ClientReceiver();
        Thread clientReceiverThread = new Thread(clientReceiver);
        clientReceiverThread.start();
    }

    /**
     * This method gets the server address from the user
     * Currently, it is set to localhost for testing purposes
     * The commented out code is for when the user is asked for the server address
     */
    private static void getServerAddress() {
//        System.out.println("Enter the server address: ");
        try {
//            serverAddress = InetAddress.getByName(getUserInput());
            serverAddress = InetAddress.getLocalHost(); // for testing purposes
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Should handle all logic related to sending messages to the server where user input is required
     */
    static class ClientSender implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    printOptions();
                    String input = getUserInput().trim();
                    switch (input) {
                        case "1":
                            printRegisterOptions();
                            break;
                        case "2":
                            printPublishingOptions();
                            break;
                        case "3":
                            for(ClientInfo clientInfo : listOfClientInformationsFromServer){
                                System.out.println(clientInfo);
                            }
                            fileRequestInput();
                            break;
                        case "6":
                            for(ClientInfo clientInfo : listOfClientInformationsFromServer){
                                System.out.println(clientInfo);
                            }

                            break;
                        default:
                            System.out.println("Invalid option");
                    }
                }
            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                System.out.println("The connection has an error. Exiting...sender");
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
            System.out.println("3. File transfer between clients");
            System.out.println("4. Update contact information");
            System.out.println("6. View all clients (for testing if receives clientInfo from server updates)"); // for testing purposes (to see if the client is receiving the list of clients)
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
                String input = getUserInput().trim();

                switch (input) {
                    case "1":
                        registerWithServer();
                        break;
                    case "2":
                        deregisterWithServer();
                        return;
                    case "3":
                        return;
                    default:
                        System.out.println("Invalid option");
                }
            }
        }

        /**
         * This method deals with any registration the user does
         *
         * @return the clientInfo and stores it into a static field
         * @throws IOException
         * @throws ClassNotFoundException
         * @author Alex
         */

        private static void registerWithServer() throws IOException, ClassNotFoundException, InterruptedException {
            if(storedClient.getName() == null || storedClient.getName().isEmpty()) {
                System.out.println("Enter your name");
                String name = getUserInput().trim();

                // Creates a client with the entered name
                storedClient.setName(name);
            }

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
            while (true) {
                System.out.println("Select a Publishing Option");
                System.out.println("1. PUBLISH");
                System.out.println("2. REMOVE");
                System.out.println("3. Return");
                String input = getUserInput().trim();

                switch (input) {
                    case "1":
                        publishFileToServer();
                        break;
                    case "2":
                        removeFileFromServer();
                        break;
                    case "3":
                        return;
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
         */
        private static void publishFileToServer() throws IOException { // might take in multiple files to add
            System.out.println("Enter the file path you want to publish to the server");
            System.out.println("Working directory is " + System.getProperty("user.dir"));
            currentFilePathToPublish = getUserInput().trim();
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

        private static void removeFileFromServer() throws IOException { // Might take in multiple files to remove
            //Ask for a list and comma seperated
            System.out.println("Enter the file path you want to remove from the server");
            currentFilePathToDelete = getUserInput().trim();

            Message removeOutgoingMessage = new Message(Status.REMOVE, storedClient.getRqNum(), storedClient,currentFilePathToDelete);

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
                    Message receivedMessage = getMessageFromServer();
                    handleMessage(receivedMessage);
                }
            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                System.out.println("The connection has an error. Exiting...receiver");
                System.out.println(e.getMessage());
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
                    storedClient.setName("");
                    break;
                case PUBLISHED:
                    System.out.println("Publish successful.");
                    break;
                case PUBLISH_DENIED:
                    storedClient.getFiles().remove(currentFilePathToPublish);
                    System.out.println(receivedMessage.getAction() + " Request Number: " + storedClient.getRqNum() + " " + receivedMessage.getReason());
                    break;
                case REMOVED:
                    storedClient.getFiles().remove(currentFilePathToDelete);
                    System.out.println("File Removed");
                    break;
                case REMOVED_DENIED:
                    System.out.println(receivedMessage.getAction() + " Request Number: " + storedClient.getRqNum() + " " + receivedMessage.getReason());
                    break;
                case UPDATE:
                    System.out.println("Received an update from the server");
                    listOfClientInformationsFromServer = receivedMessage.getListOfClientsInfosForUpdate();
                    break;
                case FILE_REQ:

                    break;
                case FILE_CONF:
                    int tcpSocket = Integer.parseInt(receivedMessage.getReason());// TCP socket number
                    String fileName = receivedMessage.getFile();
                    //transferFile(IPAddressPeer, tcpSocket, fileName);
                    break;
                case FILE_ERROR:
                    System.out.println("File does not exist at destination or cannot transfer now");
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
         * @return the message sent from the server
         * @throws IOException
         * @throws ClassNotFoundException
         * @author Alex & Sunil
         */
        private static Message getMessageFromServer() throws IOException, ClassNotFoundException {
            //Prepares the space for the message to be stored in
            byte[] buffer = new byte[5000];

            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
            //waits for the message to be received
            clientSocket.receive(request); // will timeout after timeout is reached

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);

            //Increments the rqNum because a rqNum is associated to a specific message
            storedClient.incrementRqNum();

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
        DatagramPacket sendPacket = new DatagramPacket(sendMessage, sendMessage.length, serverAddress, SERVER_PORT);
        clientSocket.send(sendPacket);
    }


    /**
     * @return The users input
     * @author Sunil
     */
    private static String getUserInput() {
        Scanner scanner = new Scanner(System.in);
        String input;
        do {
            input = scanner.nextLine();
        } while (input == null || input.isEmpty());
        return input;
    }


    //2.4 File transfer between clients (peers)


    //file request input
    private static void fileRequestInput(){
        Scanner scanner =new Scanner(System.in);
        System.out.println("Enter IP address of desired peer: ");
        String ipAddressPeer = scanner.nextLine();
        //convert to InetAddress
        InetAddress IPAddressPeer = null;
        try {
            IPAddressPeer = InetAddress.getByName(ipAddressPeer);
        } catch (UnknownHostException e){
            e.printStackTrace();
            return;
        }

        System.out.println("Enter the peer's port number: ");
        int peerPort = scanner.nextInt();

        System.out.println("Enter the desire file name: ");
        String fileName = scanner.nextLine();

        requestFile(IPAddressPeer,fileName,peerPort);

    }

    //File request
    //FILE-REQ | RQ# | File-name
    private static void requestFile(InetAddress IPAddressPeer, String fileName, int peerPort) {
        try {

            //message object
            Message reqMessage = new Message(Status.FILE_REQ, storedClient.getRqNum(), fileName);

            //convert message into byte to send UDP
            //object into byte array, just used to store the serialized message
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            //serialize it
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

            objectOutputStream.writeObject(reqMessage);
            objectOutputStream.flush(); //to make sure the data is immediately sent over

            byte[] transferInfo = byteArrayOutputStream.toByteArray();

            //to send the packet (Lab 2, slide 20...) UDP
            DatagramPacket sendPacket = new DatagramPacket(transferInfo, transferInfo.length, IPAddressPeer, peerPort);
            clientSocket.send(sendPacket);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

   //handle file request
    private static void handleFileRequest(Message receivedMessage){

    }

    //check file existance




    //File transfer
    //FILE | RQ# | File-Name | Chunk# | Text
    //FILE-END | RQ# | File-Name | Chunk# | Text
    private static void transferFile(InetAddress IPAddressPeer, int tcpSocket, String fileName) {
        try {
            // Connect to peer's TCP socket
            Socket socket = new Socket(IPAddressPeer, tcpSocket);
            System.out.println("Connected to peer on TCP socket: " + tcpSocket);

            // Open streams for file transfer
            BufferedReader fileReader = new BufferedReader(new FileReader(fileName));
            PrintWriter outputStream = new PrintWriter(socket.getOutputStream(), true);

            // Transfer the file in chunks of 200 characters
            char[] buffer = new char[200];
            int charsRead;
            int chunkNumber = 0;
            int clientRqNum = 1; // Temporary
            while ((charsRead = fileReader.read(buffer)) != -1) {
                chunkNumber++;
                String text = new String(buffer, 0, charsRead);
                Message fileMessage = new Message(Status.FILE, storedClient.getRqNum(), fileName, chunkNumber, text);
                // Serialize the message and send it
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                objectOutputStream.writeObject(fileMessage);
                objectOutputStream.flush();
                byte[] sendMessage = byteArrayOutputStream.toByteArray();
                outputStream.write(new String(sendMessage));
                outputStream.flush();
            }

            // Send FILE_END
            Message fileEndMessage = new Message(Status.FILE_END, storedClient.getRqNum(), fileName, chunkNumber, "");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(fileEndMessage);
            objectOutputStream.flush();
            byte[] sendMessage = byteArrayOutputStream.toByteArray();
            outputStream.write(new String(sendMessage));
            outputStream.flush();

            System.out.println("File transfer complete");

            // Close streams and socket
            fileReader.close();
            outputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}