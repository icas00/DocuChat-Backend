package com.aiassistant.service;

import com.aiassistant.model.Client;
import com.aiassistant.model.FaqDoc;
import com.aiassistant.repository.ClientRepository;
import com.aiassistant.repository.FaqDocRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final FaqDocRepository faqDocRepository;

    public Optional<Client> findByApiKey(String apiKey) {
        return clientRepository.findByApiKey(apiKey);
    }

    @Transactional
    public void saveDocument(Long clientId, String filename, String content) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + clientId));
        
        String[] chunks = content.split("\n");
        
        for (String chunk : chunks) {
            if (chunk.trim().isEmpty() || chunk.trim().toLowerCase().startsWith("section")) {
                continue;
            }
            FaqDoc newDoc = new FaqDoc();
            newDoc.setClient(client);
            newDoc.setQuestion("Source: " + filename);
            newDoc.setAnswer(chunk.trim());
            faqDocRepository.save(newDoc);
        }
    }

    @Transactional
    public void clearAllData(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + clientId));
        faqDocRepository.deleteByClient(client);
    }
}
