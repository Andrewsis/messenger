package server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import server.XML.HandleClientXML;

public class ClientHandler implements Runnable {
    private String userName = null;
    private Socket clientSocket = null;

    private PrintWriter outMessage;
    private Scanner inMessage;

    public ClientHandler(Socket socket) {
        try {
            Server.getInstance().increaseByOneClientCount();

            this.clientSocket = socket;
            this.outMessage = new PrintWriter(socket.getOutputStream(), true);
            this.inMessage = new Scanner(socket.getInputStream(), StandardCharsets.UTF_8);
            this.inMessage.useDelimiter("<END_OF_MESSAGE>");
        } catch (IOException e) {
            closeEverything(clientSocket, inMessage, outMessage);
        }
    }

    public String getUserName() {
        return userName;
    }

    public boolean isInChat(int chatId) {
        if (userName == null)
            return false;
        // Пример для JDBC
        DatabaseConnection connectNow = null;
        try {
            connectNow = new DatabaseConnection();
            try (
                    Connection connectDB = connectNow.getConnection();
                    PreparedStatement stmt = connectDB.prepareStatement(
                            """
                                    SELECT COUNT(*)
                                    FROM chat_participants c
                                    JOIN user_accounts ON c.user_id = user_accounts.id
                                    WHERE c.chat_id = ? AND user_accounts.username = ?
                                            """)) {
                stmt.setInt(1, chatId);
                stmt.setString(2, userName);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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
        Server.getInstance().decreaseByOneClientCount();
        Server.getInstance().sendMessageToAllClients(Server.getInstance().getClientCount() + "");
    }

    public void sendMessage(String message) {
        outMessage.println(message);
    }

    private String extractUserNameFromXml(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            Element root = doc.getDocumentElement();
            return root.getAttribute("userName");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private int extractChatId(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            Element root = doc.getDocumentElement();
            return Integer.parseInt(root.getAttribute("chatId"));
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public void run() {
        try {
            while (!clientSocket.isClosed() && inMessage.hasNext()) {
                String clientXml = inMessage.next().trim();
                // System.out.println("Received on server XML:\n" + clientXml);

                if (userName == null && clientXml.contains("<login")) {
                    this.userName = extractUserNameFromXml(clientXml);
                    System.out.println("User logged in: " + userName);
                }

                String responseXml = HandleClientXML.processXml(clientXml);

                if (clientXml.contains("<sendMessage")) {
                    int chatId = extractChatId(clientXml);
                    // Рассылаем ответ всем участникам чата
                    Server.getInstance().sendMessageToChat(responseXml, chatId);
                } else {
                    System.out.println("Sending response to client: " + responseXml);
                    outMessage.print(responseXml);
                    outMessage.flush();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
