package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket clientSocket;

    private PrintWriter outMessage; // Sending
    private Scanner inMessage; // Getting from server
    public String userName;

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

    public void sendMessage() {
        Scanner consoleScanner = new Scanner(System.in);
        while (clientSocket.isConnected()) {
            String messageToSend = consoleScanner.nextLine();
            outMessage.println(userName + ": " + messageToSend);
        }
        consoleScanner.close();
    }

    public void listenForMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String messageFromChat;

                while (clientSocket.isConnected()) {
                    messageFromChat = inMessage.nextLine();
                    System.out.println(messageFromChat);
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

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Write your username: ");
        String userName = scanner.nextLine();
        Socket socket = new Socket(Constants.IP_ADDR, Constants.PORT);

        Client client = new Client(socket, userName);
        client.outMessage.println(userName); // Send name to server

        client.listenForMessage();
        client.sendMessage();

        scanner.close();
    }
}
