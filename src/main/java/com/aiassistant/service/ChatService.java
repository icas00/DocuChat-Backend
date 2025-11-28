package com.aiassistant.service;

import com.aiassistant.adapter.ModelAdapter;
import com.aiassistant.dto.AnswerDTO;
import com.aiassistant.model.Client;
import com.aiassistant.model.FaqDoc;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ModelAdapter modelAdapter;
    private final EmbeddingService embeddingService;
    private final ClientService clientService;

    public AnswerDTO processMessage(String apiKey, String message, List<String> history) {
        Optional<Client> clientOpt = clientService.findByApiKey(apiKey);
        if (clientOpt.isEmpty()) {
            throw new SecurityException("Invalid API Key");
        }
        Client client = clientOpt.get();

        // Use only the user's latest message for the semantic search.
        // This provides a clean, accurate vector for finding relevant documents.
        List<FaqDoc> relevantDocs = embeddingService.findRelevantDocs(client.getId(), message, 3);

        // The full history is still passed to the AI for conversational context.
        return modelAdapter.generateAnswer(client.getId(), message, relevantDocs, history);
    }
}
