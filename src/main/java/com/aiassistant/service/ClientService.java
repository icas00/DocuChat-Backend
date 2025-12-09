package com.aiassistant.service;

import com.aiassistant.dto.ClientSettingsDto;
import com.aiassistant.model.Client;
import com.aiassistant.model.FaqDoc;
import com.aiassistant.repository.ClientRepository;
import com.aiassistant.repository.FaqDocRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // save full doc here, chunker handles splitting later
        FaqDoc newDoc = new FaqDoc();
        newDoc.setClient(client);
        newDoc.setQuestion("Document: " + filename); // filename is the title
        newDoc.setAnswer(content);
        faqDocRepository.save(newDoc);
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
        // delete everything and reset ids
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
