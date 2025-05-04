package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    private ArrayList<ClientHandler> clients = new ArrayList<>();
    private int client_count = 0;

    public Server() throws IOException {
        ServerSocket serverSocket = new ServerSocket(Constants.PORT);
        System.out.println("Server has started");
        Socket clientSocket = null;
        try {
            while (true) {
                clientSocket = serverSocket.accept();
                System.out.println("Connection established");

                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                System.out.println("USERNAME IS: " + clientHandler.userName);
                clients.add(clientHandler);

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {

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

    // Client logic
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

    public void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        sendMessageToAllClients("SERVER: User " + clientHandler.userName + " has leaved the chat(");
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
    }
}
