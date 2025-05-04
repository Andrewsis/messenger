package sample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.net.URL;
import java.util.ResourceBundle;

public class ChatController implements Initializable {
    @FXML
    private ListView accountListView;

    public void initialize(URL location, ResourceBundle resources) {
        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();

        String connectQuery = "SELECT id, username FROM user_accounts";

        try {
            Statement statement = connectDB.createStatement();
            ResultSet queryResult = statement.executeQuery(connectQuery);

            while (queryResult.next()) {
                int id = queryResult.getInt("id");
                String username = queryResult.getString("username");

                String listOut = id + " " + username;
                accountListView.getItems().add(listOut);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
