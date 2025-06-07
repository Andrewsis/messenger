package server.XML;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.*;

import common.ChatPreviewServ;
import common.MessageServ;
import server.DatabaseConnection;

import java.util.List;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class ServerResponse {
    public static String statusResponse(int code, String message) throws Exception {
        // Создаем объект DocumentBuilder
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        // Создаем новый документ
        Document doc = builder.newDocument();

        // Создаем элемент ответа
        Element status = doc.createElement("status");
        status.setAttribute("code", String.valueOf(code));
        status.appendChild(doc.createTextNode(message));

        // Добавляем элемент в корень документа
        doc.appendChild(status);

        // Преобразуем документ в строку
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }

    public static String getMessagesResponse(List<MessageServ> messages) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element chatsElem = doc.createElement("messages");

        for (MessageServ message : messages) {
            Element chatElem = doc.createElement("message");

            Element content = doc.createElement("content");
            content.appendChild(doc.createTextNode(message.getContent()));
            chatElem.appendChild(content);

            Element username = doc.createElement("username");
            username.appendChild(doc.createTextNode(message.getUsername()));
            chatElem.appendChild(username);

            Element timestamp = doc.createElement("lastMessageTimestamp");
            timestamp.appendChild(doc.createTextNode(message.getTimestamp()));
            chatElem.appendChild(timestamp);

            chatsElem.appendChild(chatElem);
        }

        doc.appendChild(chatsElem);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();

    }

    public static String getAllUsersResponse() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        ArrayList<String> users = new ArrayList<>();

        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();

        PreparedStatement statement = connectDB.prepareStatement("""
                SELECT user_accounts.username FROM user_accounts """);

        ResultSet result = statement.executeQuery();

        while (result.next()) {
            users.add(result.getString("username"));
        }

        result.close();
        statement.close();
        connectDB.close();

        Element chatsElem = doc.createElement("users");

        for (String user : users) {
            Element username = doc.createElement("username");
            username.appendChild(doc.createTextNode(user));

            chatsElem.appendChild(username);
        }

        doc.appendChild(chatsElem);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }

    public static String getChatReviewsResponse(List<ChatPreviewServ> chats) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element chatsElem = doc.createElement("chatPreviews");

        for (ChatPreviewServ chat : chats) {
            Element chatElem = doc.createElement("chat");

            Element id = doc.createElement("chatId");
            id.appendChild(doc.createTextNode(String.valueOf(chat.chatId)));
            chatElem.appendChild(id);

            Element name = doc.createElement("chatName");
            name.appendChild(doc.createTextNode(chat.chatName));
            chatElem.appendChild(name);

            Element timestamp = doc.createElement("lastMessageTimestamp");
            timestamp.appendChild(doc.createTextNode(chat.lastMessageTimestamp));
            chatElem.appendChild(timestamp);

            Element message = doc.createElement("lastMessage");
            message.appendChild(doc.createTextNode(chat.lastMessage));
            chatElem.appendChild(message);

            chatsElem.appendChild(chatElem);
        }

        doc.appendChild(chatsElem);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }

    public static String createChatResponse(String ourUsername, String otherUsername, String chatName, String chatDesc)
            throws Exception {

        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();

        // Получаем id пользователей по username
        int ourUserId = -1;
        int otherUserId = -1;

        PreparedStatement userStmt = connectDB.prepareStatement(
                "SELECT id FROM user_accounts WHERE username = ?");
        userStmt.setString(1, ourUsername);
        ResultSet userRs = userStmt.executeQuery();
        if (userRs.next()) {
            ourUserId = userRs.getInt("id");
        }
        userRs.close();

        userStmt.setString(1, otherUsername);
        ResultSet otherUserRs = userStmt.executeQuery();
        if (otherUserRs.next()) {
            otherUserId = otherUserRs.getInt("id");
        }
        otherUserRs.close();
        userStmt.close();

        if (ourUserId == -1 || otherUserId == -1) {
            connectDB.close();
            return statusResponse(400, "User not found");
        }

        // Создаем чат
        PreparedStatement chatStmt = connectDB.prepareStatement(
                "INSERT INTO chats (group_bio, name) VALUES (?, ?) RETURNING id");
        chatStmt.setString(1, chatDesc);
        chatStmt.setString(2, chatName);
        ResultSet chatRs = chatStmt.executeQuery();

        int chatId = -1;
        if (chatRs.next()) {
            chatId = chatRs.getInt("id");
        }
        chatRs.close();
        chatStmt.close();

        if (chatId == -1) {
            connectDB.close();
            return statusResponse(500, "Failed to create chat");
        }

        // Вставляем участников чата
        PreparedStatement partStmt = connectDB.prepareStatement(
                "INSERT INTO chat_participants (chat_id, user_id) VALUES (?, ?), (?, ?)");
        partStmt.setInt(1, chatId);
        partStmt.setInt(2, ourUserId);
        partStmt.setInt(3, chatId);
        partStmt.setInt(4, otherUserId);
        partStmt.executeUpdate();
        partStmt.close();

        // Вставляем первое сообщение в чат
        PreparedStatement msgStmt = connectDB.prepareStatement(
                "INSERT INTO messages (chat_id, sender_id, content) VALUES (?, ?, ?)");
        msgStmt.setInt(1, chatId);
        msgStmt.setInt(2, ourUserId);
        msgStmt.setString(3, "CHAT WAS CREATED");
        msgStmt.executeUpdate();
        msgStmt.close();

        connectDB.close();

        return statusResponse(200, "Chat created successfully: " + chatName + " with " + otherUsername);
    }

    public static void main(String[] args) throws Exception {
        // Пример ответа на запрос сообщений
        List<ChatPreviewServ> chats = ChatPreviewServ.getChatPreview("vanya");
        // String responseXml = messagesResponse(chats);
        // System.out.println(responseXml);

        // // Пример ответа на успешную отправку сообщения
        // String statusXml = statusResponse(200, "Message delivered");
        // System.out.println(statusXml);

        // // Пример ответа на ошибку
        // String errorXml = statusResponse(400, "Invalid chatId");
        // System.out.println(errorXml);
    }
}