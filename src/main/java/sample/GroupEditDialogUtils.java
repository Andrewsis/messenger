package sample;

import common.GroupInfo;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import server.Client;

public class GroupEditDialogUtils {
    public static void show(Stage owner, GroupInfo groupInfo, String groupName, String userName, int chatId,
            Client client, ChatController controller) {
        Stage stage = new Stage();
        stage.setTitle("Редактировать группу");
        stage.initOwner(owner);

        VBox vbox = new VBox();
        vbox.setPadding(new Insets(15));
        vbox.setSpacing(10);

        TextField nameField = new TextField(groupName);
        nameField.setPromptText("Название группы");

        TextField descField = new TextField(groupInfo.getGroupBio());
        descField.setPromptText("Описание группы");

        Label membersTitle = new Label("Участники:");
        ListView<String> membersList = new ListView<>();
        membersList.getItems().addAll(groupInfo.getMembers());

        Button removeBtn = new Button("Удалить выбранного");
        removeBtn.setOnAction(_ -> {
            String selectedUser = membersList.getSelectionModel().getSelectedItem();
            if (selectedUser != null && !selectedUser.equals(userName)) {
                try {
                    String req = ClientRequest.removeUserFromChatRequest(selectedUser, chatId);
                    client.sendSystemMessage(req);
                    membersList.getItems().remove(selectedUser);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        Button addBtn = new Button("Добавить участника");
        addBtn.setOnAction(_ -> {
            try {
                controller.setUsersDialogModeAddToGroup();
                String req = ClientRequest.getAllUsersRequest();
                client.sendSystemMessage(req);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Button saveBtn = new Button("Сохранить");
        saveBtn.setOnAction(_ -> {
            // Можно реализовать сохранение изменений
            stage.close();
        });

        Button exportBtn = new Button("Сохранить чат");
        exportBtn.setOnAction(event -> controller.exportChatToTxt(event));

        HBox membersBtnBox = new HBox(5, removeBtn, addBtn);
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
}