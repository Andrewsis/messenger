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

import java.sql.Connection;
import java.sql.PreparedStatement;
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

        String verifyLogin = "SELECT count(1) FROM user_accounts WHERE username = '" + username
                + "' AND password = '" + password_passwordField.getText() + "'";

        try {
            Statement statement = connectDB.createStatement();
            ResultSet queryResult = statement.executeQuery(verifyLogin);

            while (queryResult.next()) {
                if (queryResult.getInt(1) == 1) {
                    loginMessage_label.setText("Welcome " + username + "!");
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/chat.fxml"));
                        Parent root = loader.load();

                        ChatController chatController = loader.getController();
                        chatController.setClientConnection(username);

                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.setScene(new Scene(root));
                        stage.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        loginMessage_label.setText("Failed to load chat scene.");
                    }

                } else {
                    loginMessage_label.setText("Invalid Login or password");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
