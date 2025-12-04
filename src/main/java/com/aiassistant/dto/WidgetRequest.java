package com.aiassistant.dto;

import java.util.List;

// What the chat widget sends to our backend.
public class WidgetRequest {
    private String apiKey;
    private String message;
    private List<String> history; // For keeping track of conversation.

    public WidgetRequest() {
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getHistory() {
        return history;
    }

    public void setHistory(List<String> history) {
        this.history = history;
    }
}
