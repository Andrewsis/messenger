package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.scene.layout.HBox;
import server.Client;
import server.Constants;
import java.time.format.DateTimeFormatter;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import common.ChatPreview;
import common.GroupInfo;
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
    @FXML
    private Label membersLabel;
    @FXML
    private Label groupName;
    @FXML
    private VBox groupInfoVBox;
    @FXML
    private javafx.scene.control.Button scrollToBottomButton;

    private String chatName = null;
    private Client client = null;
    public String userName = null;
    public int chatId = -1;
    private GroupInfo groupInfo;

    private enum UsersDialogMode {
        CREATE_CHAT, ADD_TO_GROUP
    }

    private UsersDialogMode usersDialogMode = UsersDialogMode.CREATE_CHAT;

    public void initialize(URL location, ResourceBundle resources) {
        groupInfoVBox.setCursor(Cursor.HAND);

        // Изменяем фон при наведении мыши
        groupInfoVBox.setOnMouseEntered(e -> groupInfoVBox.setStyle("-fx-background-color: #e0e0e0;"));
        groupInfoVBox.setOnMouseExited(e -> groupInfoVBox.setStyle(""));

        // Обработчик клика
        groupInfoVBox.setOnMouseClicked(e -> {
            try {
                String requestMessagesXml = ClientRequest.getGroupInfoRequest(chatId);
                client.sendSystemMessage(requestMessagesXml);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        accountListView.setCellFactory(param -> new ListCell<ChatPreview>() {
            private final HBox content;
            private final VBox textContainer;
            private final Label chatName;
            private final Label lastMessage;
            // Конструктор для инициализации элементов интерфейса

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

        messageTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                sendButtonOnActivation(new ActionEvent()); // вызываем тот же метод
            }
        });
        // Also allow scrolling to bottom when chat area is focused and DOWN is pressed
        scrollPane.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DOWN && event.isControlDown()) {
                Platform.runLater(() -> scrollPane.setVvalue(1.0));
            }
            if (event.getCode() == KeyCode.UP && event.isControlDown()) {
                Platform.runLater(() -> scrollPane.setVvalue(0.0));
            }
        });

        // download messages for selected chat
        accountListView.setOnMouseClicked(event -> {
            ChatPreview selectedItem = accountListView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                chatId = selectedItem.getChatId();
                messageTextField.setDisable(false);
                try {
                    String requestMessagesXml = ClientRequest.getMessagesRequest(chatId);
                    messageContainer.getChildren().clear();

                    client.sendSystemMessage(requestMessagesXml);

                    groupName.setText(selectedItem.getChatName());
                    membersLabel.setText("Members: " + selectedItem.getMembersQuantity());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void createChatButtonOnActivation(ActionEvent e) {
        try {
            usersDialogMode = UsersDialogMode.CREATE_CHAT;
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
            // System.out.println("Received on ChatController XML:\n" + responseXml);
            List<Message> messages = ClientRequest.parseChatMessages(responseXml);
            Platform.runLater(() -> {
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
                    messageBox.maxWidthProperty().bind(messageContainer.widthProperty().subtract(20));

                    // Выравнивание по отправителю
                    if (msg.getUsername().equals(userName)) {
                        messageBox.setStyle("-fx-alignment: top-right;");
                    } else {
                        messageBox.setStyle("-fx-alignment: top-left;");
                    }

                    messageContainer.getChildren().add(messageBox);
                }
                // Прокручиваем вниз после добавления новых сообщений
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
                        if (usersDialogMode == UsersDialogMode.CREATE_CHAT) {
                            showUsersForNewChat(message);
                        } else if (usersDialogMode == UsersDialogMode.ADD_TO_GROUP) {
                            showUsersForAddToGroup(message);
                        }
                    } else if (message.contains("<groupInfo>")) {
                        handleGroupInfo(message); // обычное сообщение
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

    private void handleChatPreview(String responseXml) {
        try {
            List<ChatPreview> chatPreviews = ClientRequest.parseChatPreviews(responseXml);

            // Проверяем, остался ли выбранный чат в списке
            boolean chatExists = chatPreviews.stream().anyMatch(c -> c.getChatId() == chatId);
            if (!chatExists) {
                chatId = -1;
                messageContainer.getChildren().clear();
                messageTextField.setDisable(true);
                groupName.setText("");
                membersLabel.setText("");
            } else {
                messageTextField.setDisable(false);
            }

            accountListView.getItems().clear();
            accountListView.getItems().addAll(chatPreviews);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showUsersForAddToGroup(String usersXml) {
        try {
            List<String> users = ClientRequest.parseAllUsers(usersXml);
            // Убираем уже существующих участников группы
            users.removeAll(groupInfo.getMembers());

            Stage stage = new Stage();
            stage.setTitle("Добавить участника в группу");

            VBox vbox = new VBox();
            vbox.setPadding(new Insets(10));
            vbox.setSpacing(10);

            for (String user : users) {
                Label userLabel = new Label(user);
                userLabel.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 8; -fx-background-radius: 8;");
                userLabel.setMaxWidth(Double.MAX_VALUE);

                userLabel.setOnMouseClicked(event -> {
                    try {
                        String req = ClientRequest.addUserToChatRequest(user, chatId);
                        client.sendSystemMessage(req);
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

    private void handleGroupInfo(String responseXml) {
        groupInfo = ClientRequest.parseGroupInfo(responseXml);

        Stage stage = new Stage();
        stage.setTitle("Редактировать группу");

        VBox vbox = new VBox();
        vbox.setPadding(new Insets(15));
        vbox.setSpacing(10);

        // Название группы
        TextField nameField = new TextField(groupName.getText());
        nameField.setPromptText("Название группы");

        // Описание группы
        TextField descField = new TextField(groupInfo.getGroupBio());
        descField.setPromptText("Описание группы");

        // Список участников
        Label membersTitle = new Label("Участники:");
        ListView<String> membersList = new ListView<>();

        membersList.getItems().addAll(groupInfo.getMembers());

        // Кнопка удалить участника
        javafx.scene.control.Button removeBtn = new javafx.scene.control.Button("Удалить выбранного");
        removeBtn.setOnAction(ev -> {
            String selectedUser = membersList.getSelectionModel().getSelectedItem();
            if (selectedUser != null && !selectedUser.equals(userName)) {
                // Отправить запрос на удаление участника
                try {
                    String req = ClientRequest.removeUserFromChatRequest(selectedUser, chatId);
                    client.sendSystemMessage(req);
                    membersList.getItems().remove(selectedUser);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Кнопка добавить участника
        javafx.scene.control.Button addBtn = new javafx.scene.control.Button("Добавить участника");
        addBtn.setOnAction(ev -> {
            // Получить всех пользователей, кроме уже в чате
            try {
                usersDialogMode = UsersDialogMode.ADD_TO_GROUP;
                String req = ClientRequest.getAllUsersRequest();
                client.sendSystemMessage(req);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            // После получения <users> сервером, showUsersForNewChat будет вызван
            // Можно реализовать отдельное окно выбора пользователя для добавления
        });

        // Кнопка сохранить изменения
        javafx.scene.control.Button saveBtn = new javafx.scene.control.Button("Сохранить");
        saveBtn.setOnAction(ev -> {
            String newName = nameField.getText().trim();
            String newDesc = descField.getText().trim();
            // if (!newName.isEmpty()) {
            // String req = ClientRequest.updateChatInfoRequest(chatId, newName, newDesc);
            // client.sendSystemMessage(req);
            // groupName.setText(newName);
            // }
            stage.close();
        });

        vbox.getChildren().addAll(
                new Label("Название:"), nameField,
                new Label("Описание:"), descField,
                membersTitle, membersList,
                new HBox(5, removeBtn, addBtn),
                saveBtn);

        Scene scene = new Scene(vbox, 350, 400);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }
}
