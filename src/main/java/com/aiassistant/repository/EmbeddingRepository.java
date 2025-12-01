package com.aiassistant.repository;

import com.aiassistant.model.Embedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// Helps us save and find Embedding data in the database.
public interface EmbeddingRepository extends JpaRepository<Embedding, Long> {
    // Get all embeddings for a specific client's documents.
    List<Embedding> findByDocClientId(Long clientId);

    /**
     * Find top K most similar embeddings using pgvector cosine similarity.
     * Uses the <=> operator for cosine distance (lower is more similar).
     * 
     * @param clientId    The client ID to filter by
     * @param queryVector The query embedding vector as a string (e.g., "[0.1, 0.2,
     *                    ...]")
     * @param limit       Maximum number of results to return
     * @return List of embeddings ordered by similarity (most similar first)
     */
    @Query(value = """
            SELECT e.* FROM embeddings e
            INNER JOIN faq_docs d ON e.doc_id = d.id
            WHERE d.client_id = :clientId
            AND e.vector_data_pgvector IS NOT NULL
            ORDER BY e.vector_data_pgvector <=> CAST(:queryVector AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<Embedding> findTopKSimilarByClientId(
            @Param("clientId") Long clientId,
            @Param("queryVector") String queryVector,
            @Param("limit") int limit);
}
