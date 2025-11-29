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
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@ConditionalOnProperty(name = "model.adapter", havingValue = "remote", matchIfMissing = true)
@RequiredArgsConstructor
public class RemoteModelAdapter implements ModelAdapter {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final ModelProviderProperties properties;

    private static final String STANDARD_SYSTEM_PROMPT = 
        "You are an AI assistant with a very specific and strict set of rules. You MUST follow these rules without exception. " +
        "RULE 1: Your primary function is to answer questions based *only* on the text provided in the KNOWLEDGE BASE. " +
        "RULE 2: If the answer is in the KNOWLEDGE BASE, you must provide it. " +
        "RULE 3: If the answer is NOT in the KNOWLEDGE BASE, you are forbidden from using any other knowledge and MUST reply with the exact phrase: 'I'm sorry, I don't have that information.' " +
        "RULE 4: You are an information-retrieval bot, NOT a calculator or a task-doer. If the user asks you to perform a calculation (like math), book something, or any other action, you MUST politely refuse and state that you can only answer questions based on the provided text. For example, say 'I cannot perform calculations, but I can tell you that the policy for overtime is...' " +
        "RULE 5: You must not refer to yourself as an AI or mention the 'KNOWLEDGE BASE' in your responses.";

    private static final String FALLBACK_SYSTEM_PROMPT = 
        "You are 'AI Assistant', a helpful customer support agent. " +
        "You have been asked a question for which you have NO information in your knowledge base. " +
        "Your task is to be helpful and manage the user's expectations without making up an answer. " +
        "Politely state that you don't have the specific information they are looking for. " +
        "If appropriate, suggest a more general topic they could ask about, or apologize for the inconvenience. " +
        "DO NOT invent an answer. Example: 'I'm sorry, I don't have specific details about that. I can answer questions about topics like membership, billing, and facility hours.'";

    @Override
    public Flux<String> generateStreamingAnswer(Long clientId, String prompt, List<FaqDoc> relevantDocs, List<String> history) {
        String userPrompt = buildUserPrompt(prompt, relevantDocs);
        List<Map<String, String>> messages = buildMessageHistory(STANDARD_SYSTEM_PROMPT, userPrompt, history);
        
        Map<String, Object> requestBody = Map.of(
            "model", properties.getChat().getModel(),
            "messages", messages,
            "stream", true
        );

        return webClient.post()
                .uri(properties.getChat().getEndpoint())
                .header("Authorization", "Bearer " + properties.getChat().getKey())
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .mapNotNull(this::extractContentFromStream);
    }

    @Override
    public AnswerDTO generateAnswerWithFallback(Long clientId, String prompt, List<String> history) {
        List<Map<String, String>> messages = buildMessageHistory(FALLBACK_SYSTEM_PROMPT, prompt, history);
        Map<String, Object> requestBody = Map.of("model", properties.getChat().getModel(), "messages", messages);

        try {
            String jsonResponse = webClient.post()
                    .uri(properties.getChat().getEndpoint())
                    .header("Authorization", "Bearer " + properties.getChat().getKey())
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            String answerText = extractAnswerFromResponse(jsonResponse);
            return new AnswerDTO(answerText, List.of(), 0.0);
        } catch (Exception e) {
            log.error("Error calling chat API for fallback", e);
            return new AnswerDTO("Error generating fallback answer.", List.of(), 0.0);
        }
    }

    @Override
    public float[] generateEmbedding(String text) {
        Map<String, Object> requestBody = Map.of(
            "input", text,
            "model", properties.getEmbedding().getModel()
        );

        try {
            String jsonResponse = webClient.post()
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
        sb.append("--- KNOWLEDGE BASE ---\n");
        if (docs != null && !docs.isEmpty()) {
            for (FaqDoc doc : docs) {
                sb.append(doc.getAnswer()).append("\n\n");
            }
        } else {
            sb.append("[No relevant information found]\n");
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
    
    private String extractContentFromStream(String sseEvent) {
        if (sseEvent.startsWith("data: ")) {
            String data = sseEvent.substring(6);
            if (data.trim().equals("[DONE]")) {
                return null;
            }
            try {
                JsonNode root = objectMapper.readTree(data);
                JsonNode contentNode = root.path("choices").get(0).path("delta").path("content");
                return contentNode.isMissingNode() ? null : contentNode.asText();
            } catch (Exception e) {
                log.error("Error parsing stream event: {}", sseEvent, e);
                return null;
            }
        }
        return null;
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
