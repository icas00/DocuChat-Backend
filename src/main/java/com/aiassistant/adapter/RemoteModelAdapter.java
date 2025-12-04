package com.aiassistant.adapter;

import com.aiassistant.config.ModelProviderProperties;
import com.aiassistant.dto.AnswerDTO;
import com.aiassistant.model.FaqDoc;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "model.adapter", havingValue = "remote", matchIfMissing = true)
public class RemoteModelAdapter implements ModelAdapter {

    private static final Logger log = LoggerFactory.getLogger(RemoteModelAdapter.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final ModelProviderProperties properties;

    public RemoteModelAdapter(WebClient webClient, ObjectMapper objectMapper, ModelProviderProperties properties) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Value("${app.prompts.standard}")
    private String standardSystemPrompt;

    @Value("${app.prompts.fallback}")
    private String fallbackSystemPrompt;

    @Value("${app.retry.max-attempts:3}")
    private long maxRetryAttempts;

    @Value("${app.retry.backoff-seconds:2}")
    private long retryBackoffSeconds;

    @Override
    public Flux<String> generateStreamingAnswer(Long clientId, String prompt, List<FaqDoc> relevantDocs,
            List<String> history) {
        log.info("Generating streaming answer for client: {}", clientId);
        String userPrompt = buildUserPrompt(prompt, relevantDocs);
        List<Map<String, String>> messages = buildMessageHistory(standardSystemPrompt, userPrompt, history);

        Map<String, Object> requestBody = Map.of(
                "model", properties.getChat().getModel(),
                "messages", messages,
                "stream", true);

        return webClient.post()
                .uri(properties.getChat().getEndpoint())
                .header("Authorization", "Bearer " + properties.getChat().getKey())
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .map(this::extractTextFromStreamChunk)
                .filter(text -> !text.isEmpty())
                .doOnSubscribe(s -> log.info("Stream subscribed for client: {}", clientId))
                .doOnNext(s -> log.debug("Sending text chunk: {}", s))
                .doOnError(e -> log.error("Error in streaming answer for client: {}", clientId, e))
                .doOnComplete(() -> log.info("Stream completed for client: {}", clientId))
                .retryWhen(Retry.backoff(maxRetryAttempts, Duration.ofSeconds(retryBackoffSeconds))
                        .filter(throwable -> throwable instanceof Exception));
    }

    @Override
    public Flux<String> generateAnswerWithFallback(Long clientId, String prompt, List<String> history) {
        log.info("Generating fallback answer for client: {}", clientId);
        List<Map<String, String>> messages = buildMessageHistory(fallbackSystemPrompt, prompt, history);

        Map<String, Object> requestBody = Map.of(
                "model", properties.getChat().getModel(),
                "messages", messages,
                "stream", true);

        return webClient.post()
                .uri(properties.getChat().getEndpoint())
                .header("Authorization", "Bearer " + properties.getChat().getKey())
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .map(this::extractTextFromStreamChunk)
                .filter(text -> !text.isEmpty())
                .doOnSubscribe(s -> log.info("Fallback stream subscribed for client: {}", clientId))
                .doOnNext(s -> log.debug("Sending fallback text chunk: {}", s))
                .doOnError(e -> log.error("Error in fallback streaming answer for client: {}", clientId, e))
                .doOnComplete(() -> log.info("Fallback stream completed for client: {}", clientId))
                .retryWhen(Retry.backoff(maxRetryAttempts, Duration.ofSeconds(retryBackoffSeconds))
                        .filter(throwable -> throwable instanceof Exception))
                .onErrorResume(e -> {
                    log.error("Error calling chat API for fallback", e);
                    return Flux.just(
                            "I apologize, but I'm having trouble generating a response right now. Please try again later.");
                });
    }

    @Override
    @org.springframework.cache.annotation.Cacheable(value = "queryEmbeddings", key = "#text")
    public Mono<float[]> generateEmbedding(String text) {
        log.info("Generating embedding for text length: {} (cache miss)", text.length());
        Map<String, Object> requestBody = Map.of(
                "input", text,
                "model", properties.getEmbedding().getModel());

        return webClient.post()
                .uri(properties.getEmbedding().getEndpoint())
                .header("Authorization", "Bearer " + properties.getEmbedding().getKey())
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(s -> log.info("Embedding received successfully"))
                .map(this::extractOpenAIEmbedding)
                .retryWhen(Retry.backoff(maxRetryAttempts, Duration.ofSeconds(retryBackoffSeconds)))
                .onErrorResume(e -> {
                    log.error("Failed to get embedding from OpenAI-compatible API", e);
                    return Mono.empty();
                });
    }

    @Override
    public Mono<List<float[]>> generateEmbeddings(List<String> texts) {
        log.info("Generating embeddings for batch of size: {}", texts.size());
        Map<String, Object> requestBody = Map.of(
                "input", texts,
                "model", properties.getEmbedding().getModel());

        return webClient.post()
                .uri(properties.getEmbedding().getEndpoint())
                .header("Authorization", "Bearer " + properties.getEmbedding().getKey())
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(s -> log.info("Batch embeddings received successfully"))
                .map(this::extractOpenAIEmbeddings)
                .retryWhen(Retry.backoff(maxRetryAttempts, Duration.ofSeconds(retryBackoffSeconds)))
                .onErrorResume(e -> {
                    log.error("Failed to get batch embeddings from OpenAI-compatible API", e);
                    return Mono.empty();
                });
    }

    private String buildUserPrompt(String query, List<FaqDoc> docs) {
        StringBuilder sb = new StringBuilder();
        sb.append("--- KNOWLEDGE BASE ---\n");
        if (docs != null && !docs.isEmpty()) {
            for (FaqDoc doc : docs) {
                sb.append("[ID: ").append(doc.getId()).append("] ");
                sb.append("Q: ").append(doc.getQuestion()).append("\n");
                sb.append("A: ").append(doc.getAnswer()).append("\n\n");
            }
        } else {
            sb.append("[No relevant information found]\n");
        }
        sb.append("\n--- USER'S QUESTION ---\n").append(query);
        return sb.toString();
    }

    private List<Map<String, String>> buildMessageHistory(String systemPrompt, String userPrompt,
            List<String> history) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        if (history != null) {
            for (String msg : history) {
                String[] parts = msg.split(": ", 2);
                if (parts.length == 2) {
                    String role = parts[0].equalsIgnoreCase("user") ? "user" : "assistant";
                    messages.add(Map.of("role", role, "content", parts[1]));
                }
            }
        }
        messages.add(Map.of("role", "user", "content", userPrompt));
        return messages;
    }

    private String extractAnswerFromResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            return root.path("choices").get(0).path("message").path("content")
                    .asText("Sorry, I could not process the response.");
        } catch (Exception e) {
            log.error("Error parsing response", e);
            return "Error parsing response.";
        }
    }

    private float[] extractOpenAIEmbedding(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode embeddingNode = root.path("data").get(0).path("embedding");

            if (embeddingNode.isMissingNode() || !embeddingNode.isArray()) {
                throw new RuntimeException("Embedding data not found in the expected OpenAI format.");
            }

            float[] vector = new float[embeddingNode.size()];
            for (int i = 0; i < embeddingNode.size(); i++) {
                vector[i] = (float) embeddingNode.get(i).asDouble();
            }
            return vector;
        } catch (Exception e) {
            throw new RuntimeException("Error parsing embedding", e);
        }
    }

    private List<float[]> extractOpenAIEmbeddings(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode dataNode = root.path("data");

            if (dataNode.isMissingNode() || !dataNode.isArray()) {
                throw new RuntimeException("Embedding data not found in the expected OpenAI format.");
            }

            List<float[]> vectors = new ArrayList<>();
            for (JsonNode embeddingItem : dataNode) {
                JsonNode embeddingNode = embeddingItem.path("embedding");
                if (embeddingNode.isMissingNode() || !embeddingNode.isArray()) {
                    continue; // Skip malformed items
                }

                float[] vector = new float[embeddingNode.size()];
                for (int i = 0; i < embeddingNode.size(); i++) {
                    vector[i] = (float) embeddingNode.get(i).asDouble();
                }
                vectors.add(vector);
            }
            return vectors;
        } catch (Exception e) {
            throw new RuntimeException("Error parsing batch embeddings", e);
        }
    }

    private String extractTextFromStreamChunk(String chunk) {
        log.debug("Raw chunk received: '{}'", chunk);
        try {
            // Handle [DONE] signal
            if (chunk.equals("[DONE]") || chunk.equals("data: [DONE]")) {
                return "";
            }

            // Remove 'data: ' prefix if present
            String jsonPart = chunk.startsWith("data: ") ? chunk.substring(6).trim() : chunk.trim();

            // Parse JSON and extract content
            JsonNode root = objectMapper.readTree(jsonPart);
            String content = root.path("choices").get(0)
                    .path("delta").path("content").asText("");

            if (!content.isEmpty()) {
                log.debug("Extracted content: '{}'", content);
            }

            return content;
        } catch (Exception e) {
            log.warn("Failed to parse stream chunk: {}", chunk, e);
            return "";
        }
    }
}
