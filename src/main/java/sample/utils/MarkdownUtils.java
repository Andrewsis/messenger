package sample.utils;

import javafx.scene.text.TextFlow;
import javafx.scene.text.Text;
import javafx.scene.control.Hyperlink;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownUtils {
    // Markdown parser for bold, italic, and links
    public static TextFlow parseMarkdownToTextFlow(String text) {
        TextFlow flow = new TextFlow();
        if (text == null)
            return flow;

        // Patterns: **bold**, *italic*, [text](url)
        Pattern pattern = Pattern.compile(
                "(\\*\\*([^*]+)\\*\\*)" + // bold
                        "|(\\*([^*]+)\\*)" + // italic
                        "|(\\[([^\\]]+)\\]\\(([^)]+)\\))" // link
        );
        Matcher matcher = pattern.matcher(text);

        int lastEnd = 0;
        while (matcher.find()) {
            // Plain text before match
            if (matcher.start() > lastEnd) {
                flow.getChildren().add(new Text(text.substring(lastEnd, matcher.start())));
            }
            if (matcher.group(1) != null) { // bold
                Text t = new Text(matcher.group(2));
                t.setStyle("-fx-font-weight: bold;");
                flow.getChildren().add(t);
            } else if (matcher.group(3) != null) { // italic
                Text t = new Text(matcher.group(4));
                t.setStyle("-fx-font-style: italic;");
                flow.getChildren().add(t);
            } else if (matcher.group(5) != null) { // link
                String label = matcher.group(6);
                String url = matcher.group(7);
                Hyperlink link = new Hyperlink(label);
                link.setOnAction(e -> {
                    try {
                        java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
                    } catch (Exception ex) {
                        // ignore
                    }
                });
                link.setStyle("-fx-text-fill: #1976D2;");
                flow.getChildren().add(link);
            }
            lastEnd = matcher.end();
        }
        // Remaining text
        if (lastEnd < text.length()) {
            flow.getChildren().add(new Text(text.substring(lastEnd)));
        }
        return flow;
    }
}