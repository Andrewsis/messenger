package server.XML;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.*;

import common.ChatPreviewServ;
import common.GroupInfoServ;
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

            Element membersQuantity = doc.createElement("membersQuantity");
            membersQuantity.appendChild(doc.createTextNode(String.valueOf(chat.membersQuantity)));
            chatElem.appendChild(membersQuantity);

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

    public static String getGroupInfoResponse(GroupInfoServ groupInfo) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element groupInfoElem = doc.createElement("groupInfo");

        // Добавляем участников группы
        Element membersElem = doc.createElement("members");
        for (String member : groupInfo.members) {
            Element memberElem = doc.createElement("member");
            memberElem.appendChild(doc.createTextNode(member));
            membersElem.appendChild(memberElem);
        }
        groupInfoElem.appendChild(membersElem);

        // Добавляем описание группы
        Element bioElem = doc.createElement("groupBio");
        bioElem.appendChild(doc.createTextNode(groupInfo.groupBio));
        groupInfoElem.appendChild(bioElem);

        doc.appendChild(groupInfoElem);

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

    public static String removeUserFromChatResponse(String username, int chatId) throws Exception {
        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();

        // Получаем id пользователя по username
        int userId = -1;
        PreparedStatement userStmt = connectDB.prepareStatement(
                "SELECT id FROM user_accounts WHERE username = ?");
        userStmt.setString(1, username);
        ResultSet userRs = userStmt.executeQuery();
        if (userRs.next()) {
            userId = userRs.getInt("id");
        }
        userRs.close();
        userStmt.close();

        if (userId == -1) {
            connectDB.close();
            return statusResponse(400, "User not found");
        }

        // Удаляем участника из чата
        PreparedStatement partStmt = connectDB.prepareStatement(
                "DELETE FROM chat_participants WHERE chat_id = ? AND user_id = ?");
        partStmt.setInt(1, chatId);
        partStmt.setInt(2, userId);
        int rowsAffected = partStmt.executeUpdate();
        partStmt.close();

        connectDB.close();

        if (rowsAffected > 0) {
            return statusResponse(200, "User removed from chat successfully");
        } else {
            return statusResponse(400, "User not found in chat");
        }
    }

    public static String addUserToChatResponse(String username, int chatId) throws Exception {
        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();

        int userId = -1;
        PreparedStatement userStmt = connectDB.prepareStatement(
                "SELECT id FROM user_accounts WHERE username = ?");
        userStmt.setString(1, username);
        ResultSet userRs = userStmt.executeQuery();
        if (userRs.next()) {
            userId = userRs.getInt("id");
        }
        userRs.close();
        userStmt.close();

        if (userId == -1) {
            connectDB.close();
            return statusResponse(400, "User not found");
        }

        PreparedStatement checkStmt = connectDB.prepareStatement(
                "SELECT COUNT(*) FROM chat_participants WHERE chat_id = ? AND user_id = ?");
        checkStmt.setInt(1, chatId);
        checkStmt.setInt(2, userId);
        ResultSet checkRs = checkStmt.executeQuery();
        checkRs.next();
        int count = checkRs.getInt(1);
        checkRs.close();
        checkStmt.close();

        if (count > 0) {
            connectDB.close();
            return statusResponse(400, "User already in chat");
        }

        PreparedStatement partStmt = connectDB.prepareStatement(
                "INSERT INTO chat_participants (chat_id, user_id) VALUES (?, ?)");
        partStmt.setInt(1, chatId);
        partStmt.setInt(2, userId);
        partStmt.executeUpdate();
        partStmt.close();

        connectDB.close();

        return statusResponse(200, "User added to chat successfully");
    }

    public static String loginResponse(String userName, String password) throws Exception {
        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();
        PreparedStatement stmt = connectDB.prepareStatement("SELECT password FROM user_accounts WHERE username = ?");
        stmt.setString(1, userName);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            String dbPassword = rs.getString("password");
            rs.close();
            stmt.close();
            connectDB.close();
            if (dbPassword.equals(password)) {
                return statusResponse(200, "Login successful");
            } else {
                return statusResponse(400, "Wrong password");
            }
        } else {
            rs.close();
            stmt.close();
            PreparedStatement insertStmt = connectDB
                    .prepareStatement("INSERT INTO user_accounts (username, password) VALUES (?, ?)");
            insertStmt.setString(1, userName);
            insertStmt.setString(2, password);
            insertStmt.executeUpdate();
            insertStmt.close();
            connectDB.close();
            return statusResponse(200, "User created and logged in");
        }
    }

    public static void main(String[] args) throws Exception {

        List<ChatPreviewServ> chats = ChatPreviewServ.getChatPreview("vanya");
        // String responseXml = messagesResponse(chats);
        // System.out.println(responseXml);

        // String statusXml = statusResponse(200, "Message delivered");
        // System.out.println(statusXml);

        // String errorXml = statusResponse(400, "Invalid chatId");
        // System.out.println(errorXml);
    }
}