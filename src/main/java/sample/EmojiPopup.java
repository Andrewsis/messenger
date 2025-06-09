package sample;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.Popup;
import javafx.stage.Window;

import java.util.Arrays;
import java.util.List;

public class EmojiPopup {
    private static final List<String> EMOJIS = Arrays.asList(
            "😀", "😁", "😂", "🤣", "😃", "😄", "😅", "😆", "😉", "😊",
            "😍", "😘", "😗", "😙", "😚", "🙂", "🤗", "🤔", "😐",
            "😑", "😶", "🙄", "😏", "😣", "😥", "😮", "🤐", "😯", "😪",
            "😫", "😴", "😌", "😛", "😜", "😝", "🤤", "😒", "😓", "😔");

    private final Popup popup = new Popup();

    public EmojiPopup(TextField targetField) {
        FlowPane pane = new FlowPane();
        pane.setHgap(6);
        pane.setVgap(6);
        pane.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-border-radius: 8; -fx-background-radius: 8;");
        for (String emoji : EMOJIS) {
            Button btn = new Button(emoji);
            btn.setStyle("-fx-font-size: 20; -fx-background-radius: 8;");
            btn.setOnAction(e -> {
                int pos = targetField.getCaretPosition();
                targetField.insertText(pos, emoji);
                popup.hide();
            });
            pane.getChildren().add(btn);
        }
        popup.getContent().add(pane);
        popup.setAutoHide(true);
    }

    public void show(Window owner, double x, double y) {
        popup.show(owner, x, y);
    }

    public void hide() {
        popup.hide();
    }
}