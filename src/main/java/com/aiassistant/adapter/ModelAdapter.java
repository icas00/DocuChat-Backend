package com.aiassistant.adapter;

import com.aiassistant.dto.AnswerDTO;
import com.aiassistant.model.FaqDoc;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ModelAdapter {
    // This is the new method for streaming responses
    Flux<String> generateStreamingAnswer(Long clientId, String prompt, List<FaqDoc> relevantDocs, List<String> history);

    // This method will now only be used for the fallback scenario
    Mono<AnswerDTO> generateAnswerWithFallback(Long clientId, String prompt, List<String> history);

    Mono<float[]> generateEmbedding(String text);
}
