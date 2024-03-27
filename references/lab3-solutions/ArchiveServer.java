package test;


import java.io.*;
import java.net.*;

public class ArchiveServer {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(54321)) { // Replace with actual port
            System.out.println("ArchiveServer listening on port 54321");

            while (true) {
                Socket server = serverSocket.accept();
                handleInventoryData(server);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleInventoryData(Socket server) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
            String inventoryData = in.readLine(); // Read inventory data
            System.out.println("Received inventory data: " + inventoryData);
            // Store inventory data (e.g., write to a file or database)

            // Close resources
            in.close();
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
