package com.aiassistant.adapter;

import com.aiassistant.dto.AnswerDTO;
import com.aiassistant.model.FaqDoc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(name = "model.adapter", havingValue = "local")
public class LocalModelAdapter implements ModelAdapter {

    @Override
    public AnswerDTO generateAnswer(Long clientId, String prompt, List<FaqDoc> relevantDocs, List<String> history) {
        log.warn("LocalModelAdapter is active. It will return a dummy response.");
        String combinedDocs = relevantDocs.stream()
                .map(FaqDoc::getAnswer)
                .reduce("", (a, b) -> a + "\n" + b);
        
        String responseText = "This is a dummy response from the local adapter. " +
                              "Based on the documents, the answer might be related to: " + combinedDocs;
        
        return new AnswerDTO(responseText, List.of("Local Source"), 0.5);
    }

    @Override
    public AnswerDTO generateAnswerWithFallback(Long clientId, String prompt, List<String> history) {
        log.warn("LocalModelAdapter is active. Using fallback response.");
        String responseText = "This is a dummy fallback response. I could not find any relevant documents.";
        return new AnswerDTO(responseText, List.of(), 0.0);
    }

    @Override
    public float[] generateEmbedding(String text) {
        log.warn("LocalModelAdapter is active. It will return a dummy embedding vector.");
        // Return a fixed-size array of zeros as a placeholder.
        return new float[768];
    }
}
