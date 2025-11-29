package com.aiassistant.adapter;

import com.aiassistant.dto.AnswerDTO;
import com.aiassistant.model.FaqDoc;

import java.util.List;

public interface ModelAdapter {
    AnswerDTO generateAnswer(Long clientId, String prompt, List<FaqDoc> relevantDocs, List<String> history);
    AnswerDTO generateAnswerWithFallback(Long clientId, String prompt, List<String> history);
    float[] generateEmbedding(String text);
}
