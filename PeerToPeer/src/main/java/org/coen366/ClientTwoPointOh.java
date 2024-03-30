package org.coen366;

import java.io.*;
import java.net.*;
import java.util.Scanner;

import static java.lang.System.exit;

public class ClientTwoPointOh {
    private static DatagramSocket clientSocket;
    private static ClientInfo storedClient;

    private static int serverPort;

    public static void main(String[] args) {
        System.out.println("Enter the port you wish to connect to (Starting at 3000): ");
        int enteredClientPort = Integer.parseInt(getUserInput());

        System.out.println("Enter the server port you wish to connect to (Starting at 3000): ");
        serverPort = Integer.parseInt(getUserInput());

        try {
            //This connects the user to the specified port ngl idk if it should work like this
            clientSocket = new DatagramSocket(serverPort);
            clientSocket.setSoTimeout(10000);

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
                    storedClient = registerWithServerTwoPointOh(ClientTwoPointOh.clientSocket);
                    break;
                case "2":
                    if(storedClient != null){
//                        deregisterWithServer(ClientTwoPointOh.clientSocket);
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
    private static ClientInfo registerWithServerTwoPointOh(DatagramSocket socket) throws IOException, ClassNotFoundException {
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

    private static Message getMessage(DatagramSocket socket, ClientInfo clientInfo) throws IOException, ClassNotFoundException {
        Message registerMessage = new Message(Status.REGISTER, clientInfo.getRqNum(), clientInfo);

//        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
//        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        //Sends out the register message to the server
//        out.writeObject(registerMessage);

        byte[] sendData = registerMessage.toString().getBytes();
        InetAddress serverAddress = InetAddress.getByName("localhost");
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
        socket.send(sendPacket);

        //This waits for the server to respond back with the confirmation
//        return (Message) in.readObject();


        try
        {      InetAddress address = InetAddress.getByName("localhost");
            ByteArrayOutputStream byteStream = new
                    ByteArrayOutputStream(5000);
            ObjectOutputStream os = new ObjectOutputStream(new
                    BufferedOutputStream(byteStream));
            os.flush();
            os.writeObject(registerMessage);
            os.flush();
            //retrieves byte array
            byte[] sendBuf = byteStream.toByteArray();
            DatagramPacket packet = new DatagramPacket(
                    sendBuf, sendBuf.length, serverAddress, serverPort);
            int byteCount = packet.getLength();
            clientSocket.send(packet);
            os.close();


            // receive response
            byte[] recvBuf = new byte[5000];
            DatagramPacket receivedPacket = new DatagramPacket(recvBuf,
                    recvBuf.length);
            clientSocket.receive(receivedPacket);
            int byteCounts = receivedPacket.getLength();
            ByteArrayInputStream receiveByteStream = new
                    ByteArrayInputStream(recvBuf);
            ObjectInputStream is = new
                    ObjectInputStream(new BufferedInputStream(receiveByteStream));
            Message receivedMessage = (Message)is.readObject();
            is.close();
            System.out.println(receivedMessage);
            return receivedMessage;

        }
        catch (UnknownHostException e)
        {
            System.err.println("Exception:  " + e);
            e.printStackTrace();    }
        catch (IOException e)    { e.printStackTrace();
        }
return null;
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
