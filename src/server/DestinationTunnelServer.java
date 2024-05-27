package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// a fost folosit TCP (protocol de control al transmisiei) - orientat catre conexiune
public class DestinationTunnelServer {
    private ServerSocket serverSocket;
    private ExecutorService executorService;

    // constructorul initializeaza serverul pe un port specific primit ca parametru
    // creeaza un ServerSocket si un ExecutorService
    // ExecutorService este configurat ca un pool dinamic de thread-uri - se pot gestiona multiple conexiuni simultan
    public DestinationTunnelServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        executorService = Executors.newCachedThreadPool();
        System.out.println("Destination Server listening on port " + port);
    }

    // punctul principal de functionare al serverului
    // se asteapta conexiuni de la client intr-o bucla inifinita
    // la acceptarea unei conexiuni se creeaza si se atribuie un nou thread pentru fiecare client conectat
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

        // constructorul primeste un socket care reprezinta conexiunea clientului
        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        // defineste activitatea pentru fiecare client
        // datele sunt citite de la client printr-un InputStream si sunt procesate si trimise alte date printr-un OutputStream
        // daca clientul trimite mesajul GET TIME se va returna ora actuala, daca nu se va afisa mesajul trimis de client
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

    // punctul de intrare al aplicatiei
    // se creeaza o instanta a serverului pe portul 9090
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
