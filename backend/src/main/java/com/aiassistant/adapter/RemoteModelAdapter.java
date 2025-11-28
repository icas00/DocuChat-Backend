package com.aiassistant.adapter;

import com.aiassistant.config.ModelProviderProperties;
import com.aiassistant.dto.AnswerDTO;
import com.aiassistant.model.FaqDoc;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@ConditionalOnProperty(name = "model.adapter", havingValue = "remote", matchIfMissing = true)
@RequiredArgsConstructor
public class RemoteModelAdapter implements ModelAdapter {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    private final ModelProviderProperties properties;

    @Override
    public AnswerDTO generateAnswer(Long clientId, String prompt, List<FaqDoc> relevantDocs, List<String> history) {
        // Adjusted system prompt for better follow-up handling and helpfulness
        String systemPrompt = "You are 'AI Assistant', a helpful and factual customer support agent. " +
            "Your primary goal is to answer the user's question based *only* on the provided KNOWLEDGE BASE. " +
            "If the KNOWLEDGE BASE contains the answer, provide it clearly and concisely. " +
            "If the KNOWLEDGE BASE does NOT contain the answer, you MUST state: 'I'm sorry, I don't have that information.' " +
            "You can use the conversation history to understand follow-up questions, but your answers must still be derived from the KNOWLEDGE BASE. " +
            "Do not invent information. Do not refer to yourself as an AI or mention the knowledge base directly in your answer.";

        String userPrompt = buildUserPrompt(prompt, relevantDocs);
        List<Map<String, String>> messages = buildMessageHistory(systemPrompt, userPrompt, history);
        
        Map<String, Object> requestBody = Map.of(
            "model", properties.getChat().getModel(),
            "messages", messages
        );

        try {
            String jsonResponse = webClientBuilder.build().post()
                    .uri(properties.getChat().getEndpoint())
                    .header("Authorization", "Bearer " + properties.getChat().getKey())
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            String answerText = extractAnswerFromResponse(jsonResponse);
            List<String> sources = relevantDocs.stream().map(FaqDoc::getQuestion).collect(Collectors.toList());
            return new AnswerDTO(answerText, sources, 0.9);

        } catch (Exception e) {
            log.error("Error calling chat API", e);
            return new AnswerDTO("Error generating answer: " + e.getMessage(), List.of(), 0.0);
        }
    }

    @Override
    public float[] generateEmbedding(String text) {
        Map<String, Object> requestBody = Map.of(
            "input", text,
            "model", properties.getEmbedding().getModel()
        );

        try {
            String jsonResponse = webClientBuilder.build().post()
                    .uri(properties.getEmbedding().getEndpoint())
                    .header("Authorization", "Bearer " + properties.getEmbedding().getKey())
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return extractOpenAIEmbedding(jsonResponse);
        } catch (Exception e) {
            log.error("Failed to get embedding from OpenAI-compatible API", e);
            return new float[0];
        }
    }

    private String buildUserPrompt(String query, List<FaqDoc> docs) {
        StringBuilder sb = new StringBuilder();
        if (docs != null && !docs.isEmpty()) {
            sb.append("--- KNOWLEDGE BASE ---\n");
            for (FaqDoc doc : docs) {
                sb.append(doc.getAnswer()).append("\n\n");
            }
        } else {
            sb.append("--- KNOWLEDGE BASE ---\n[No relevant information found]\n");
        }
        sb.append("\n--- USER'S QUESTION ---\n").append(query);
        return sb.toString();
    }

    private List<Map<String, String>> buildMessageHistory(String systemPrompt, String userPrompt, List<String> history) {
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

    private String extractAnswerFromResponse(String jsonResponse) throws Exception {
        String cleanJson = jsonResponse.substring(jsonResponse.indexOf('{'));
        JsonNode root = objectMapper.readTree(cleanJson);
        return root.path("choices").get(0).path("message").path("content").asText("Sorry, I could not process the response.");
    }

    private float[] extractOpenAIEmbedding(String jsonResponse) throws Exception {
        String cleanJson = jsonResponse.substring(jsonResponse.indexOf('{'));
        JsonNode root = objectMapper.readTree(cleanJson);
        JsonNode embeddingNode = root.path("data").get(0).path("embedding");
        
        if (embeddingNode.isMissingNode() || !embeddingNode.isArray()) {
            throw new Exception("Embedding data not found in the expected OpenAI format.");
        }

        float[] vector = new float[embeddingNode.size()];
        for (int i = 0; i < embeddingNode.size(); i++) {
            vector[i] = (float) embeddingNode.get(i).asDouble();
        }
        return vector;
    }
}
