package com.aiassistant.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "clients")
@Data
@NoArgsConstructor
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String apiKey;

    private String websiteUrl;

    @Column(columnDefinition = "TEXT")
    private String widgetColor = "#007aff"; // Default blue

    @Column
    private String chatbotName = "AI Assistant";

    @Column(columnDefinition = "TEXT")
    private String welcomeMessage = "Hi! How can I help you today?";

    @CreationTimestamp
    private Instant createdAt;
}
