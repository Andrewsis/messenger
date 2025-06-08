package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.Node;
import javafx.stage.Stage;
import server.DatabaseConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class SampleController {

    @FXML
    private Button cancel_button;
    @FXML
    private Label loginMessage_label;
    @FXML
    private TextField username_textField;
    @FXML
    private PasswordField password_passwordField;

    public void login_button_OnAction(ActionEvent event) {
        if (username_textField.getText().isBlank() == false &&
                password_passwordField.getText().isBlank() == false) {
            validateLogin(event);
        } else {
            loginMessage_label.setText("Please enter username and password");
        }
    }

    public void cancel_button_OnAction(ActionEvent e) {
        Stage stage = (Stage) cancel_button.getScene().getWindow();
        stage.close();
    }

    public void validateLogin(ActionEvent event) {
        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();
        String username = username_textField.getText();
        String password = password_passwordField.getText();

        String checkUser = "SELECT password FROM user_accounts WHERE username = '" + username + "'";

        try {
            Statement statement = connectDB.createStatement();
            ResultSet resultSet = statement.executeQuery(checkUser);

            if (resultSet.next()) {
                // User exists, check password
                String dbPassword = resultSet.getString("password");
                if (dbPassword.equals(password)) {
                    // Correct password, proceed to chat
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/chat.fxml"));
                        Parent root = loader.load();

                        ChatController chatController = loader.getController();
                        chatController.setClientConnection(username);

                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.setScene(new Scene(root));

                        stage.setMinWidth(600);
                        stage.setMinHeight(300);
                        stage.setTitle("Chat Application - " + username);

                        stage.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        loginMessage_label.setText("Failed to load chat scene.");
                    }
                } else {
                    // Wrong password
                    loginMessage_label.setText("Wrong password.");
                }
            } else {
                // User does not exist, create new user
                String addUser = "INSERT INTO user_accounts (username, password) VALUES ('"
                        + username + "', '" + password + "')";
                statement.executeUpdate(addUser);

                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/chat.fxml"));
                    Parent root = loader.load();

                    ChatController chatController = loader.getController();
                    chatController.setClientConnection(username);

                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(root));

                    stage.setMinWidth(600);
                    stage.setMinHeight(300);
                    stage.setTitle("Chat Application - " + username);

                    stage.show();
                } catch (Exception e) {
                    e.printStackTrace();
                    loginMessage_label.setText("Failed to load chat scene.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
