package com.aiassistant.service;

import com.aiassistant.adapter.ModelAdapter;
import com.aiassistant.model.Embedding;
import com.aiassistant.model.FaqDoc;
import com.aiassistant.repository.EmbeddingRepository;
import com.aiassistant.repository.FaqDocRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    private final ModelAdapter modelAdapter;
    private final FaqDocRepository faqDocRepository;
    private final EmbeddingRepository embeddingRepository;
    private final ObjectMapper objectMapper;
    private final DocumentChunker documentChunker;

    public EmbeddingService(ModelAdapter modelAdapter, FaqDocRepository faqDocRepository,
            EmbeddingRepository embeddingRepository, ObjectMapper objectMapper, DocumentChunker documentChunker) {
        this.modelAdapter = modelAdapter;
        this.faqDocRepository = faqDocRepository;
        this.embeddingRepository = embeddingRepository;
        this.objectMapper = objectMapper;
        this.documentChunker = documentChunker;
    }

    @org.springframework.beans.factory.annotation.Value("${app.retrieval.max-search-k:10}")
    private int maxSearchK;

    @org.springframework.beans.factory.annotation.Value("${app.demo.cleanup-enabled:false}")
    private boolean cleanupEnabled;

    @org.springframework.beans.factory.annotation.Value("${app.demo.client-id:1}")
    private Long demoClientId;

    @Transactional
    public Mono<Void> indexClientDocs(Long clientId) {
        return Mono.fromRunnable(() -> log.info("Starting incremental indexing for client ID: {}", clientId))
                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
                .then(Mono.fromCallable(() -> faqDocRepository.findByClientId(clientId))
                        .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
                        .flatMapMany(allDocuments -> {
                            if (allDocuments.isEmpty()) {
                                log.warn("No documents found to index for client ID: {}", clientId);
                                return Flux.empty();
                            }

                            // skip docs that are already indexed
                            List<FaqDoc> docsToIndex = allDocuments.stream()
                                    .filter(doc -> !embeddingRepository.isDocumentIndexed(doc.getId()))
                                    .collect(Collectors.toList());

                            if (docsToIndex.isEmpty()) {
                                log.info("All documents are already indexed for client ID: {}", clientId);
                                return Flux.empty();
                            }

                            log.info("Found {} new or unindexed documents to index.", docsToIndex.size());

                            // removing old broken embeddings just in case
                            docsToIndex.forEach(doc -> {
                                List<Embedding> old = embeddingRepository.findByDocId(doc.getId());
                                if (!old.isEmpty()) {
                                    log.debug("Removing broken embedding for doc ID: {}", doc.getId());
                                    embeddingRepository.deleteAll(old);
                                }
                            });

                            // 1. split docs into smaller chunks
                            return Flux.fromIterable(docsToIndex)
                                    .flatMap(doc -> {
                                        List<DocumentChunker.DocumentChunk> chunks = documentChunker.chunkDocument(
                                                doc.getAnswer(),
                                                doc.getId());
                                        return Flux.fromIterable(chunks)
                                                .map(chunk -> new ChunkContext(chunk, doc));
                                    })
                                    // 2. group them to save api calls
                                    .buffer(50)
                                    .flatMap(batch -> {
                                        List<String> texts = batch.stream()
                                                .map(ctx -> ctx.chunk().getText())
                                                .collect(Collectors.toList());

                                        log.info("Processing batch of {} chunks...", texts.size());

                                        // 3. get vectors from ai
                                        return modelAdapter.generateEmbeddings(texts)
                                                .flatMapMany(vectors -> {
                                                    if (vectors.size() != batch.size()) {
                                                        log.error("Mismatch in embedding count! Sent {}, received {}",
                                                                batch.size(), vectors.size());
                                                        return Flux.error(
                                                                new RuntimeException("Embedding count mismatch"));
                                                    }

                                                    // 4. save vectors to db
                                                    return Flux.range(0, batch.size())
                                                            .flatMap(i -> {
                                                                ChunkContext ctx = batch.get(i);
                                                                float[] vector = vectors.get(i);
                                                                return saveEmbedding(ctx.doc(), vector);
                                                            });
                                                });
                                    }, 5); // dont overwhelm the server
                        })
                        .then());
    }

    private Mono<Embedding> saveEmbedding(FaqDoc doc, float[] vector) {
        return Mono.fromCallable(() -> {
            try {
                Embedding embedding = new Embedding();
                embedding.setDoc(doc);
                log.info("Saving embedding for Doc ID: {}", doc.getId());
                // keeping json for backup
                embedding.setVectorData(objectMapper.writeValueAsString(vector));
                // this is for pgvector search
                embedding.setVectorDataPgvector(vectorToPgVectorString(vector));

                Embedding savedEmbedding = embeddingRepository.save(embedding);

                // need native sql for vector update
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

        // dont search too many items
        final int MAX_K = Math.min(k, maxSearchK);

        long startTime = System.currentTimeMillis();

        // pgvector is way faster than doing it in java
        String queryVectorString = vectorToPgVectorString(queryVector);
        List<Embedding> similarEmbeddings = embeddingRepository.findTopKSimilarByClientId(
                clientId,
                queryVectorString,
                MAX_K);

        long searchTime = System.currentTimeMillis() - startTime;
        log.info("pgvector search completed in {}ms, found {} chunks", searchTime, similarEmbeddings.size());

        // get the actual docs from embeddings
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
        if (!cleanupEnabled) {
            return;
        }
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

    // helper to format vector for postgres
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
