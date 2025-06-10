package sample.utils;

import common.GroupInfo;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sample.ChatController;
import sample.ClientRequest;
import server.Client;

public class GroupEditDialogUtils {
    public static void show(Stage owner, GroupInfo groupInfo, String groupName, String userName, int chatId,
            Client client, ChatController controller) {
        Stage stage = new Stage();
        stage.setTitle("Edit Group");
        stage.initOwner(owner);

        VBox vbox = new VBox();
        vbox.setPadding(new Insets(15));
        vbox.setSpacing(10);

        TextField nameField = new TextField(groupName);
        nameField.setPromptText("Group name");

        TextField descField = new TextField(groupInfo.getGroupBio());
        descField.setPromptText("Group description");

        Label membersTitle = new Label("Members:");
        ListView<String> membersList = new ListView<>();
        membersList.getItems().addAll(groupInfo.getMembers());

        Button removeBtn = new Button("Remove selected");
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

        Button addBtn = new Button("Add member");
        addBtn.setOnAction(_ -> {
            try {
                controller.setUsersDialogModeAddToGroup();
                String req = ClientRequest.getAllUsersRequest();
                client.sendSystemMessage(req);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Button saveBtn = new Button("Save");
        saveBtn.setOnAction(_ -> {
            stage.close();
        });

        Button exportBtn = new Button("Export chat");
        exportBtn.setOnAction(event -> controller.exportChatToTxt(event));

        HBox membersBtnBox = new HBox(5, removeBtn, addBtn);
        HBox saveBtnBox = new HBox(5, saveBtn, exportBtn);

        vbox.getChildren().addAll(
                new Label("Name:"), nameField,
                new Label("Description:"), descField,
                membersTitle, membersList,
                membersBtnBox,
                saveBtnBox);

        Scene scene = new Scene(vbox, 350, 400);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }
}