package sample;

import java.time.LocalDateTime;

public class ChatPreview {
    private String chatName;
    private String lastMessage;
    private LocalDateTime timestamp;
    private int chatId;

    public ChatPreview(int chatId, String chatName, String lastMessage, LocalDateTime timestamp) {
        this.chatName = chatName;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.chatId = chatId;
    }

    public String getChatName() {
        return chatName;
    }

    public int getChatId() {
        return chatId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return chatName + ": " + lastMessage;
    }
}
