package com.aiassistant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// What the AI sends back as an answer.
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerDTO {
    private String text;
    private List<String> sources;
    private double confidence;
}
