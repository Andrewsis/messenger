package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import server.Client;
import server.Constants;
import server.DatabaseConnection;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.ResultSet;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;

public class ChatController implements Initializable {
    @FXML
    private ListView<ChatPreview> accountListView;
    @FXML
    private TextField messageTextField;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox messageContainer;

    private String chatName = null;
    private Client client = null;
    public String userName = null;

    public void sendButtonOnActivation(ActionEvent e) {
        String message = messageTextField.getText();
        if (!message.isEmpty()) {
            client.sendMessage(message);
            messageTextField.clear();
        }
    }

    private void addMessageToChat(String message) {
        Platform.runLater(() -> {
            Label messageLabel = new Label(message);
            messageLabel.setWrapText(true);
            messageLabel.setStyle("-fx-background-color: #DCF8C6; -fx-padding: 8; -fx-background-radius: 10;");

            HBox messageBox = new HBox(messageLabel);
            messageBox.setMaxWidth(messageContainer.getWidth());
            messageBox.setStyle("-fx-alignment: top-right;");

            messageContainer.getChildren().add(messageBox);

            scrollPane.layout();
            scrollPane.setVvalue(1.0);
        });
    }

    public void setClientConnection(String userName) {
        this.userName = userName;
        System.out.println("Logged in as: " + userName);

        try {
            Socket socket = new Socket(Constants.IP_ADDR, Constants.PORT);
            this.client = new Client(socket, this.userName);

            client.setOnMessageReceived(message -> {
                Platform.runLater(() -> addMessageToChat(message));
            });
            client.listenForMessage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initialize(URL location, ResourceBundle resources) {
        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();

        accountListView.setCellFactory(param -> new ListCell<ChatPreview>() {
            private final HBox content;
            private final VBox textContainer;
            private final Label chatName;
            private final Label lastMessage;

            {
                chatName = new Label();
                chatName.setStyle("-fx-font-weight: bold;");

                lastMessage = new Label();
                lastMessage.setStyle("-fx-text-fill: gray;");

                textContainer = new VBox(chatName, lastMessage);
                content = new HBox(textContainer);
                content.setSpacing(10);
            }

            @Override
            protected void updateItem(ChatPreview item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    chatName.setText(item.getChatName());
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                    String timeText = item.getTimestamp().format(formatter);
                    lastMessage.setText(item.getLastMessage() + "  •  " + timeText);
                    setGraphic(content);
                }
            }
        });

        String connectQuery = "SELECT DISTINCT ON (chats.id) " +
                "chats.id AS chat_id, " +
                "chats.name AS chat_name, " +
                "messages.content AS message_content, " +
                "messages.timestamp AS message_time " +
                "FROM chats " +
                "JOIN messages ON chats.id = messages.chat_id " +
                "ORDER BY chats.id, messages.timestamp DESC;";

        try {
            Statement statement = connectDB.createStatement();
            ResultSet queryResult = statement.executeQuery(connectQuery);

            while (queryResult.next()) {
                int chatId = queryResult.getInt("chat_id");
                String chatName = queryResult.getString("chat_name");
                String messageContent = queryResult.getString("message_content");
                LocalDateTime timestamp = queryResult.getObject("message_time", LocalDateTime.class);

                ChatPreview preview = new ChatPreview(chatId, chatName, messageContent, timestamp);
                accountListView.getItems().add(preview);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        accountListView.setOnMouseClicked(event -> {
            ChatPreview selectedItem = accountListView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                int chatId = selectedItem.getChatId();
            }
        });
    }

}
