package main;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class PingPongClient {
    public static final String SERVER_IP = "localhost";
    public static final int SERVER_PORT = 12345;
    public static final long EXCHANGE_DURATION = 5 * 60 * 1000; // 5 minutes in milliseconds

    public static void main(String[] args) {
        try {
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);
            System.out.println("Connected to server.");

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        socket.close();
                        timer.cancel();
                        System.out.println("Connection closed after 5 minutes.");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, EXCHANGE_DURATION);

            while (true) {
                out.println("ping");
                System.out.println("Sent ping");
                String response = in.readLine();
                System.out.println("Received: " + response);
                Thread.sleep(1000); // Wait for a second before sending the next ping
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
