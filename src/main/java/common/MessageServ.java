package common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import server.DatabaseConnection;

public class MessageServ {
    private String content;
    private String username;
    private String timestamp;

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

    public static List<MessageServ> getMessagesByChatId(int chat_id) throws SQLException {
        List<MessageServ> messages = new ArrayList<>();

        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();

        PreparedStatement statement = connectDB.prepareStatement("""
                SELECT messages.content AS content,
                messages.timestamp AS timestamp,
                user_accounts.username AS username
                FROM messages
                    JOIN user_accounts ON messages.sender_id = user_accounts.id
                WHERE messages.chat_id = ?;
                """);

        statement.setInt(1, chat_id);
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
}
