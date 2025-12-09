package com.aiassistant.service;

import com.aiassistant.adapter.ModelAdapter;
import com.aiassistant.dto.AnswerDTO;
import com.aiassistant.model.FaqDoc;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ClientService clientService;
    private final EmbeddingService embeddingService;
    private final ModelAdapter modelAdapter;
    private final CacheService cacheService;

    public ChatService(ClientService clientService, EmbeddingService embeddingService, ModelAdapter modelAdapter,
            CacheService cacheService) {
        this.clientService = clientService;
        this.embeddingService = embeddingService;
        this.modelAdapter = modelAdapter;
        this.cacheService = cacheService;
    }

    @org.springframework.beans.factory.annotation.Value("${app.retrieval.default-top-k:15}")
    private int defaultTopK;

    @Transactional(readOnly = true)
    public Flux<String> processStreamingMessage(String apiKey, String message, List<String> history) {
        log.info("Processing streaming message for API key: {}", apiKey);

        return Mono.justOrEmpty(clientService.findByApiKey(apiKey))
                .switchIfEmpty(Mono.error(new SecurityException("Invalid API Key provided.")))
                .flatMapMany(client -> {
                    return modelAdapter.generateEmbedding(message)
                            .switchIfEmpty(Mono.error(new RuntimeException("Failed to generate embedding")))
                            .flatMapMany(queryVector -> {
                                if (queryVector.length == 0) {
                                    return Flux.just("Sorry, I couldn't process your question.");
                                }

                                // checking if we already answered this before
                                Optional<AnswerDTO> cachedOpt = cacheService.findInCache(queryVector);
                                if (cachedOpt.isPresent()) {
                                    log.info("Cache hit for query: '{}'", message);
                                    return Flux.just(cachedOpt.get().getText());
                                }

                                List<FaqDoc> relevantDocs = embeddingService.findRelevantDocs(client.getId(),
                                        queryVector, defaultTopK);

                                if (relevantDocs.isEmpty()) {
                                    log.warn("No relevant documents found for query: '{}'. Using fallback.", message);
                                    return modelAdapter.generateAnswerWithFallback(client.getId(), message, history);
                                }

                                return modelAdapter.generateStreamingAnswer(client.getId(), message, relevantDocs,
                                        history);
                            });
                }).onErrorResume(e -> {
                    log.error("Error processing message", e);
                    return Flux.just(
                            "I apologize, but I'm having trouble connecting to my brain right now. Please try again in a moment.");
                });
    }
}
