package main;

import java.net.*;
import java.util.Scanner;

public class UDPEchoClient {
    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(5000); // 5 second timeout

            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter message to send to server: ");
            String message = scanner.nextLine();

            byte[] sendData = message.getBytes();
            InetAddress serverAddress = InetAddress.getByName("localhost");
            int serverPort = 9876;

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
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
}
