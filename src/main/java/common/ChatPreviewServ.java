package common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import server.DatabaseConnection;

public class ChatPreviewServ {
    public int chatId;
    public String chatName;
    public String lastMessageTimestamp;
    public String lastMessage;
    public int membersQuantity;

    public ChatPreviewServ(int chatId, String chatName, String lastMessageTimestamp, String lastMessage,
            int membersQuantity) {
        this.chatId = chatId;
        this.chatName = chatName;
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.lastMessage = lastMessage;
        this.membersQuantity = membersQuantity;
    }

    public static List<ChatPreviewServ> getChatPreview(String userName) throws SQLException {
        List<ChatPreviewServ> chats = new ArrayList<>();

        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();

        PreparedStatement statement = connectDB.prepareStatement("""
                SELECT DISTINCT ON (chats.id)
                chats.id AS chat_id,
                chats.name AS chat_name,
                messages.content AS message_content,
                messages.timestamp AS message_time,
                (SELECT COUNT(*)
                    FROM chat_participants cp
                    WHERE cp.chat_id = chats.id
                ) AS members_quantity
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
            chats.add(new ChatPreviewServ(
                    result.getInt("chat_id"),
                    result.getString("chat_name"),
                    result.getString("message_time"),
                    result.getString("message_content"),
                    result.getInt("members_quantity")));
        }

        result.close();
        statement.close();
        connectDB.close();

        return chats;
    }

}
