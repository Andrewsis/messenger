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
import java.io.File;
import java.io.FileInputStream;
import java.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import javafx.scene.control.Button;

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
    private EmojiPopupUtils emojiPopup;

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
        if (emojiPopup == null) {
            emojiPopup = new EmojiPopupUtils(messageTextField);
        }
        double x = emojiButton.localToScreen(0, 0).getX();
        double y = emojiButton.localToScreen(0, emojiButton.getHeight()).getY();
        emojiPopup.show(emojiButton.getScene().getWindow(), x, y);
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
            users.remove(userName); // remove self

            DialogUtils.showCreateChatDialog(users, (selectedUser, chatInfo) -> {
                try {
                    String chatNameInput = chatInfo[0];
                    String chatDescInput = chatInfo[1];
                    String requestXml = ClientRequest.createChatRequest(userName, selectedUser, chatNameInput,
                            chatDescInput);
                    client.sendSystemMessage(requestXml);

                    requestXml = ClientRequest.getChatReviewsRequest(userName);
                    client.sendSystemMessage(requestXml);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
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
        PopupNotifierUtils.show(mainStage, text);
    }

    private List<HBox> allMessageBoxes = new java.util.ArrayList<>();

    private void addMessageToChat(String responseXml) {
        try {
            List<Message> messages = ClientRequest.parseChatMessages(responseXml);
            Platform.runLater(() -> {
                boolean notify = false;
                if (messages.size() > 1 || messageContainer.getChildren().isEmpty()) {
                    messageContainer.getChildren().clear();
                    allMessageBoxes.clear();
                    for (Message msg : messages) {
                        HBox messageBox = MessageRendererUtils.render(msg, userName, messageContainer.getWidth());
                        messageContainer.getChildren().add(messageBox);
                        allMessageBoxes.add(messageBox);
                        if (!msg.getUsername().equals(userName)) {
                            notify = true;
                        }
                    }
                } else if (messages.size() == 1) {
                    Message msg = messages.get(0);
                    HBox messageBox = MessageRendererUtils.render(msg, userName, messageContainer.getWidth());
                    messageContainer.getChildren().add(messageBox);
                    allMessageBoxes.add(messageBox);
                    if (!msg.getUsername().equals(userName)) {
                        notify = true;
                    }
                }
                scrollPane.setVvalue(1.0);
                if (notify && mainStage != null && (!mainStage.isFocused() || mainStage.isIconified())) {
                    showPopupNotification("New message in chat!");
                }
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
            users.removeAll(groupInfo.getMembers());

            DialogUtils.showAddToGroupDialog(users, selectedUser -> {
                try {
                    String req = ClientRequest.addUserToChatRequest(selectedUser, chatId);
                    client.sendSystemMessage(req);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setUsersDialogModeAddToGroup() {
        usersDialogMode = UsersDialogMode.ADD_TO_GROUP;
    }

    private void handleGroupInfo(String responseXml) {
        groupInfo = ClientRequest.parseGroupInfo(responseXml);
        GroupEditDialogUtils.show(mainStage, groupInfo, groupName.getText(), userName, chatId, client, this);
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

    // --- Экспорт чата в txt ---
    @FXML
    public void exportChatToTxt(ActionEvent e) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save chat as TXT");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text file", "*.txt"));
        File file = fileChooser.showSaveDialog(messageTextField.getScene().getWindow());
        if (file != null) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(file, false))) {
                ChatExportUtils.exportChatToTxt(allMessageBoxes, pw);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void filterMessages(String query) {
        messageContainer.getChildren().setAll(
                MessageFilterUtils.filter(allMessageBoxes, query));
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
