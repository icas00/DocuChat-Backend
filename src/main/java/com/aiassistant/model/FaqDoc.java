package com.aiassistant.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

// Represents one of the client's FAQ documents.
@Data
@Entity
@Table(name = "faq_docs")
public class FaqDoc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client; // Which client this FAQ belongs to.

    @Column(columnDefinition = "TEXT", nullable = false)
    private String question;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String answer;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Lombok handles getters/setters, but sometimes explicit ones are needed.
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
}
