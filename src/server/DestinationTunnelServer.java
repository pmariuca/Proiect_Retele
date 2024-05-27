package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DestinationTunnelServer {
    private ServerSocket serverSocket;
    private ExecutorService executorService;

    public DestinationTunnelServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        executorService = Executors.newCachedThreadPool();
        System.out.println("Destination Server listening on port " + port);
    }

    public void start() {
        System.out.println("Destination Tunnel server is running...");
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                executorService.submit(new ClientHandler(clientSocket));
            } catch (IOException e) {
                System.out.println("Error accepting connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (InputStream input = clientSocket.getInputStream();
                 OutputStream output = clientSocket.getOutputStream()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                PrintWriter writer = new PrintWriter(output, true);
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.equals("GET TIME")) {
                        writer.println("Server time: " + new java.util.Date().toString());
                    } else {
                        writer.println("Echo: " + line);
                    }
                }
            } catch (IOException e) {
                System.out.println("Error handling client connection: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.out.println("Error closing socket: " + e.getMessage());
                }
            }
        }
    }

    public static void main(String[] args) {
        int port = 9090;

        try {
            DestinationTunnelServer server = new DestinationTunnelServer(port);
            server.start();
        } catch (IOException e) {
            System.out.println("Error starting the destination server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
