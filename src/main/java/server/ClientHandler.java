package server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import server.XML.HandleClientXML;

public class ClientHandler implements Runnable {
    private Server server;

    private Socket clientSocket = null;

    private PrintWriter outMessage;
    private Scanner inMessage;

    public ClientHandler(Socket socket, Server server) {
        try {
            server.increaseByOneClientCount();

            this.server = server;
            this.clientSocket = socket;
            this.outMessage = new PrintWriter(socket.getOutputStream(), true);
            this.inMessage = new Scanner(socket.getInputStream());
        } catch (IOException e) {
            closeEverything(clientSocket, inMessage, outMessage);
        }
    }

    public void closeEverything(Socket socket, Scanner scanner, PrintWriter printWriter) {
        // server.removeClient(this);
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
        // server.removeClient(this);
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
        try (
                InputStream inputStream = clientSocket.getInputStream();
                OutputStream outputStream = clientSocket.getOutputStream()) {
            Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8);
            scanner.useDelimiter("<END_OF_MESSAGE>"); // Устанавливаем маркер окончания сообщения

            while (!clientSocket.isClosed() && scanner.hasNext()) {
                // 1. Считываем XML до маркера
                String clientXml = scanner.next().trim(); // Убираем лишние пробелы
                System.out.println("Received on server XML:\n" + clientXml);

                // 2. Обработка XML и генерация ответа
                String responseXml = HandleClientXML.processXml(clientXml);

                // 3. Добавляем маркер <end> к ответу и отправляем клиенту
                responseXml += "<END_OF_MESSAGE>";
                outputStream.write(responseXml.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
