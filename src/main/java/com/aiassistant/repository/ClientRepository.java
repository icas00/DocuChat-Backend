package com.aiassistant.repository;

import com.aiassistant.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// Helps us save and find Client data in the database.
public interface ClientRepository extends JpaRepository<Client, Long> {
    // Find a client using their secret API key.
    Optional<Client> findByApiKey(String apiKey);
}
