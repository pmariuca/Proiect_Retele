package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimeClient {
    public static void main(String[] args) {
        // se stabileste o conexiune cu serverul pe localhost, portul 8080
        try (Socket socket = new Socket("localhost", 8080);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Enter 'GET TIME' to start receiving time every 20 seconds or 'EXIT' to quit:");
            // se initiaza un ScheduledExecutorService, care va permite programarea taskurilor care se repeta
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

            while (true) {
                String userInput = scanner.nextLine();
                if ("GET TIME".equalsIgnoreCase(userInput)) {
                    scheduler.scheduleAtFixedRate(() -> {
                        out.println("GET TIME");
                        try {
                            String response = in.readLine();
                            System.out.println("Server time: " + response);
                        } catch (IOException e) {
                            System.out.println("Error reading from server: " + e.getMessage());
                        }
                    }, 0, 20, TimeUnit.SECONDS);
                } else if ("exit".equalsIgnoreCase(userInput)) {
                    scheduler.shutdown();
                    break;
                } else {
                    System.out.println("Unknown command. Type 'GET TIME' or 'exit'.");
                }
            }

        } catch (IOException e) {
            System.out.println("Error in client: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
