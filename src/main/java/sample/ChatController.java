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
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;
import java.io.FileInputStream;
import java.util.Base64;
import java.io.ByteArrayOutputStream;
import javafx.scene.layout.StackPane;
import java.io.FileWriter;
import java.io.PrintWriter;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Popup;
import javafx.scene.text.Text;
import javafx.scene.text.FontWeight;
import javafx.scene.text.FontPosture;
import javafx.scene.text.TextFlow;
import javafx.scene.paint.Color;
import javafx.scene.control.Hyperlink;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    @FXML
    private Button emojiButton; // Кнопка эмодзи, теперь связывается через FXML

    private String chatName = null;
    private Client client = null;
    public String userName = null;
    public int chatId = -1;
    private GroupInfo groupInfo;

    private enum UsersDialogMode {
        CREATE_CHAT, ADD_TO_GROUP
    }

    private Stage mainStage;
    private UsersDialogMode usersDialogMode = UsersDialogMode.CREATE_CHAT;

    private TextField searchTextField; // поле поиска
    private javafx.scene.layout.AnchorPane mainAnchor; // родительский AnchorPane для динамического добавления поля
                                                       // поиска

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

        // Сохраняем ссылку на основной Stage для уведомлений
        Platform.runLater(() -> {
            mainStage = (Stage) messageTextField.getScene().getWindow();
        });

        // --- Поиск: строка поиска ---
        searchTextField = new TextField();
        searchTextField.setPromptText("Поиск по сообщениям...");
        searchTextField.setMinWidth(200);
        searchTextField.setStyle(
                "-fx-background-radius: 8; -fx-padding: 6 10; -fx-font-size: 13; -fx-background-color: white; -fx-effect: dropshadow(gaussian, #888, 8, 0.2, 0, 2);");
        searchTextField.textProperty().addListener((_, __, newVal) -> filterMessages(newVal));
        searchTextField.setOnKeyPressed(ev -> {
            if (ev.getCode() == KeyCode.ESCAPE) {
                hideSearchField();
            }
        });
        searchTextField.focusedProperty().addListener((_, __, newVal) -> {
            if (!newVal)
                hideSearchField();
        });

        // Найдём AnchorPane-родитель scrollPane (mainAnchor)
        Node parent = scrollPane.getParent();
        if (parent instanceof javafx.scene.layout.AnchorPane anchor) {
            mainAnchor = anchor;
        }

        // Глобальный хоткей Ctrl+F (оставьте этот блок для scrollPane, если хотите)
        scrollPane.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.F && event.isControlDown()) {
                showSearchField();
                event.consume();
            }
        });

        // --- Добавьте этот блок для всей сцены ---
        Platform.runLater(() -> {
            mainStage = (Stage) messageTextField.getScene().getWindow();
            if (mainStage != null && mainStage.getScene() != null) {
                mainStage.getScene().addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode() == KeyCode.F && event.isControlDown()) {
                        showSearchField();
                        event.consume();
                    }
                });
            }
        });
        // --- конец нового блока ---

        if (emojiButton != null) {
            emojiButton.setOnAction(e -> showEmojiPopup());
        }
    }

    // --- Всплывающее окно с эмодзи ---
    private void showEmojiPopup() {
        Popup popup = new Popup();
        FlowPane emojiPane = new FlowPane();
        emojiPane.setHgap(6);
        emojiPane.setVgap(6);
        emojiPane.setPadding(new Insets(10));
        emojiPane.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-radius: 8;");

        // Пример набора эмодзи (можно расширить)
        String[] emojis = {
                "😀", "😁", "😂", "🤣", "😃", "😄", "😅", "😆", "😉", "😊",
                "😍", "😘", "😗", "😙", "😚", "🙂", "🤗", "🤔", "😐",
                "😑", "😶", "🙄", "😏", "😣", "😥", "😮", "🤐", "😯", "😪",
                "😫", "😴", "😌", "😛", "😜", "😝", "🤤", "😒", "😓", "😔"
        };

        for (String emoji : emojis) {
            Button btn = new Button(emoji);
            btn.setStyle("-fx-font-size: 20; -fx-background-radius: 8;");
            btn.setOnAction(e -> {
                insertEmojiToTextField(emoji);
                popup.hide();
            });
            emojiPane.getChildren().add(btn);
        }

        popup.getContent().add(emojiPane);
        // Позиционируем popup под кнопкой
        double x = emojiButton.localToScreen(0, 0).getX();
        double y = emojiButton.localToScreen(0, emojiButton.getHeight()).getY();
        popup.show(emojiButton, x, y);
    }

    private void insertEmojiToTextField(String emoji) {
        int caret = messageTextField.getCaretPosition();
        String text = messageTextField.getText();
        StringBuilder sb = new StringBuilder(text);
        sb.insert(caret, emoji);
        messageTextField.setText(sb.toString());
        messageTextField.positionCaret(caret + emoji.length());
        messageTextField.requestFocus();
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

                userLabel.setOnMouseClicked(_ -> {
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

    private void showPopupNotification(String text) {
        if (mainStage == null)
            return;
        Stage popup = new Stage();
        popup.initOwner(mainStage);
        popup.initStyle(javafx.stage.StageStyle.UNDECORATED);
        popup.setAlwaysOnTop(true);
        Label label = new Label(text);
        label.setStyle(
                "-fx-background-color: #323232; -fx-text-fill: white; -fx-padding: 16; -fx-font-size: 14; -fx-background-radius: 8;");
        Scene scene = new Scene(new VBox(label));
        popup.setScene(scene);
        // Position in the bottom right corner
        popup.setX(mainStage.getX() + mainStage.getWidth() - 300);
        popup.setY(mainStage.getY() + mainStage.getHeight() - 100);
        popup.show();
        // Auto close after 3 seconds
        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {
            }
            Platform.runLater(popup::close);
        }).start();
    }

    private List<HBox> allMessageBoxes = new java.util.ArrayList<>();

    private void addMessageToChat(String responseXml) {
        try {
            List<Message> messages = ClientRequest.parseChatMessages(responseXml);
            Platform.runLater(() -> {
                boolean notify = false;
                // If more than one message arrived or the container is empty (first load) —
                // clear and add all
                if (messages.size() > 1 || messageContainer.getChildren().isEmpty()) {
                    messageContainer.getChildren().clear();
                    allMessageBoxes.clear();
                    for (Message msg : messages) {
                        // Format time
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                        String timeText = msg.getTimestamp().format(formatter);

                        // Compose signature: username and time
                        Label metaLabel = new Label(msg.getUsername() + " • " + timeText);
                        metaLabel.setStyle("-fx-font-size: 10; -fx-text-fill: gray;");

                        VBox messageVBox = new VBox();
                        messageVBox.setSpacing(2);

                        String content = msg.getContent();
                        if (content != null && content.startsWith("[image;base64,")) {
                            try {
                                int start = "[image;base64,".length();
                                int end = content.lastIndexOf("]");
                                String base64 = content.substring(start, end);
                                byte[] imageBytes = Base64.getDecoder().decode(base64);
                                Image image = new Image(new java.io.ByteArrayInputStream(imageBytes));
                                messageVBox.getChildren().add(createImageMessageBox(image, metaLabel));
                            } catch (Exception ex) {
                                // If failed to decode, show as text
                                Label messageLabel = new Label("[Image error]");
                                messageLabel.setWrapText(true);
                                messageVBox.getChildren().addAll(metaLabel, messageLabel);
                            }
                        } else {
                            // --- replace Label with TextFlow with markdown ---
                            TextFlow messageFlow = parseMarkdownToTextFlow(content);
                            // --- Message color depends on user ---
                            if (msg.getUsername().equals(userName)) {
                                messageFlow.setStyle(
                                        "-fx-background-color: #B3E5FC; -fx-padding: 8; -fx-background-radius: 10;");
                            } else {
                                messageFlow.setStyle(
                                        "-fx-background-color: #DCF8C6; -fx-padding: 8; -fx-background-radius: 10;");
                            }
                            messageVBox.getChildren().addAll(metaLabel, messageFlow);
                        }

                        HBox messageBox = new HBox(messageVBox);
                        messageBox.maxWidthProperty().bind(messageContainer.widthProperty().subtract(20));
                        if (msg.getUsername().equals(userName)) {
                            messageBox.setStyle("-fx-alignment: top-right;");
                        } else {
                            messageBox.setStyle("-fx-alignment: top-left;");
                        }
                        messageContainer.getChildren().add(messageBox);
                        allMessageBoxes.add(messageBox);
                        // If the message is not from the current user, mark for notification
                        if (!msg.getUsername().equals(userName)) {
                            notify = true;
                        }
                    }
                } else if (messages.size() == 1) {
                    // Add only the new message
                    Message msg = messages.get(0);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                    String timeText = msg.getTimestamp().format(formatter);
                    Label metaLabel = new Label(msg.getUsername() + " • " + timeText);
                    metaLabel.setStyle("-fx-font-size: 10; -fx-text-fill: gray;");
                    VBox messageVBox = new VBox();
                    messageVBox.setSpacing(2);
                    String content = msg.getContent();
                    if (content != null && content.startsWith("[image;base64,")) {
                        try {
                            int start = "[image;base64,".length();
                            int end = content.lastIndexOf("]");
                            String base64 = content.substring(start, end);
                            byte[] imageBytes = Base64.getDecoder().decode(base64);
                            Image image = new Image(new java.io.ByteArrayInputStream(imageBytes));
                            messageVBox.getChildren().add(createImageMessageBox(image, metaLabel));
                        } catch (Exception ex) {
                            // If failed to decode, show as text
                            Label messageLabel = new Label("[Image error]");
                            messageLabel.setWrapText(true);
                            messageVBox.getChildren().addAll(metaLabel, messageLabel);
                        }
                    } else {
                        // --- replace Label with TextFlow with markdown ---
                        TextFlow messageFlow = parseMarkdownToTextFlow(content);
                        // --- Message color depends on user ---
                        if (msg.getUsername().equals(userName)) {
                            messageFlow.setStyle(
                                    "-fx-background-color: #B3E5FC; -fx-padding: 8; -fx-background-radius: 10;");
                        } else {
                            messageFlow.setStyle(
                                    "-fx-background-color: #DCF8C6; -fx-padding: 8; -fx-background-radius: 10;");
                        }
                        messageVBox.getChildren().addAll(metaLabel, messageFlow);
                    }
                    HBox messageBox = new HBox(messageVBox);
                    messageBox.maxWidthProperty().bind(messageContainer.widthProperty().subtract(20));
                    if (msg.getUsername().equals(userName)) {
                        messageBox.setStyle("-fx-alignment: top-right;");
                    } else {
                        messageBox.setStyle("-fx-alignment: top-left;");
                    }
                    messageContainer.getChildren().add(messageBox);
                    allMessageBoxes.add(messageBox);
                    // If the message is not from the current user, mark for notification
                    if (!msg.getUsername().equals(userName)) {
                        notify = true;
                    }
                }
                // Прокручиваем вниз после добавления новых сообщений
                scrollPane.setVvalue(1.0);
                // Показываем уведомление, если окно неактивно и есть новое чужое сообщение
                if (notify && mainStage != null && (!mainStage.isFocused() || mainStage.isIconified())) {
                    showPopupNotification("New message in chat!");
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Markdown парсер для жирного, курсива и ссылок ---
    private TextFlow parseMarkdownToTextFlow(String text) {
        TextFlow flow = new TextFlow();
        if (text == null)
            return flow;

        // Паттерны: **bold**, *italic*, [text](url)
        Pattern pattern = Pattern.compile(
                "(\\*\\*([^*]+)\\*\\*)" + // bold
                        "|(\\*([^*]+)\\*)" + // italic
                        "|(\\[([^\\]]+)\\]\\(([^)]+)\\))" // link
        );
        Matcher matcher = pattern.matcher(text);

        int lastEnd = 0;
        while (matcher.find()) {
            // Обычный текст до совпадения
            if (matcher.start() > lastEnd) {
                flow.getChildren().add(new Text(text.substring(lastEnd, matcher.start())));
            }
            if (matcher.group(1) != null) { // bold
                Text t = new Text(matcher.group(2));
                t.setStyle("-fx-font-weight: bold;");
                flow.getChildren().add(t);
            } else if (matcher.group(3) != null) { // italic
                Text t = new Text(matcher.group(4));
                t.setStyle("-fx-font-style: italic;");
                flow.getChildren().add(t);
            } else if (matcher.group(5) != null) { // link
                String label = matcher.group(6);
                String url = matcher.group(7);
                Hyperlink link = new Hyperlink(label);
                link.setOnAction(e -> {
                    try {
                        java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
                    } catch (Exception ex) {
                        // ignore
                    }
                });
                link.setStyle("-fx-text-fill: #1976D2;");
                flow.getChildren().add(link);
            }
            lastEnd = matcher.end();
        }
        // Остаток текста
        if (lastEnd < text.length()) {
            flow.getChildren().add(new Text(text.substring(lastEnd)));
        }
        return flow;
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
        removeBtn.setOnAction(_ -> {
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
        addBtn.setOnAction(_ -> {
            // Получить всех пользователей, кроме уже в чате
            try {
                usersDialogMode = UsersDialogMode.ADD_TO_GROUP;
                String req = ClientRequest.getAllUsersRequest();
                client.sendSystemMessage(req);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Кнопка сохранить изменения
        javafx.scene.control.Button saveBtn = new javafx.scene.control.Button("Сохранить");
        saveBtn.setOnAction(_ -> {
            // String newName = nameField.getText().trim(); // не используется
            // String newDesc = descField.getText().trim(); // не используется
            // if (!newName.isEmpty()) {
            // String req = ClientRequest.updateChatInfoRequest(chatId, newName, newDesc);
            // client.sendSystemMessage(req);
            // groupName.setText(newName);
            // }
            stage.close();
        });

        // Кнопка экспортировать чат
        javafx.scene.control.Button exportBtn = new javafx.scene.control.Button("Сохранить чат");
        exportBtn.setOnAction(this::exportChatToTxt);

        // Кнопки управления участниками
        HBox membersBtnBox = new HBox(5, removeBtn, addBtn);
        // Кнопки сохранения
        HBox saveBtnBox = new HBox(5, saveBtn, exportBtn);

        vbox.getChildren().addAll(
                new Label("Название:"), nameField,
                new Label("Описание:"), descField,
                membersTitle, membersList,
                membersBtnBox,
                saveBtnBox);

        Scene scene = new Scene(vbox, 350, 400);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    @FXML
    private void sendImageButtonOnActivation(ActionEvent e) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите изображение");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
        File file = fileChooser.showOpenDialog(messageTextField.getScene().getWindow());
        if (file != null) {
            try (FileInputStream fis = new FileInputStream(file)) {
                // Читаем файл в массив байт
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                byte[] imageBytes = baos.toByteArray();
                String base64 = Base64.getEncoder().encodeToString(imageBytes);
                String content = "[image;base64," + base64 + "]";
                // Отправляем как обычное сообщение
                String requestSendMessage = ClientRequest.sendMessageRequest(chatId, userName, content);
                client.sendSystemMessage(requestSendMessage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private VBox createImageMessageBox(Image image, Label metaLabel) {
        ImageView imageView = createChatImageView(image);
        VBox messageVBox = new VBox();
        messageVBox.setSpacing(2);
        messageVBox.getChildren().addAll(metaLabel, imageView);
        return messageVBox;
    }

    private ImageView createChatImageView(Image image) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(220);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);
        imageView.setOnMouseClicked(_ -> showImageFullscreen(image));
        return imageView;
    }

    private void showImageFullscreen(Image image) {
        Stage stage = new Stage();
        stage.setTitle("Просмотр изображения");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setFullScreenExitHint("");
        stage.setFullScreen(true);

        ImageView fullView = new ImageView(image);
        fullView.setPreserveRatio(true);
        fullView.setSmooth(true);
        fullView.setCache(true);
        fullView.fitWidthProperty().bind(stage.widthProperty().subtract(80));
        fullView.fitHeightProperty().bind(stage.heightProperty().subtract(80));

        javafx.scene.control.Button saveBtn = new javafx.scene.control.Button("Скачать");
        saveBtn.setStyle(
                "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 16; -fx-font-size: 14;");
        saveBtn.setOnAction(ev -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Сохранить изображение");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("PNG", "*.png"),
                    new FileChooser.ExtensionFilter("JPEG", "*.jpg", "*.jpeg"),
                    new FileChooser.ExtensionFilter("Все файлы", "*.*"));
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                try {
                    String ext = "png";
                    String fileName = file.getName().toLowerCase();
                    if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg"))
                        ext = "jpg";
                    javax.imageio.ImageIO.write(
                            javafx.embed.swing.SwingFXUtils.fromFXImage(image, null),
                            ext,
                            file);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        // Кнопка в правом верхнем угле
        VBox box = new VBox();
        box.setStyle("-fx-background-color: rgba(20,20,20,0.95); -fx-alignment: center; -fx-padding: 40;");
        javafx.scene.layout.StackPane stack = new javafx.scene.layout.StackPane(fullView);
        StackPane.setAlignment(saveBtn, javafx.geometry.Pos.TOP_RIGHT);
        StackPane.setMargin(saveBtn, new Insets(20, 20, 0, 0));
        stack.getChildren().add(saveBtn);
        box.getChildren().add(stack);
        Scene scene = new Scene(box);
        stage.setScene(scene);
        // Закрытие по клику или Esc
        fullView.setOnMouseClicked(_ -> stage.close());
        scene.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ESCAPE)
                stage.close();
        });
        stage.show();
    }

    // --- Экспорт чата в txt ---
    @FXML
    private void exportChatToTxt(ActionEvent e) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save chat as TXT");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text file", "*.txt"));
        File file = fileChooser.showSaveDialog(messageTextField.getScene().getWindow());
        if (file != null) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(file, false))) {
                for (HBox box : allMessageBoxes) {
                    VBox vbox = (VBox) box.getChildren().get(0);
                    String userAndTime = "";
                    String text = "";
                    for (Node node : vbox.getChildren()) {
                        if (node instanceof Label label) {
                            if (userAndTime.isEmpty()) {
                                userAndTime = label.getText();
                            } else {
                                text = label.getText();
                            }
                        } else if (node instanceof TextFlow flow) {
                            StringBuilder sb = new StringBuilder();
                            for (Node t : flow.getChildren()) {
                                if (t instanceof Text txt) {
                                    sb.append(txt.getText());
                                } else if (t instanceof Hyperlink link) {
                                    sb.append(link.getText());
                                }
                            }
                            text = sb.toString();
                        } else if (node instanceof javafx.scene.control.TextArea area) {
                            text = area.getText();
                        }
                    }
                    if (!userAndTime.isEmpty() && !text.isEmpty()) {
                        pw.println(userAndTime + ": " + text);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void filterMessages(String query) {
        if (query == null || query.isBlank()) {
            messageContainer.getChildren().setAll(allMessageBoxes);
            return;
        }
        String lower = query.toLowerCase();
        List<HBox> filtered = new java.util.ArrayList<>();
        for (HBox box : allMessageBoxes) {
            VBox vbox = (VBox) box.getChildren().get(0);
            boolean found = false;
            for (javafx.scene.Node node : vbox.getChildren()) {
                if (node instanceof Label label && label.getText().toLowerCase().contains(lower)) {
                    found = true;
                    break;
                }
                // --- ищем по содержимому TextFlow ---
                if (node instanceof TextFlow flow) {
                    StringBuilder sb = new StringBuilder();
                    for (javafx.scene.Node t : flow.getChildren()) {
                        if (t instanceof Text txt) {
                            sb.append(txt.getText());
                        } else if (t instanceof Hyperlink link) {
                            sb.append(link.getText());
                        }
                    }
                    if (sb.toString().toLowerCase().contains(lower)) {
                        found = true;
                        break;
                    }
                }
            }
            if (found) {
                filtered.add(box);
            }
        }
        messageContainer.getChildren().setAll(filtered);
    }

    private void showSearchField() {
        if (mainAnchor != null && !mainAnchor.getChildren().contains(searchTextField)) {
            mainAnchor.getChildren().add(searchTextField);
            javafx.scene.layout.AnchorPane.setTopAnchor(searchTextField, 10.0);
            javafx.scene.layout.AnchorPane.setLeftAnchor(searchTextField, 10.0);
            javafx.scene.layout.AnchorPane.setRightAnchor(searchTextField, 10.0);
        }
        searchTextField.setVisible(true);
        searchTextField.setManaged(true);
        searchTextField.requestFocus();
    }

    private void hideSearchField() {
        if (mainAnchor != null) {
            mainAnchor.getChildren().remove(searchTextField);
        }
        searchTextField.clear();
        filterMessages("");
    }
}
