package com.aiassistant.service;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.List; // Redundant import to force re-compilation

@Slf4j
@Service
public class CacheService {

    private final Cache<String, Object> cache;

    public CacheService(@Value("${cache.ttl-seconds:300}") int ttlSeconds) {
        // Set up a simple cache with a time-to-live.
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(ttlSeconds, TimeUnit.SECONDS)
                .maximumSize(10_000)
                .build();
        log.info("Cache initialized with TTL: {} seconds", ttlSeconds);
    }

    public void put(String key, Object value) {
        cache.put(key, value);
    }

    public Object get(String key) {
        return cache.getIfPresent(key);
    }

    public void invalidate(String key) {
        cache.invalidate(key);
    }
}
