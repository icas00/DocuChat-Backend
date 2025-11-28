package com.aiassistant.dto;

import lombok.Data;
import java.util.List;

// What the chat widget sends to our backend.
@Data
public class WidgetRequest {
    private String apiKey;
    private String message;
    private List<String> history; // For keeping track of conversation.
}
