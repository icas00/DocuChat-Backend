package com.aiassistant.service;

import com.aiassistant.adapter.ModelAdapter;
import com.aiassistant.dto.AnswerDTO;
import com.aiassistant.model.Client;
import com.aiassistant.model.FaqDoc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ClientService clientService;
    private final EmbeddingService embeddingService;
    private final ModelAdapter modelAdapter;
    private final CacheService cacheService; // Inject the new cache service

    @Transactional(readOnly = true)
    public AnswerDTO processMessage(String apiKey, String message, List<String> history) {
        log.info("Processing message for API key: {}", apiKey);
        Client client = clientService.findByApiKey(apiKey)
                .orElseThrow(() -> new SecurityException("Invalid API Key provided."));

        // 1. Generate an embedding for the user's query.
        float[] queryVector = modelAdapter.generateEmbedding(message);
        if (queryVector.length == 0) {
            return new AnswerDTO("Sorry, I couldn't process your question.", List.of(), 0.0);
        }

        // 2. Check for a semantically similar answer in the cache.
        Optional<AnswerDTO> cachedAnswer = cacheService.findInCache(queryVector);
        if (cachedAnswer.isPresent()) {
            log.info("Semantic cache hit for query: '{}'", message);
            return cachedAnswer.get();
        }
        log.info("Semantic cache miss for query: '{}'", message);

        // 3. If no cache hit, proceed with the normal RAG workflow.
        List<FaqDoc> relevantDocs = embeddingService.findRelevantDocs(client.getId(), queryVector);
        AnswerDTO generatedAnswer = modelAdapter.generateAnswer(client.getId(), message, relevantDocs, history);

        // 4. Store the newly generated answer in the cache for future use.
        cacheService.addToCache(queryVector, generatedAnswer);

        return generatedAnswer;
    }
}
