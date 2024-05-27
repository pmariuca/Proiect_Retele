package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class TextClient {
    public static void main(String[] args) {
        // se incearca deschiderea unui socket catre serverul specificat pe portul 8080
        try (Socket socket = new Socket("localhost", 8080);
             // true argumentul în constructor asigura ca PrintWriter va face flush automat dupa fiecare operație de
             // output, asigurand ca datele sunt trimise imediat
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            String userInput;
            System.out.println("Enter messages to send to the server or 'exit' to quit:");
            while (!(userInput = scanner.nextLine()).equalsIgnoreCase("exit")) {
                out.println(userInput);
                String response = in.readLine();
                System.out.println("Server response: " + response);
            }


        } catch (IOException e) {
            System.out.println("Error in client: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
