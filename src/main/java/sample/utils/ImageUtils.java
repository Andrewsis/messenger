package sample.utils;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.input.KeyCode;

import java.io.File;
import javax.imageio.ImageIO;

public class ImageUtils {

    public static ImageView createChatImageView(Image image, Runnable onClick) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(220);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);
        imageView.setOnMouseClicked(e -> onClick.run());
        return imageView;
    }

    public static void showImageFullscreen(Image image) {
        Stage stage = new Stage();
        stage.setTitle("View Image");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setFullScreenExitHint("");
        stage.setFullScreen(true);

        ImageView fullView = new ImageView(image);
        fullView.setPreserveRatio(true);
        fullView.setSmooth(true);
        fullView.setCache(true);
        fullView.fitWidthProperty().bind(stage.widthProperty().subtract(80));
        fullView.fitHeightProperty().bind(stage.heightProperty().subtract(80));

        Button saveBtn = new Button("Save");
        saveBtn.setStyle(
                "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 16; -fx-font-size: 14;");
        saveBtn.setOnAction(ev -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("PNG", "*.png"),
                    new FileChooser.ExtensionFilter("JPEG", "*.jpg", "*.jpeg"),
                    new FileChooser.ExtensionFilter("All files", "*.*"));
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                try {
                    String ext = "png";
                    String fileName = file.getName().toLowerCase();
                    if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg"))
                        ext = "jpg";
                    ImageIO.write(
                            SwingFXUtils.fromFXImage(image, null),
                            ext,
                            file);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        VBox box = new VBox();
        box.setStyle("-fx-background-color: rgba(20,20,20,0.95); -fx-alignment: center; -fx-padding: 40;");
        StackPane stack = new StackPane(fullView);
        StackPane.setAlignment(saveBtn, javafx.geometry.Pos.TOP_RIGHT);
        StackPane.setMargin(saveBtn, new Insets(20, 20, 0, 0));
        stack.getChildren().add(saveBtn);
        box.getChildren().add(stack);
        Scene scene = new Scene(box);
        stage.setScene(scene);

        fullView.setOnMouseClicked(_ -> stage.close());
        scene.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ESCAPE)
                stage.close();
        });
        stage.show();
    }
}