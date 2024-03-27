package main;

import java.net.*;

public class UDPEchoServer {
    public static void main(String[] args) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(9876);
            byte[] buffer = new byte[1024];

            // Receive the first message
            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
            socket.receive(request);

            String message = new String(request.getData(), 0, request.getLength());
            System.out.println("Client: " + message);

            InetAddress clientAddress = request.getAddress();
            int clientPort = request.getPort();
            byte[] sendData = message.getBytes();
            DatagramPacket response = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
            socket.send(response);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }
}
