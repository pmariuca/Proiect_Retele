package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TunnelServer {
    private ServerSocket serverSocket;
    private String destinationIp;
    private int destinationPort;
    private ExecutorService executorService;

    // initializeaza serverul pentru a asculta pe un port local
    // setează IP-ul si portul destinatarului la care datele trebuie sa fie tunelate
    public TunnelServer(int localPort, String destinationIp, int destinationPort) throws IOException {
        this.serverSocket = new ServerSocket(localPort);
        this.destinationIp = destinationIp;
        this.destinationPort = destinationPort;
        this.executorService = Executors.newCachedThreadPool();
        System.out.println("Server listening on port " + localPort);
    }

    // serverul incepe sa ruleze intr-o bucla infinita, accepta conexiuni de la clienti
    // pentru fiecare conexiune client se creeaza o instanta a clasei ConnectionHandler
    // conexiunea este executata intr-un nou thread prin executorService.submit()
    // acest model asigura ca serverul poate gestiona simultan multiple conexiuni client
    public void start() {
        System.out.println("Tunnel server is running...");
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                executorService.submit(new ConnectionHandler(clientSocket, destinationIp, destinationPort));
            } catch (IOException e) {
                System.out.println("Error accepting connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static class ConnectionHandler implements Runnable {
        private Socket clientSocket;
        private String destinationIp;
        private int destinationPort;

        // primeate socketul clientului si detaliile serverului
        public ConnectionHandler(Socket clientSocket, String destinationIp, int destinationPort) {
            this.clientSocket = clientSocket;
            this.destinationIp = destinationIp;
            this.destinationPort = destinationPort;
        }

        // gestioneaza transferul de date intre client si serverul destinatar
        // se creeaza un socket pentru conectarea la server
        // se initializeaza stream-uri pentru a citi si scrie date intre client si destinatar
        // se deschid doua fluxuri de date: unul care citeste de la client si scrie catre destinatar
        //                                  un altul care citeste de la destinatar si scrie inapoi la client
        // fiecare flux ruleaza pe propriul thread
        // datele sunt relayed back and forth pana cand una dintre conexiuni este inchisa sau o eroare intrerupe transferul
        @Override
        public void run() {
            try (Socket destinationSocket = new Socket(destinationIp, destinationPort);
                 InputStream clientInput = clientSocket.getInputStream();
                 OutputStream clientOutput = clientSocket.getOutputStream();
                 InputStream destInput = destinationSocket.getInputStream();
                 OutputStream destOutput = destinationSocket.getOutputStream()) {

                Thread responseThread = new Thread(() -> {
                    try {
                        byte[] buffer = new byte[4096];
                        int length;
                        while ((length = destInput.read(buffer)) != -1) {
                            clientOutput.write(buffer, 0, length);
                        }
                    } catch (IOException e) {
                        System.out.println("Error in reading from destination server: " + e.getMessage());
                    }
                });
                responseThread.start();

                byte[] buffer = new byte[4096];
                int length;
                while ((length = clientInput.read(buffer)) != -1) {
                    destOutput.write(buffer, 0, length);
                }

                responseThread.join();
            } catch (IOException | InterruptedException e) {
                System.out.println("Error handling connection: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.out.println("Error closing client socket: " + e.getMessage());
                }
            }
        }
    }

    // punctul de intrare pentru aplicatie, unde se creeaza o instanta a serverului pe un port local specificat,
    // cu un IP destinatar si un port
    public static void main(String[] args) {
        int localPort = 8080;
        String destinationIp = "127.0.0.1";
        int destinationPort = 9090;

        try {
            TunnelServer server = new TunnelServer(localPort, destinationIp, destinationPort);
            server.start();
        } catch (IOException e) {
            System.out.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
