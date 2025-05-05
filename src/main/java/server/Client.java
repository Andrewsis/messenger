package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.function.Consumer;

public class Client {
    private Socket clientSocket;

    private PrintWriter outMessage; // Sending
    private Scanner inMessage; // Getting from server
    public String userName;
    private Consumer<String> onMessageReceived;

    public Client(Socket socket, String userName) {
        try {
            this.clientSocket = socket;
            this.outMessage = new PrintWriter(socket.getOutputStream(), true);
            this.inMessage = new Scanner(socket.getInputStream());
            this.userName = userName;
        } catch (IOException e) {
            closeEverything(clientSocket, inMessage, outMessage);
        }
    }

    public void sendMessage(String message) {
        if (clientSocket.isConnected()) {
            outMessage.println(userName + ": " + message);
        }
    }

    public void setOnMessageReceived(Consumer<String> onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
    }

    public void listenForMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String messageFromChat;

                while (clientSocket.isConnected()) {
                    messageFromChat = inMessage.nextLine();
                    if (onMessageReceived != null) {
                        onMessageReceived.accept(messageFromChat);
                    }
                }
            }
        }).start();
    }

    public void closeEverything(Socket socket, Scanner scanner, PrintWriter printWriter) {
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
}
