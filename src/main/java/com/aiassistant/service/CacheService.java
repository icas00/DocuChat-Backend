package com.aiassistant.service;

import com.aiassistant.dto.AnswerDTO;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CacheService {

    // A thread-safe map to store cached embeddings and their corresponding answers.
    // Key: A float[] representing the query embedding.
    // Value: The AnswerDTO that was generated for that query.
    private final Map<float[], AnswerDTO> semanticCache = new ConcurrentHashMap<>();
    private static final double SIMILARITY_THRESHOLD = 0.98; // Very high threshold for a confident cache hit

    /**
     * Tries to find a cached answer for a given query embedding.
     *
     * @param queryVector The embedding of the user's current query.
     * @return An Optional containing the cached AnswerDTO if a sufficiently similar query is found, otherwise empty.
     */
    public Optional<AnswerDTO> findInCache(float[] queryVector) {
        for (Map.Entry<float[], AnswerDTO> entry : semanticCache.entrySet()) {
            float[] cachedVector = entry.getKey();
            double similarity = calculateCosineSimilarity(queryVector, cachedVector);

            if (similarity >= SIMILARITY_THRESHOLD) {
                AnswerDTO cachedAnswer = entry.getValue();
                // Mark the answer as coming from the cache for debugging/display purposes.
                cachedAnswer.setFromCache(true); 
                return Optional.of(cachedAnswer);
            }
        }
        return Optional.empty();
    }

    /**
     * Adds a new entry to the semantic cache.
     *
     * @param queryVector The embedding of the user's query.
     * @param answer      The generated answer to store.
     */
    public void addToCache(float[] queryVector, AnswerDTO answer) {
        // To prevent the cache from growing indefinitely in a real app, you'd add an eviction policy.
        // For this demo, we'll just keep it simple.
        semanticCache.put(queryVector, answer);
    }

    private double calculateCosineSimilarity(float[] vectorA, float[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        if (normA == 0 || normB == 0) {
            return 0.0;
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
