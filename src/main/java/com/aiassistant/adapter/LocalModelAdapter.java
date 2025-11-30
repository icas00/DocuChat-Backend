package com.aiassistant.adapter;

import com.aiassistant.dto.AnswerDTO;
import com.aiassistant.model.FaqDoc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(name = "model.adapter", havingValue = "local")
public class LocalModelAdapter implements ModelAdapter {

    @Override
    public Flux<String> generateStreamingAnswer(Long clientId, String prompt, List<FaqDoc> relevantDocs,
            List<String> history) {
        log.warn("LocalModelAdapter is active. It does not support streaming and will return a dummy response.");
        String responseText = "This is a dummy response from the local adapter. Streaming is not supported.";
        return Flux.just(responseText);
    }

    @Override
    public Flux<String> generateAnswerWithFallback(Long clientId, String prompt, List<String> history) {
        log.warn("LocalModelAdapter is active. Using fallback response.");
        String responseText = "This is a dummy fallback response. I could not find any relevant documents.";
        return Flux.just(responseText);
    }

    @Override
    public Mono<float[]> generateEmbedding(String text) {
        log.warn("LocalModelAdapter is active. It will return a dummy embedding vector.");
        return Mono.just(new float[768]);
    }
}
