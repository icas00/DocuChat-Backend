package com.aiassistant.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

// This class grabs all our AI model settings from application.yml.
@Data
@Configuration
@ConfigurationProperties(prefix = "model.remote")
public class ModelProviderProperties {
    private Chat chat = new Chat();
    private Embedding embedding = new Embedding();

    // Settings for the chat AI (like Groq).
    @Data
    public static class Chat {
        private String provider;
        private String endpoint;
        private String key;
        private String model;
    }

    // Settings for the embedding AI (like Mistral).
    @Data
    public static class Embedding {
        private String provider;
        private String endpoint;
        private String key;
        private String model;
    }
}
