package org.coen366;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Random;

import static java.lang.System.exit;
import static java.lang.System.setOut;

public class Client {
    private static  InetAddress serverAddress;
    private static DatagramSocket clientSocket;
    private static ClientInfo storedClient;

    private static List<ClientInfo> listOfClientInformationsFromServer = new ArrayList<>();

    private static int CLIENT_PORT; // is set in code
    private static int SERVER_PORT; // is set in code
    private static ArrayList<String> validFilesToPublish;
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
        System.out.println("Enter the server address: ");
        try {
            serverAddress = InetAddress.getByName(getUserInput());
//            serverAddress = InetAddress.getLocalHost(); // for testing purposes
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
                        case "4":
                            updateContactInfo();
                            break;
                        case "5":
                            for(ClientInfo clientInfo : listOfClientInformationsFromServer){
                                System.out.println("Client Name: " + clientInfo.getName() + ", Files: " + clientInfo.getFiles() + ", IP address: " + clientInfo.getIpAddress() + ", Port :" + clientInfo.getClientPort());
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
            System.out.println("5. View all clients (for testing if receives clientInfo from server updates)"); // for testing purposes (to see if the client is receiving the list of clients)
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
                        System.out.println("Alex has not finished implementing this part to take in multiple files to remove.");
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
            System.out.println("Enter the file paths you want to publish to the server. Use Commas to separate each file entry");
            System.out.println("Working directory is " + System.getProperty("user.dir"));
//            currentFilePathToPublish = getUserInput().trim();

            ArrayList<String> filesToPublish = new ArrayList<>(List.of(getUserInput().trim().split(",")));
            validFilesToPublish = new ArrayList<>();

            for(String file : filesToPublish){
                File publishedFile = new File(file);

                if (!(publishedFile.exists())) {
                    System.out.println("The file '" + file +"' is not found.");
                }
                else{
                    System.out.println("File found.");
                    validFilesToPublish.add(file);
                    storedClient.addToFiles(file);
                }
            }

            //In stored client, there is a list that contains all the files that the user has. This has to be Extracted and stored on the server side
            if(!validFilesToPublish.isEmpty()){
                Message publishOutgoingMessage = new Message(Status.PUBLISH, storedClient.getRqNum(), storedClient);
                sendMessageToServer(publishOutgoingMessage);
            }
            else{
                System.out.println("No files to publish. Returning...");
            }
        }

        private static void removeFileFromServer() throws IOException { // Might take in multiple files to remove
            //Ask for a list and comma seperated
            System.out.println("Enter the file paths you want to remove from the server. Use Commas to separate each file entry");
            currentFilePathToDelete = getUserInput().trim();

            Message removeOutgoingMessage = new Message(Status.REMOVE, storedClient.getRqNum(), storedClient,currentFilePathToDelete);

            sendMessageToServer(removeOutgoingMessage);
        }

        private static void updateContactInfo() throws IOException {
            System.out.println("Which one to modify");
            System.out.println("1. IP Address");
            System.out.println("2. UDP Socket");
            System.out.println("3. Both IP Address and UDP Socket");
            System.out.println("4. Return");

            String selection = getUserInput();
            InetAddress ip = storedClient.getIpAddress();
            int udpSocket = storedClient.getClientPort();
            switch(selection) {
                case "1":
                    System.out.println("Enter the new IP Address");
                    ip = InetAddress.getByName(getUserInput());
                    break;
                case "2":
                    System.out.println("Enter the new UDP Socket");
                    udpSocket = Integer.parseInt(getUserInput());
                    break;
                case "3":
                    System.out.println("Enter the new IP Address");
                    ip = InetAddress.getByName(getUserInput());

                    System.out.println("Enter the new UDP Socket");
                    udpSocket = Integer.parseInt(getUserInput());
                    break;
                case "4":
                    return;
                default:
                    System.out.println("Invalid option");
                    return;
            }

            Message messageToSend = new Message(Status.UPDATE_CONTACT, storedClient.getRqNum(), storedClient);
            messageToSend.setNewClientPort(udpSocket);
            messageToSend.setNewIPAddress(ip);

            sendMessageToServer(messageToSend);



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
                    if(receivedMessage.getReason()!=null) {
                        System.out.println("The user has been reconnected");
                    } else {
                        System.out.println("Registration Successful");
                        storedClient.incrementRqNum(); //Increments the rqNum because a rqNum is associated to a specific message
                    }
                    break;
                case DE_REGISTER:
                    System.out.println(receivedMessage);
                    System.out.println("Deregestration complete");
                    storedClient.incrementRqNum();
                    clientSocket.close();
                    exit(0);
                case REGISTER_DENIED:
                    System.out.println(receivedMessage.getAction() + " Request Number: " + storedClient.getRqNum() + " " + receivedMessage.getReason());
                    storedClient.setName("");
                    storedClient.incrementRqNum();
                    break;
                case PUBLISHED:
                    System.out.println("Publish successful.");
                    storedClient.incrementRqNum();
                    break;
                case PUBLISH_DENIED:
                    for(String file : validFilesToPublish){
                        storedClient.getFiles().remove(file);
                    }


                    System.out.println(receivedMessage.getAction() + " Request Number: " + storedClient.getRqNum() + " " + receivedMessage.getReason());
                    storedClient.incrementRqNum();
                    break;
                case REMOVED:
                    //Have a similar loop here to remove the files from the stored client
                    storedClient.getFiles().remove(currentFilePathToDelete);
                    System.out.println("Valid Files Removed");
                    storedClient.incrementRqNum();
                    break;
                case REMOVED_DENIED:
                    System.out.println(receivedMessage.getAction() + " Request Number: " + storedClient.getRqNum() + " " + receivedMessage.getReason());
                    storedClient.incrementRqNum();
                    break;
                case UPDATE:
                    System.out.println("Received an update from the server");
                    listOfClientInformationsFromServer = receivedMessage.getListOfClientsInfosForUpdate();
                    break;
                case FILE_REQ:
                    handleFileRequest(receivedMessage);
                    break;
                case FILE_CONF:
                    handleConfirm(receivedMessage);
                    break;
                case FILE_ERROR:
                    System.out.println("File does not exist at destination or cannot transfer now");
                    break;

                case UPDATE_CONFIRMED:
                    System.out.println("Update Contact Information Confirmed");
                    exit(0); // The user will have to restart the client with the new information to continue or make a new client
                    break;
                case UPDATE_DENIED:
                    System.out.println(receivedMessage.getAction() + " Request Number: " + storedClient.getRqNum() + " " + receivedMessage.getReason());
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
        System.out.println("Enter IP address of desired peer: ");
        String ipAddressPeer = getUserInput();
        //convert to InetAddress
        InetAddress IPAddressPeer = null;
        try {
            IPAddressPeer = InetAddress.getByName(ipAddressPeer);
        } catch (UnknownHostException e){
            e.printStackTrace();
            return;
        }

        System.out.println("Enter the peer's port number: ");
        int peerPort = Integer.parseInt(getUserInput());

        System.out.println("Enter the desire file name: ");
        String fileName = getUserInput();


        System.out.println("Sending request");
        requestFile(IPAddressPeer,fileName,peerPort);
    }

    //File request
    //FILE-REQ | RQ# | File-name
    private static void requestFile(InetAddress IPAddressPeer, String fileName, int peerPort) {
        try {
            //message object
            Message reqMessage = new Message(Status.FILE_REQ, storedClient.getRqNum(), fileName, storedClient);

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

            System.out.println("Request sent");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

   //handle file request, check if file exists
    private static void handleFileRequest(Message receivedMessage) throws IOException {
        System.out.println("checking if file exists");
        String fileName = receivedMessage.getFile();
        InetAddress peerIP = receivedMessage.getClientInfo().getIpAddress();
        int peerPort = receivedMessage.getClientInfo().getClientPort();


       if (checkFileExist(fileName)){
           System.out.println("File Found");
           Random rand = new Random();
           int tcpSocket = rand.nextInt(0, 65535); //tcp socket
           Message message = new Message(Status.FILE_CONF, storedClient.getRqNum(), tcpSocket, storedClient.getIpAddress());
           sendMessageToClient(message, peerIP,peerPort);
           handleFileTransfer(fileName, tcpSocket,peerIP);//file transfer to start

       }else{
           Message failedMessage = new Message(Status.FILE_ERROR, storedClient.getRqNum(),"file not found");
           sendMessageToClient(failedMessage, peerIP,peerPort);
           System.out.println("File NOT Found");
       }

    }

    //check file existance function
    private static boolean checkFileExist(String fileName){
        File file = new File(fileName);
        return file.exists();
    }

    //FILE_CONF
    private static void handleConfirm(Message receivedMessage){
        try{
            int tcpSocketNum = receivedMessage.getTcpSocketNum();
            InetAddress ipAddress = receivedMessage.getClientIPAddress();
            Socket socket = new Socket(ipAddress, tcpSocketNum);

            while(true) {
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                Message reading = (Message) in.readObject();
                System.out.println(reading);

                handleFile(reading);

                if(reading.getAction() == Status.FILE_END){
                    break;
                }
            }
            // continue until FILE_END

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    //File transfer
    //FILE | RQ# | File-Name | Chunk# | Text
    //FILE-END | RQ# | File-Name | Chunk# | Text
    //gets called at handleFileRequest
    private static void handleFileTransfer(String fileName, int tcpSocket, InetAddress peerAddress) {
        try {
            ServerSocket serverSocket = new ServerSocket(tcpSocket);
             Socket socket = serverSocket.accept(); //accept tcp

            Thread thread = new Thread(() -> {
                try {
                    // Send an update message to all clients
                    BufferedReader fileReader = new BufferedReader(new FileReader(fileName));

                    System.out.println("Client connected");

                    char[] buffer = new char[200];
                    int charsRead;
                    int chunkNumber = 0;
                    int clientRqNum = storedClient.getRqNum();

                    while ((charsRead = fileReader.read(buffer)) != -1) {
                        chunkNumber++;
                        String text = new String(buffer, 0, charsRead);
                        System.out.println("sending chunks");
                        Message fileChunkMessage = new Message(Status.FILE, clientRqNum, fileName, chunkNumber, text);
                        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                        out.writeObject(fileChunkMessage);
                    }

                    Message fileEndMessage = new Message(Status.FILE_END, clientRqNum, fileName, chunkNumber, "");
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    out.writeObject(fileEndMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            thread.start();



        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //FILE handler
    private static void handleFile(Message receivedMessage) {
        System.out.println("Handling file chunks");
        String fileName = receivedMessage.getFile();
        int chunkNumber = receivedMessage.getChunkNum();
        String text = receivedMessage.getText();

        // Check if this is the first chunk of the file
        if (chunkNumber == 1) {
            // Create a new file with the given file name
            try (FileWriter fileWriter = new FileWriter(fileName)) {
                // Write the text of the first chunk to the file
                fileWriter.write(text);
                System.out.println("Received first chunk of file: " + fileName);
            } catch (IOException e) {
                System.out.println("Error creating or writing to file: " + e.getMessage());
            }
        } else {
            // Append subsequent chunks to the existing file
            try (FileWriter fileWriter = new FileWriter(fileName, true)) {
                // Write the text of the chunk to the file
                fileWriter.write(text);
                System.out.println("Received chunk " + chunkNumber + " of file: " + fileName);
            } catch (IOException e) {
                System.out.println("Error appending to file: " + e.getMessage());
            }
        }

    }

    //FILE-END


    //created method to send
    private static void sendMessageToClient(Message message, InetAddress address, int port) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream))
        {

                objectOutputStream.writeObject(message);
                objectOutputStream.flush();

                byte[] sendData = byteArrayOutputStream.toByteArray();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
                clientSocket.send(sendPacket);
        }
    }



}