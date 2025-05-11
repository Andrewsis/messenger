package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import server.Client;
import server.Constants;
import server.DatabaseConnection;
import java.sql.Connection;
import java.time.format.DateTimeFormatter;
import java.net.Socket;
import java.net.URL;
import java.util.List;
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
                Platform.runLater(() -> {
                    if (message.contains("<chatPreviews>")) {
                        handleServerResponse(message);
                    } // если это XML с чатами
                    else {
                        addMessageToChat(message); // обычное сообщение
                    }
                });
            });
            client.listenForMessage();

            String requestXml = ClientRequest.getChatReviewsRequest(userName);
            client.sendSystemMessage(requestXml);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initialize(URL location, ResourceBundle resources) {
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

        accountListView.setOnMouseClicked(event -> {
            ChatPreview selectedItem = accountListView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                int chatId = selectedItem.getChatId();
            }
        });
    }

    private void handleServerResponse(String responseXml) {
        try {
            List<ChatPreview> chatPreviews = ClientRequest.parseChatPreviews(responseXml);

            // Обновляем список чатов в интерфейсе
            accountListView.getItems().clear();
            accountListView.getItems().addAll(chatPreviews);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
