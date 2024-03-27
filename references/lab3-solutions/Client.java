package test;

import java.io.*;
import java.net.*;
import java.util.Date;

public class Client {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 12345); // Replace with actual server IP and port
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Register with the server
            out.println("hello");

            // Wait for acknowledgment from the server
            String response = in.readLine();
            if ("registered".equals(response)) {
                // Send client information (client ID and description)
                String clientId = "Client-id = " + (new Date()).getSeconds(); // Replace with actual client ID
                String description = "Sample client description"; // Replace with actual description
                out.println(clientId + "," + description);
            }

            // Close resources
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


//-----------------------------------
//
//
//You can replace the existing code with the following snippet in order to run it four times in a loop
//
//----------------------------------

/*
import java.io.*;
import java.net.*;
import java.util.Date; 

public class Client {
    public static void main(String[] args) {
        // Create four client threads
        for (int i = 0; i < 4; i++) {
            new Thread(() -> {
                try {
                    Socket socket = new Socket("localhost", 12345); // Replace with actual server IP and port
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    // Register with the server
                    out.println("hello");

                    // Wait for acknowledgment from the server
                    String response = in.readLine();
                    if ("registered".equals(response)) {
                        // Generate a unique client ID (using seconds from the current time)
                        String clientId = "Client-id = " + (new Date()).getSeconds(); // Replace with actual client ID logic
                        String description = "Sample client description"; // Replace with actual description
                        out.println(clientId + "," + description);
                    }

                    // Close resources
                    in.close();
                    out.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}

*/