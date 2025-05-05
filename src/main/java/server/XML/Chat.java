package server.XML;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import server.DatabaseConnection;

public class Chat {
    public int chatId;
    public String chatName;
    public String lastMessageTimestamp;
    public String lastMessage;

    public Chat(int chatId, String chatName, String lastMessageTimestamp, String lastMessage) {
        this.chatId = chatId;
        this.chatName = chatName;
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.lastMessage = lastMessage;
    }

    public static List<Chat> getAllMessagesInChatsByUser(String userName) throws SQLException {
        List<Chat> chats = new ArrayList<>();

        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();

        PreparedStatement statement = connectDB.prepareStatement("""
                SELECT chats.id, chats.name, messages.timestamp, messages.content
                FROM chats
                JOIN chat_participants ON chats.id = chat_participants.chat_id
                JOIN user_accounts ON user_accounts.id = chat_participants.user_id
                JOIN messages ON chats.id = messages.chat_id
                WHERE user_accounts.username = ?
                ORDER BY chats.name, messages.timestamp DESC;
                """);

        statement.setString(1, userName);
        ResultSet result = statement.executeQuery();

        while (result.next()) {
            chats.add(new Chat(
                    result.getInt("id"),
                    result.getString("name"),
                    result.getString("timestamp"),
                    result.getString("content")));
        }

        result.close();
        statement.close();
        connectDB.close();

        return chats;
    }

    public static List<Chat> getChatPreviewByUser(String userName) throws SQLException {
        List<Chat> chats = new ArrayList<>();

        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();

        PreparedStatement statement = connectDB.prepareStatement("""
                SELECT DISTINCT ON (chats.id)
                chats.id AS chat_id,
                chats.name AS chat_name,
                messages.content AS message_content,
                messages.timestamp AS message_time
                FROM chats
                    JOIN messages ON chats.id = messages.chat_id
                    JOIN chat_participants ON chats.id = chat_participants.chat_id
                    JOIN user_accounts ON user_accounts.id = chat_participants.user_id
                WHERE user_accounts.username = ?
                ORDER BY chats.id, messages.timestamp DESC;
                """);

        statement.setString(1, userName);
        ResultSet result = statement.executeQuery();

        while (result.next()) {
            chats.add(new Chat(
                    result.getInt("chat_id"),
                    result.getString("chat_name"),
                    result.getString("message_time"),
                    result.getString("message_content")));
        }

        result.close();
        statement.close();
        connectDB.close();

        return chats;
    }

}
