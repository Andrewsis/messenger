package common;

import java.time.LocalDateTime;

public class Message {
    private String content;
    private String username;
    private LocalDateTime timestamp;

    public Message(String content, String username, LocalDateTime timestamp) {
        this.content = content;
        this.username = username;
        this.timestamp = timestamp;
    }

    public String getContent() {
        return content;
    }

    public String getUsername() {
        return username;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
