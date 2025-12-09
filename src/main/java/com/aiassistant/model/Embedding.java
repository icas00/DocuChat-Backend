package com.aiassistant.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Stores the AI's "understanding" (vector) of a document.
@Entity
@Table(name = "embeddings")
public class Embedding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "doc_id", nullable = false)
    private FaqDoc doc; // Which FAQ document this embedding belongs to.

    @Column(name = "vector_data", columnDefinition = "TEXT")
    private String vectorData; // The actual vector (stored as JSON string).

    @jakarta.persistence.Transient
    private String vectorDataPgvector; // Vector in pgvector format (handled separately)

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Embedding() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FaqDoc getDoc() {
        return doc;
    }

    public void setDoc(FaqDoc doc) {
        this.doc = doc;
    }

    public String getVectorData() {
        return vectorData;
    }

    public void setVectorData(String vectorData) {
        this.vectorData = vectorData;
    }

    public String getVectorDataPgvector() {
        return vectorDataPgvector;
    }

    public void setVectorDataPgvector(String vectorDataPgvector) {
        this.vectorDataPgvector = vectorDataPgvector;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
