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

import common.ChatPreview;
import common.Message;
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
    public int chatId = -1;

    public void sendButtonOnActivation(ActionEvent e) {
        String message = messageTextField.getText();
        if (!message.isEmpty()) {
            try {
                String requestSendMessage = ClientRequest.sendMessageRequest(chatId, userName, message);
                client.sendSystemMessage(requestSendMessage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            // client.sendMessage(message);

            messageTextField.clear();
        }
    }

    private void addMessageToChat(String responseXml) {
        try {
            List<Message> messages = ClientRequest.parseChatMessages(responseXml);
            Platform.runLater(() -> {
                // messageContainer.getChildren().clear(); // очищаем старые сообщения

                for (Message msg : messages) {
                    Label messageLabel = new Label(msg.getContent()); // или msg.getContent()
                    messageLabel.setWrapText(true);
                    messageLabel.setStyle("-fx-background-color: #DCF8C6; -fx-padding: 8; -fx-background-radius: 10;");

                    HBox messageBox = new HBox(messageLabel);
                    messageBox.setMaxWidth(messageContainer.getWidth());

                    // Выравнивание по отправителю
                    if (msg.getUsername().equals(userName)) {
                        messageBox.setStyle("-fx-alignment: top-right;");
                    } else {
                        messageBox.setStyle("-fx-alignment: top-left;");
                    }

                    messageContainer.getChildren().add(messageBox);
                }

                scrollPane.layout();
                scrollPane.setVvalue(1.0);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setClientConnection(String userName) {
        this.userName = userName;
        System.out.println("Logged in as: " + userName);

        try {
            Socket socket = new Socket(Constants.IP_ADDR, Constants.PORT);
            this.client = new Client(socket, this.userName);
            String loginXml = ClientRequest.sendLogin(this.userName);
            client.sendSystemMessage(loginXml);

            client.setOnMessageReceived(message -> {
                Platform.runLater(() -> {
                    if (message.contains("<chatPreviews>")) {
                        handleChatPreview(message);
                    } // если это XML с чатами
                    else if (message.contains("<messages>")) {
                        addMessageToChat(message); // обычное сообщение
                    }
                });
            });
            client.listenForMessage();

            String requestChatPreviewsXml = ClientRequest.getChatReviewsRequest(userName);
            client.sendSystemMessage(requestChatPreviewsXml);

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

        // download messages for selected chat
        accountListView.setOnMouseClicked(event -> {
            ChatPreview selectedItem = accountListView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                chatId = selectedItem.getChatId();
                try {
                    String requestMessagesXml = ClientRequest.getMessagesRequest(chatId);
                    messageContainer.getChildren().clear();

                    client.sendSystemMessage(requestMessagesXml);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void handleChatPreview(String responseXml) {
        try {
            System.out.println("HANDLING CHAT PREVIEWS: " + responseXml);
            List<ChatPreview> chatPreviews = ClientRequest.parseChatPreviews(responseXml);

            System.out.println("chill");
            System.out.println("Chat previews: " + chatPreviews);
            // Обновляем список чатов в интерфейсе
            accountListView.getItems().clear();
            accountListView.getItems().addAll(chatPreviews);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
