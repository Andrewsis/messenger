package common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Timestamp;

import server.DatabaseConnection;

public class MessageServ {
    private int chatId;
    private String content;
    private String username;
    private String timestamp;

    public MessageServ(int chatId, String content, String username, String timestamp) {
        this.chatId = chatId;
        this.content = content;
        this.username = username;
        this.timestamp = timestamp;
    }

    public MessageServ(String content, String username, String timestamp) {
        this.content = content;
        this.username = username;
        this.timestamp = timestamp;
    }

    public String getContent() {
        return content;
    }

    public String getUsername() {
        return username;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public static List<MessageServ> getMessages(int chatId) throws SQLException {
        List<MessageServ> messages = new ArrayList<>();

        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();

        PreparedStatement statement = connectDB.prepareStatement("""
                SELECT messages.content AS content,
                messages.timestamp AS timestamp,
                user_accounts.username AS username
                FROM messages
                    JOIN user_accounts ON messages.sender_id = user_accounts.id
                WHERE messages.chat_id = ?
                ORDER BY timestamp ;
                """);

        statement.setInt(1, chatId);
        ResultSet result = statement.executeQuery();

        while (result.next()) {
            messages.add(new MessageServ(
                    result.getString("content"),
                    result.getString("username"),
                    result.getString("timestamp")));
        }

        result.close();
        statement.close();
        connectDB.close();

        return messages;
    }

    public static List<MessageServ> sendMessage(int chatId, String content, String sender)
            throws SQLException {
        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();

        System.out.println("WE ARE INSERTING TO DB");
        System.out.println("Chat ID: " + chatId);
        System.out.println("Sender: " + sender);
        System.out.println("Content: " + content);
        PreparedStatement statement = connectDB.prepareStatement("""
                INSERT INTO messages (chat_id, sender_id, content)
                VALUES (?, (SELECT id FROM user_accounts WHERE username = ?), ?)
                RETURNING timestamp;
                """);

        statement.setInt(1, chatId);
        statement.setString(2, sender);
        statement.setString(3, content);

        ResultSet rs = statement.executeQuery();

        Timestamp timestamp = null;
        if (rs.next()) {
            timestamp = rs.getTimestamp("timestamp");
        }

        rs.close();
        statement.close();
        connectDB.close();

        // Возвращаем объект MessageServ с заполненным timestamp

        List<MessageServ> messages = new ArrayList<>();
        messages.add(new MessageServ(chatId, content, sender, timestamp.toString()));
        return messages;
    }
}
