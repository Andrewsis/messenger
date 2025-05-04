package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientHandler implements Runnable {
    private Server server;

    private Socket clientSocket = null;

    private PrintWriter outMessage;
    private Scanner inMessage;
    public String userName;

    public ClientHandler(Socket socket, Server server) {
        try {
            server.increaseByOneClientCount();

            this.server = server;
            this.clientSocket = socket;
            this.outMessage = new PrintWriter(socket.getOutputStream(), true);
            this.inMessage = new Scanner(socket.getInputStream());

            this.userName = inMessage.nextLine();
        } catch (IOException e) {
            closeEverything(clientSocket, inMessage, outMessage);
        }
    }

    public void closeEverything(Socket socket, Scanner scanner, PrintWriter printWriter) {
        server.removeClient(this);
        try {
            if (socket != null) {
                socket.close();
            }
            if (scanner != null) {
                scanner.close();
            }
            if (printWriter != null) {
                printWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        server.removeClient(this);
        server.decreaseByOneClientCount();
        server.sendMessageToAllClients(server.getClientCount() + "");
    }

    public void sendMessage(String message) {
        outMessage.println(message);
    }

    public void sendPersonalMessage(String message, ClientHandler client) {
        client.outMessage.println(message);
    }

    @Override
    public void run() {
        while (true) {
            server.sendMessageToAllClients("NEW CLIENT");
            break;
        }

        while (clientSocket.isConnected()) {

            String clientMessage = inMessage.nextLine();
            if (clientMessage.equals("END")) {
                break;
            }

            System.out.println(clientMessage); // for us
            server.sendMessageToAllClients(clientMessage); // for clients
        }
    }

}
