package com.aiassistant.adapter;

import com.aiassistant.dto.AnswerDTO;
import com.aiassistant.model.FaqDoc;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ModelAdapter {
    // This is the new method for streaming responses
    Flux<String> generateStreamingAnswer(Long clientId, String prompt, List<FaqDoc> relevantDocs, List<String> history);
    
    // This method will now only be used for the fallback scenario
    AnswerDTO generateAnswerWithFallback(Long clientId, String prompt, List<String> history);
    
    float[] generateEmbedding(String text);
}
