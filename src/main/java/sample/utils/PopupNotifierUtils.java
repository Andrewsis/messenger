package sample.utils;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

public class PopupNotifierUtils {
    public static void show(Window owner, String text) {
        if (owner == null)
            return;
        Stage popup = new Stage();
        popup.initOwner(owner);
        popup.initStyle(javafx.stage.StageStyle.UNDECORATED);
        popup.setAlwaysOnTop(true);
        Label label = new Label(text);
        label.setStyle(
                "-fx-background-color: #323232; -fx-text-fill: white; -fx-padding: 16; -fx-font-size: 14; -fx-background-radius: 8;");
        Scene scene = new Scene(new VBox(label));
        popup.setScene(scene);
        popup.setX(owner.getX() + owner.getWidth() - 300);
        popup.setY(owner.getY() + owner.getHeight() - 100);
        popup.show();

        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {
            }
            Platform.runLater(popup::close);
        }).start();
    }
}