package sample;

import common.Message;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;

import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class MessageRendererUtils {
    public static HBox render(Message msg, String currentUserName, double containerWidth) {
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
                messageVBox.getChildren()
                        .add(ImageUtils.createChatImageView(image, () -> ImageUtils.showImageFullscreen(image)));
                messageVBox.getChildren().add(0, metaLabel);
            } catch (Exception ex) {
                Label messageLabel = new Label("[Image error]");
                messageLabel.setWrapText(true);
                messageVBox.getChildren().addAll(metaLabel, messageLabel);
            }
        } else {
            TextFlow messageFlow = MarkdownUtils.parseMarkdownToTextFlow(content);
            if (msg.getUsername().equals(currentUserName)) {
                messageFlow.setStyle("-fx-background-color: #B3E5FC; -fx-padding: 8; -fx-background-radius: 10;");
            } else {
                messageFlow.setStyle("-fx-background-color: #DCF8C6; -fx-padding: 8; -fx-background-radius: 10;");
            }
            messageVBox.getChildren().addAll(metaLabel, messageFlow);
        }

        HBox messageBox = new HBox(messageVBox);
        messageBox.maxWidthProperty().set(containerWidth - 20);
        if (msg.getUsername().equals(currentUserName)) {
            messageBox.setStyle("-fx-alignment: top-right;");
        } else {
            messageBox.setStyle("-fx-alignment: top-left;");
        }
        return messageBox;
    }
}