package com.fooddelivery.communication;

import java.io.Serializable; // To allow sending over ObjectOutputStream if chosen

public class Message implements Serializable {
    private static final long serialVersionUID = 1L; // For Serializable

    private MessageType type;
    private String payload; // JSON string payload

    public Message(MessageType type, String payload) {
        this.type = type;
        this.payload = payload;
    }

    public MessageType getType() {
        return type;
    }

    public String getPayload() {
        return payload;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        // Be careful about logging entire payload if it's very large or sensitive
        return "Message{" +
               "type=" + type +
               ", payload_length=" + (payload != null ? payload.length() : 0) +
               '}';
    }
}
