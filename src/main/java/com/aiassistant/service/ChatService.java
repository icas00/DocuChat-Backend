package com.aiassistant.service;

import com.aiassistant.adapter.ModelAdapter;
import com.aiassistant.dto.AnswerDTO;
import com.aiassistant.model.Client;
import com.aiassistant.model.FaqDoc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
    public AnswerDTO processMessage(String apiKey, String message, List<String> history) {
        log.info("Processing message for API key: {}", apiKey);
        Client client = clientService.findByApiKey(apiKey)
                .orElseThrow(() -> new SecurityException("Invalid API Key provided."));

        float[] queryVector = modelAdapter.generateEmbedding(message);
        if (queryVector.length == 0) {
            return new AnswerDTO("Sorry, I couldn't process your question.", List.of(), 0.0);
        }

        Optional<AnswerDTO> cachedAnswer = cacheService.findInCache(queryVector);
        if (cachedAnswer.isPresent()) {
            log.info("Semantic cache hit for query: '{}'", message);
            return cachedAnswer.get();
        }
        log.info("Semantic cache miss for query: '{}'", message);

        List<FaqDoc> relevantDocs = embeddingService.findRelevantDocs(client.getId(), queryVector, TOP_K_DOCS);
        
        AnswerDTO generatedAnswer;

        if (relevantDocs.isEmpty()) {
            // Retrieval Failure: No relevant documents found.
            // Use a special prompt to allow for a graceful fallback.
            log.warn("No relevant documents found for query: '{}'. Using fallback prompt.", message);
            generatedAnswer = modelAdapter.generateAnswerWithFallback(client.getId(), message, history);
        } else {
            // Standard RAG: Documents found.
            generatedAnswer = modelAdapter.generateAnswer(client.getId(), message, relevantDocs, history);
        }

        cacheService.addToCache(queryVector, generatedAnswer);

        return generatedAnswer;
    }
}
