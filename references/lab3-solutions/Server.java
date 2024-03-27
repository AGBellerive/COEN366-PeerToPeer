package test;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 12345;
    private static int registeredClients = 0; // Initialize the variable
    private static List<String> clients = new ArrayList<>();
    
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(4); // Handle up to 4 clients

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server listening on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                executor.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Getter method for registeredClients (optional)
    public static int getRegisteredClients() {
        return registeredClients;
    }

    // Increment method for registeredClients (optional)
    public static void incrementRegisteredClients() {
        registeredClients++;
    }

	public static List<String> getClients() {
		return clients;
	}

	public static void setClients(List<String> clients) {
		Server.clients = clients;
	}

}

class ClientHandler implements Runnable {
    private Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            String message = in.readLine(); // Read client message (e.g., "hello")

            if ("hello".equals(message)) {
                // Register the client
                out.println("registered");
                Server.incrementRegisteredClients(); // Increment the registeredClients variable
                String client = in.readLine(); // Read client message (e.g., "hello")
                Server.getClients().add(client);
                if (Server.getRegisteredClients() == 4) {
                    sendToArchiveServer(Server.getClients().toString());
                }
            }

            // Close resources
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void sendToArchiveServer(String clientData) {
        try (Socket archiveSocket = new Socket("localhost", 54321)) { // Replace with actual ArchiveServer IP and port
            PrintWriter outToArchiveServer = new PrintWriter(archiveSocket.getOutputStream(), true);
            outToArchiveServer.println(clientData);
            // Close resources
            outToArchiveServer.close();
            archiveSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


