package com.aiassistant.repository;

import com.aiassistant.model.Client;
import com.aiassistant.model.FaqDoc;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// Helps us save and find FaqDoc data in the database.
public interface FaqDocRepository extends JpaRepository<FaqDoc, Long> {
    // Get all FAQ documents for a specific client.
    List<FaqDoc> findByClientId(Long clientId);

    // Delete all documents for a specific client.
    void deleteByClient(Client client);
}
