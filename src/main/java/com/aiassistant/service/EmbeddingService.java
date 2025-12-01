package com.aiassistant.service;

import com.aiassistant.adapter.ModelAdapter;
import com.aiassistant.model.Embedding;
import com.aiassistant.model.FaqDoc;
import com.aiassistant.repository.EmbeddingRepository;
import com.aiassistant.repository.FaqDocRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final ModelAdapter modelAdapter;
    private final FaqDocRepository faqDocRepository;
    private final EmbeddingRepository embeddingRepository;
    private final ObjectMapper objectMapper;
    private final DocumentChunker documentChunker;

    @Transactional
    public Mono<Void> indexClientDocs(Long clientId) {
        return Mono.fromRunnable(() -> {
            log.info("Starting indexing for client ID: {}", clientId);
            embeddingRepository.deleteAll(embeddingRepository.findByDocClientId(clientId));
            log.info("Cleared old embeddings for client.");
        })
                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
                .then(Mono.fromCallable(() -> faqDocRepository.findByClientId(clientId))
                        .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
                        .flatMapMany(allDocuments -> {
                            if (allDocuments.isEmpty()) {
                                log.warn("No documents found to index for client ID: {}", clientId);
                                return Flux.empty();
                            }
                            log.info("Found {} documents. Chunking and indexing...", allDocuments.size());

                            // Chunk each document into semantic units
                            return Flux.fromIterable(allDocuments)
                                    .flatMap(doc -> {
                                        // Chunk the document text
                                        List<DocumentChunker.DocumentChunk> chunks = documentChunker.chunkDocument(
                                                doc.getAnswer(),
                                                doc.getId());

                                        log.info("Document ID {} split into {} chunks", doc.getId(), chunks.size());

                                        // Create embeddings for each chunk
                                        return Flux.fromIterable(chunks)
                                                .flatMap(chunk -> modelAdapter.generateEmbedding(chunk.getText())
                                                        .map(vector -> {
                                                            try {
                                                                Embedding embedding = new Embedding();
                                                                embedding.setDoc(doc);
                                                                // Store in JSON format (backward compatibility)
                                                                embedding.setVectorData(
                                                                        objectMapper.writeValueAsString(vector));
                                                                // Also store in pgvector format for fast similarity
                                                                // search
                                                                embedding.setVectorDataPgvector(
                                                                        vectorToPgVectorString(vector));
                                                                return embedding;
                                                            } catch (JsonProcessingException e) {
                                                                throw new RuntimeException(e);
                                                            }
                                                        })
                                                        .flatMap(embedding -> Mono.fromRunnable(() -> {
                                                            embeddingRepository.save(embedding);
                                                            log.info("Successfully indexed chunk for doc ID: {}",
                                                                    doc.getId());
                                                        }).subscribeOn(
                                                                reactor.core.scheduler.Schedulers.boundedElastic())
                                                                .then(Mono.just(embedding)))
                                                        .onErrorResume(e -> {
                                                            log.error("Error indexing chunk for doc ID: {}",
                                                                    doc.getId(), e);
                                                            return Mono.empty();
                                                        }));
                                    });
                        })
                        .then());
    }

    public List<FaqDoc> findRelevantDocs(Long clientId, float[] queryVector, int k) {
        log.info("Finding relevant docs for client ID: {} using pgvector", clientId);

        if (queryVector == null || queryVector.length == 0) {
            log.error("Invalid query vector provided. Returning no documents.");
            return List.of();
        }

        // Limit K to 5 for optimal performance
        final int MAX_K = Math.min(k, 5);

        long startTime = System.currentTimeMillis();

        // Use pgvector for fast similarity search (20x faster than in-memory)
        String queryVectorString = vectorToPgVectorString(queryVector);
        List<Embedding> similarEmbeddings = embeddingRepository.findTopKSimilarByClientId(
                clientId,
                queryVectorString,
                MAX_K);

        long searchTime = System.currentTimeMillis() - startTime;
        log.info("pgvector search completed in {}ms, found {} chunks", searchTime, similarEmbeddings.size());

        // Extract unique FaqDocs (in case multiple chunks from same doc)
        List<FaqDoc> results = similarEmbeddings.stream()
                .map(Embedding::getDoc)
                .distinct()
                .limit(MAX_K)
                .collect(Collectors.toList());

        log.info("Returning {} relevant documents", results.size());

        return results;
    }

    @Scheduled(fixedRate = 3600000) // 1 hour
    @Transactional
    public void cleanupDemoData() {
        Long demoClientId = 1L;
        log.info("Running scheduled cleanup of demo data for client ID: {}", demoClientId);

        List<Embedding> embeddings = embeddingRepository.findByDocClientId(demoClientId);
        if (!embeddings.isEmpty()) {
            embeddingRepository.deleteAll(embeddings);
            log.info("Deleted {} embeddings.", embeddings.size());
        }

        List<FaqDoc> docs = faqDocRepository.findByClientId(demoClientId);
        if (!docs.isEmpty()) {
            faqDocRepository.deleteAll(docs);
            log.info("Deleted {} documents.", docs.size());
        }

        log.info("Demo data cleanup finished.");
    }

    private double cosineSimilarity(float[] v1, float[] v2) {
        if (v1 == null || v2 == null || v1.length != v2.length || v1.length == 0) {
            return 0.0;
        }
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < v1.length; i++) {
            dotProduct += v1[i] * v2[i];
            normA += v1[i] * v1[i];
            normB += v2[i] * v2[i];
        }
        if (normA == 0 || normB == 0) {
            return 0.0;
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * Convert float array to pgvector string format: "[0.1, 0.2, 0.3, ...]"
     */
    private String vectorToPgVectorString(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0)
                sb.append(",");
            sb.append(vector[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    private record DocScore(FaqDoc doc, double score) {
    }
}
