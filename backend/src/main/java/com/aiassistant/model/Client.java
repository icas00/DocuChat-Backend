package com.aiassistant.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

// Represents one of our customers (e.g., a dental clinic).
@Data
@Entity
@Table(name = "clients")
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "api_key", unique = true, nullable = false)
    private String apiKey; // The secret key for this client.

    @Column(name = "website_url")
    private String websiteUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
