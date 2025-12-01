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

                            // 1. Flatten all documents into a stream of chunks with their source document
                            return Flux.fromIterable(allDocuments)
                                    .flatMap(doc -> {
                                        List<DocumentChunker.DocumentChunk> chunks = documentChunker.chunkDocument(
                                                doc.getAnswer(),
                                                doc.getId());
                                        return Flux.fromIterable(chunks)
                                                .map(chunk -> new ChunkContext(chunk, doc));
                                    })
                                    // 2. Buffer chunks into batches of 50 to reduce API calls
                                    .buffer(50)
                                    .flatMap(batch -> {
                                        List<String> texts = batch.stream()
                                                .map(ctx -> ctx.chunk().getText())
                                                .collect(Collectors.toList());

                                        log.info("Processing batch of {} chunks...", texts.size());

                                        // 3. Generate embeddings for the batch
                                        return modelAdapter.generateEmbeddings(texts)
                                                .flatMapMany(vectors -> {
                                                    if (vectors.size() != batch.size()) {
                                                        log.error("Mismatch in embedding count! Sent {}, received {}",
                                                                batch.size(), vectors.size());
                                                        return Flux.error(
                                                                new RuntimeException("Embedding count mismatch"));
                                                    }

                                                    // 4. Zip vectors with their context and save
                                                    return Flux.range(0, batch.size())
                                                            .flatMap(i -> {
                                                                ChunkContext ctx = batch.get(i);
                                                                float[] vector = vectors.get(i);
                                                                return saveEmbedding(ctx.doc(), vector);
                                                            });
                                                });
                                    }, 5); // Concurrency limit for batches
                        })
                        .then());
    }

    private Mono<Embedding> saveEmbedding(FaqDoc doc, float[] vector) {
        return Mono.fromCallable(() -> {
            try {
                Embedding embedding = new Embedding();
                embedding.setDoc(doc);
                // Store in JSON format (backward compatibility)
                embedding.setVectorData(objectMapper.writeValueAsString(vector));
                // Store in pgvector format
                embedding.setVectorDataPgvector(vectorToPgVectorString(vector));

                Embedding savedEmbedding = embeddingRepository.save(embedding);

                // Update pgvector column using native SQL
                if (embedding.getVectorDataPgvector() != null) {
                    embeddingRepository.updatePgVector(
                            savedEmbedding.getId(),
                            embedding.getVectorDataPgvector());
                }
                return savedEmbedding;
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error serializing vector", e);
            }
        }).subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());
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

    private record ChunkContext(DocumentChunker.DocumentChunk chunk, FaqDoc doc) {
    }
}
