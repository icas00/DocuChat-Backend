package com.aiassistant.service;

import com.aiassistant.dto.ClientSettingsDto;
import com.aiassistant.model.Client;
import com.aiassistant.model.FaqDoc;
import com.aiassistant.repository.ClientRepository;
import com.aiassistant.repository.FaqDocRepository;
import com.aiassistant.repository.ClientRepository;
import com.aiassistant.repository.FaqDocRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final FaqDocRepository faqDocRepository;

    public ClientService(ClientRepository clientRepository, FaqDocRepository faqDocRepository) {
        this.clientRepository = clientRepository;
        this.faqDocRepository = faqDocRepository;
    }

    public Optional<Client> findByApiKey(String apiKey) {
        return clientRepository.findByApiKey(apiKey);
    }

    @Transactional
    public Client createClient(String name) {
        Client client = new Client();
        client.setName(name);
        client.setApiKey("DOC-" + UUID.randomUUID().toString());
        client.setAdminKey("ADM-" + UUID.randomUUID().toString());
        return clientRepository.save(client);
    }

    @Transactional
    public void saveDocument(Long clientId, String filename, String content) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + clientId));

        List<String> sentences = new ArrayList<>();
        BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
        iterator.setText(content);
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            String sentence = content.substring(start, end).trim();
            if (!sentence.isEmpty()) {
                sentences.add(sentence);
            }
        }

        for (int i = 0; i < sentences.size(); i++) {
            String currentSentence = sentences.get(i);
            String prevSentence = (i > 0) ? sentences.get(i - 1) : "";
            String nextSentence = (i < sentences.size() - 1) ? sentences.get(i + 1) : "";
            String contextualChunk = (prevSentence + " " + currentSentence + " " + nextSentence).trim();

            FaqDoc newDoc = new FaqDoc();
            newDoc.setClient(client);
            newDoc.setQuestion(currentSentence);
            newDoc.setAnswer(contextualChunk);
            faqDocRepository.save(newDoc);
        }
    }

    @Transactional
    public long clearAllData(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + clientId));
        return faqDocRepository.deleteByClient(client);
    }

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    @Transactional
    public void clearSystemData() {
        // Deleting all clients will cascade delete all docs and embeddings
        // Using TRUNCATE with RESTART IDENTITY to reset auto-increment counters to 1
        entityManager.createNativeQuery("TRUNCATE TABLE embeddings, faq_docs, clients RESTART IDENTITY CASCADE")
                .executeUpdate();
    }

    @Transactional
    public void updateSettings(Long clientId, ClientSettingsDto settings) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + clientId));

        if (settings.getWidgetColor() != null) {
            client.setWidgetColor(settings.getWidgetColor());
        }
        if (settings.getChatbotName() != null) {
            client.setChatbotName(settings.getChatbotName());
        }
        if (settings.getWelcomeMessage() != null) {
            client.setWelcomeMessage(settings.getWelcomeMessage());
        }

        clientRepository.save(client);
    }
}
