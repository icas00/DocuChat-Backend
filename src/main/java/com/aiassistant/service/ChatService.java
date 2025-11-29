package com.aiassistant.service;

import com.aiassistant.adapter.ModelAdapter;
import com.aiassistant.dto.AnswerDTO;
import com.aiassistant.model.Client;
import com.aiassistant.model.FaqDoc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ClientService clientService;
    private final EmbeddingService embeddingService;
    private final ModelAdapter modelAdapter;
    private final CacheService cacheService;

    private static final int TOP_K_DOCS = 15;

    @Transactional(readOnly = true)
    public Flux<String> processStreamingMessage(String apiKey, String message, List<String> history) {
        log.info("Processing streaming message for API key: {}", apiKey);
        Client client = clientService.findByApiKey(apiKey)
                .orElseThrow(() -> new SecurityException("Invalid API Key provided."));

        float[] queryVector = modelAdapter.generateEmbedding(message);
        if (queryVector.length == 0) {
            return Flux.just("Sorry, I couldn't process your question.");
        }

        // Note: Semantic caching is bypassed for streaming responses in this implementation.
        
        List<FaqDoc> relevantDocs = embeddingService.findRelevantDocs(client.getId(), queryVector, TOP_K_DOCS);
        
        if (relevantDocs.isEmpty()) {
            log.warn("No relevant documents found for query: '{}'. Using fallback.", message);
            // For fallback, we can't stream, so we return a single-item Flux.
            AnswerDTO fallbackAnswer = modelAdapter.generateAnswerWithFallback(client.getId(), message, history);
            return Flux.just(fallbackAnswer.getText());
        }

        return modelAdapter.generateStreamingAnswer(client.getId(), message, relevantDocs, history);
    }
}
