package sample.utils;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.function.BiConsumer;

public class DialogUtils {

    public static void showCreateChatDialog(List<String> users, BiConsumer<String, String[]> onUserSelected) {
        Stage stage = new Stage();
        stage.setTitle("Create new chat");

        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(10);

        TextField chatNameField = new TextField();
        chatNameField.setPromptText("Chat name");

        TextField chatDescField = new TextField();
        chatDescField.setPromptText("Chat description");

        vbox.getChildren().addAll(chatNameField, chatDescField);

        for (String user : users) {
            Label userLabel = new Label(user);
            userLabel.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 8; -fx-background-radius: 8;");
            userLabel.setMaxWidth(Double.MAX_VALUE);

            userLabel.setOnMouseClicked(_ -> {
                String chatNameInput = chatNameField.getText().trim();
                String chatDescInput = chatDescField.getText().trim();
                onUserSelected.accept(user, new String[] { chatNameInput, chatDescInput });
                stage.close();
            });

            vbox.getChildren().add(userLabel);
        }

        Scene scene = new Scene(vbox, 300, 400);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    public static void showAddToGroupDialog(List<String> users, java.util.function.Consumer<String> onUserSelected) {
        Stage stage = new Stage();
        stage.setTitle("Add user to group");

        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(10);

        for (String user : users) {
            Label userLabel = new Label(user);
            userLabel.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 8; -fx-background-radius: 8;");
            userLabel.setMaxWidth(Double.MAX_VALUE);

            userLabel.setOnMouseClicked(event -> {
                onUserSelected.accept(user);
                stage.close();
            });

            vbox.getChildren().add(userLabel);
        }

        Scene scene = new Scene(vbox, 300, 400);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }
}