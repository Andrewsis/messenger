package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import server.Client;
import server.Constants;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.io.IOError;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class ChatController implements Initializable {
    @FXML
    private ListView accountListView;
    @FXML
    private TextField messageTextField;

    private Client client = null;
    public String userName = null;

    public void sendButtonOnActivation(ActionEvent e) {
        System.out.println("Send button pressed");
        String message = messageTextField.getText();
        if (!message.isEmpty()) {
            System.out.println("message: " + message);
            if (client == null) {
                System.out.println("Client is null, creating new client connection.");
            }
            client.sendMessage(message);
            messageTextField.clear();
        } else {
            System.out.println("Please enter a message to send.");
        }

    }

    public void setClientConnection(String userName) {
        this.userName = userName;
        System.out.println("Logged in as: " + userName);

        try {
            Socket socket = new Socket(Constants.IP_ADDR, Constants.PORT);
            this.client = new Client(socket, this.userName);
            client.listenForMessage();
            // client.sendMessage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
