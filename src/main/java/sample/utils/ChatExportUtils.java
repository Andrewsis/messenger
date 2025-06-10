package sample.utils;

import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.PrintWriter;
import java.util.List;

public class ChatExportUtils {
    public static void exportChatToTxt(List<HBox> allMessageBoxes, PrintWriter pw) {
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
    }
}