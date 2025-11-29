package com.aiassistant.repository;

import com.aiassistant.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByApiKey(String apiKey);
    Optional<Client> findByAdminKey(String adminKey);
}
