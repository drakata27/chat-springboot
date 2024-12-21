package com.aleksdraka.chatwebsocket.chat;

public class ChatMessage {
    private String content;
    private String sender;
    private MessageType type;

    public ChatMessage() {
    }

    public ChatMessage(String content, String sender, MessageType type) {
        this.content = content;
        this.sender = sender;
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    // Builder class
    public static class Builder {
        private String content;
        private String sender;
        private MessageType type;

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder sender(String sender) {
            this.sender = sender;
            return this;
        }

        public Builder type(MessageType type) {
            this.type = type;
            return this;
        }

        public ChatMessage build() {
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setContent(content);
            chatMessage.setSender(sender);
            chatMessage.setType(type);
            return chatMessage;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
