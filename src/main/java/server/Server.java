package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    private static Server instance; // Singleton instance

    private static ArrayList<ClientHandler> clients = new ArrayList<>();
    private static int client_count = 0;

    private Server() {
    }

    public static Server getInstance() {
        if (instance == null) {
            instance = new Server();
        }
        return instance;
    }

    public static ArrayList<ClientHandler> getClients() {
        return clients;
    }

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(Constants.PORT);
        System.out.println("Server has started");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Connection established");

            ClientHandler clientHandler = new ClientHandler(clientSocket);
            clients.add(clientHandler);

            new Thread(clientHandler).start();
        }
    }

    public void closeServerSocket(ServerSocket serverSocket) {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void increaseByOneClientCount() {
        client_count++;
    }

    public void decreaseByOneClientCount() {
        client_count--;
    }

    public int getClientCount() {
        return client_count;
    }

    public void sendMessageToAllClients(String message) {
        for (ClientHandler entry : clients) {
            entry.sendMessage(message);
        }
    }

    public void sendMessageToChat(String message, int chatId) {
        for (ClientHandler client : clients) {
            if (client.isInChat(chatId)) {
                System.out.println("Sending message to client in chat " + chatId);
                client.sendMessage(message);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Server.getInstance().start();
    }
}
