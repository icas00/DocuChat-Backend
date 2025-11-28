package com.aiassistant.repository;

import com.aiassistant.model.Embedding;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// Helps us save and find Embedding data in the database.
public interface EmbeddingRepository extends JpaRepository<Embedding, Long> {
    // Get all embeddings for a specific client's documents.
    List<Embedding> findByDocClientId(Long clientId);
}
