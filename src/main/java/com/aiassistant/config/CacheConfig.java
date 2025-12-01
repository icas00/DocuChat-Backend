package com.aiassistant.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuration for caching query embeddings to reduce API calls.
 * Caching embeddings can reduce latency by 60-90% for repeated queries.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("queryEmbeddings");
        cacheManager.setCaffeine(caffeineCacheBuilder());
        cacheManager.setAsyncCacheMode(true); // Enable async mode for reactive (Mono/Flux) support
        return cacheManager;
    }

    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS) // Cache for 1 hour
                .maximumSize(1000) // Max 1000 cached queries
                .recordStats(); // Enable stats for monitoring
    }
}
