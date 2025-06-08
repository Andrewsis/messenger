package common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import server.DatabaseConnection;

public class GroupInfoServ {
    public List<String> members = new ArrayList<>();
    public String groupBio;

    public static GroupInfoServ getGroupInfo(int chatId) throws SQLException {
        GroupInfoServ groupInfo = new GroupInfoServ();

        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();

        // Get group members
        PreparedStatement membersStmt = connectDB.prepareStatement("""
                SELECT user_accounts.username
                FROM chat_participants
                JOIN user_accounts
                    ON chat_participants.user_id = user_accounts.id
                WHERE chat_participants.chat_id = ?
                """);
        membersStmt.setInt(1, chatId);
        ResultSet membersResult = membersStmt.executeQuery();
        while (membersResult.next()) {
            groupInfo.members.add(membersResult.getString("username"));
        }
        membersResult.close();
        membersStmt.close();

        // Get group bio
        PreparedStatement bioStmt = connectDB.prepareStatement("""
                SELECT group_bio AS groupBio
                FROM chats
                WHERE id = ?
                """);
        bioStmt.setInt(1, chatId);
        ResultSet bioResult = bioStmt.executeQuery();
        if (bioResult.next()) {
            groupInfo.groupBio = bioResult.getString("groupBio");
        }
        bioResult.close();
        bioStmt.close();

        connectDB.close();

        return groupInfo;
    }
}
