package org.example;


import java.net.*;
import java.util.Scanner;

public class Client {

private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(10000); // 10 second timeout

//            String message = getUserInput();
            String message = "Sending server port to connect to the server";

            byte[] sendData = message.getBytes();
            InetAddress serverAddress = InetAddress.getByName("localhost");

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, SERVER_PORT);
            socket.send(sendPacket);

            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);

            String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("Server: " + receivedMessage);

            socket.close();
        } catch (SocketTimeoutException e) {
            System.err.println("Timeout occurred: Server did not respond.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getUserInput() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter message to send to server: ");
        String message = scanner.nextLine();
        return message;
    }
}
