package com.aiassistant.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

// Stores the AI's "understanding" (vector) of a document.
@Data
@Entity
@Table(name = "embeddings")
public class Embedding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "doc_id", nullable = false)
    private FaqDoc doc; // Which FAQ document this embedding belongs to.

    @Column(name = "vector_data", columnDefinition = "TEXT")
    private String vectorData; // The actual vector (stored as JSON string).

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
