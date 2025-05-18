package common;

import java.time.LocalDateTime;

public class ChatPreview {
    public int chatId;
    public String chatName;
    public LocalDateTime timestamp;
    public String lastMessage;

    public ChatPreview(int chatId, String chatName, String lastMessage, LocalDateTime timestamp) {
        this.chatId = chatId;
        this.chatName = chatName;
        this.timestamp = timestamp;
        this.lastMessage = lastMessage;
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
        return chatName + ": " + lastMessage + " (" + timestamp + ")";
    }
}
