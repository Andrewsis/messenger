package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
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
import javafx.stage.Stage;

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

    public void addChatButtonOnActivation(ActionEvent e) {
        try {
            String requestAllUsersFromDB = ClientRequest.getAllUsersRequest();
            client.sendSystemMessage(requestAllUsersFromDB);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Вызывайте этот метод, когда получите XML <users>...</users> с сервера
    private void showUsersForNewChat(String usersXml) {
        try {
            List<String> users = ClientRequest.parseAllUsers(usersXml);
            users.remove(userName); // убираем себя из списка

            Stage stage = new Stage();
            stage.setTitle("Создать новый чат");

            VBox vbox = new VBox();
            vbox.setPadding(new Insets(10));
            vbox.setSpacing(10);

            // Поля для ввода названия и описания чата
            TextField chatNameField = new TextField();
            chatNameField.setPromptText("Название чата");

            TextField chatDescField = new TextField();
            chatDescField.setPromptText("Описание чата");

            vbox.getChildren().addAll(chatNameField, chatDescField);

            for (String user : users) {
                Label userLabel = new Label(user);
                userLabel.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 8; -fx-background-radius: 8;");
                userLabel.setMaxWidth(Double.MAX_VALUE);

                userLabel.setOnMouseClicked(event -> {
                    try {
                        String chatNameInput = chatNameField.getText().trim();
                        String chatDescInput = chatDescField.getText().trim();
                        String requestXml = ClientRequest.createChatRequest(userName, user,
                                chatNameInput, chatDescInput);
                        client.sendSystemMessage(requestXml);

                        requestXml = ClientRequest.getChatReviewsRequest(userName);
                        client.sendSystemMessage(requestXml);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    stage.close();
                });

                vbox.getChildren().add(userLabel);
            }

            Scene scene = new Scene(vbox, 300, 400);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
            System.out.println("Received on ChatController XML:\n" + responseXml);
            List<Message> messages = ClientRequest.parseChatMessages(responseXml);
            Platform.runLater(() -> {
                // messageContainer.getChildren().clear(); // очищаем старые сообщения

                for (Message msg : messages) {
                    // Форматируем время
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                    String timeText = msg.getTimestamp().format(formatter);

                    // Формируем подпись: имя пользователя и время
                    Label metaLabel = new Label(msg.getUsername() + " • " + timeText);
                    metaLabel.setStyle("-fx-font-size: 10; -fx-text-fill: gray;");

                    Label messageLabel = new Label(msg.getContent());
                    messageLabel.setWrapText(true);
                    messageLabel.setStyle("-fx-background-color: #DCF8C6; -fx-padding: 8; -fx-background-radius: 10;");

                    VBox messageVBox = new VBox(metaLabel, messageLabel);
                    messageVBox.setSpacing(2);

                    HBox messageBox = new HBox(messageVBox);
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
            String loginXml = ClientRequest.sendLoginRequest(this.userName);
            client.sendSystemMessage(loginXml);

            client.setOnMessageReceived(message -> {
                Platform.runLater(() -> {
                    if (message.contains("<chatPreviews>")) {
                        handleChatPreview(message);
                    } // если это XML с чатами
                    else if (message.contains("<messages>")) {
                        addMessageToChat(message); // обычное сообщение
                    } else if (message.contains("<users>")) {
                        showUsersForNewChat(message); // обычное сообщение
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
