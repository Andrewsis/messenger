package sample.utils;

import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.List;

public class MessageFilterUtils {
    public static List<HBox> filter(List<HBox> allMessageBoxes, String query) {
        if (query == null || query.isBlank()) {
            return new ArrayList<>(allMessageBoxes);
        }
        String lower = query.toLowerCase();
        List<HBox> filtered = new ArrayList<>();
        for (HBox box : allMessageBoxes) {
            VBox vbox = (VBox) box.getChildren().get(0);
            boolean found = false;
            for (javafx.scene.Node node : vbox.getChildren()) {
                if (node instanceof Label label && label.getText().toLowerCase().contains(lower)) {
                    found = true;
                    break;
                }
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
        return filtered;
    }
}