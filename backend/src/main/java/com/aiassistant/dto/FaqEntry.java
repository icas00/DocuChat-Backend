package com.aiassistant.dto;

import lombok.Data;

// A single question/answer pair for the FAQ upload.
@Data
public class FaqEntry {
    private String question;
    private String answer;
}
