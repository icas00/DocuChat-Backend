package com.aiassistant.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class AnswerDTO {
    private String text;
    private List<String> sources;
    private double similarity;
    private boolean fromCache = false; // Default to false

    public AnswerDTO(String text, List<String> sources, double similarity) {
        this.text = text;
        this.sources = sources;
        this.similarity = similarity;
    }
}
