package com.aiassistant.adapter;

import com.aiassistant.dto.AnswerDTO;
import com.aiassistant.model.FaqDoc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(name = "model.adapter", havingValue = "local")
public class LocalModelAdapter implements ModelAdapter {

    @Value("${model.local.inference-url}")
    private String inferenceUrl;
    
    @Value("${model.local.embedding-url}")
    private String embeddingUrl;

    private final WebClient webClient = WebClient.builder().build();

    @Override
    public AnswerDTO generateAnswer(Long clientId, String prompt, List<FaqDoc> relevantDocs, List<String> history) {
        log.info("Using local inference at: {}", inferenceUrl);
        throw new UnsupportedOperationException("Local adapter needs manual setup - check README.");
    }

    @Override
    public float[] generateEmbedding(String text) {
        log.info("Using local embeddings at: {}", embeddingUrl);
        throw new UnsupportedOperationException("Local adapter needs manual setup - check README.");
    }
}
