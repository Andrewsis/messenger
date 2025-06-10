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
import server.Client;
import server.Constants;

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
        String username = username_textField.getText();
        String password = password_passwordField.getText();

        try {
            String loginRequest = ClientRequest.sendLoginRequest(username, password);

            Client client = new Client(new java.net.Socket(Constants.IP_ADDR, Constants.PORT), username);
            client.sendSystemMessage(loginRequest);

            client.setOnMessageReceived(message -> {
                if (message.contains("code=\"200\"")) {
                    javafx.application.Platform.runLater(() -> {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/chat.fxml"));
                            Parent root = loader.load();
                            ChatController chatController = loader.getController();
                            chatController.setClientConnection(username, client);
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
                    });
                } else {
                    javafx.application.Platform.runLater(() -> loginMessage_label.setText("Login failed."));
                }
            });
            client.listenForMessage();
        } catch (Exception e) {
            e.printStackTrace();
            loginMessage_label.setText("Failed to connect to server.");
        }
    }

}
