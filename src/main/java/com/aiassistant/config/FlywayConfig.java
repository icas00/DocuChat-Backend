package com.aiassistant.config;

import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;

@Configuration
public class FlywayConfig {

    @Bean
    public FlywayConfigurationCustomizer flywayConfigurationCustomizer(Environment environment) {
        return configuration -> {
            String[] activeProfiles = environment.getActiveProfiles();
            String[] locations;

            if (Arrays.stream(activeProfiles).anyMatch(p -> p.equalsIgnoreCase("prod"))) {
                // For production, use common seed data and the postgresql schema
                locations = new String[]{"classpath:db/migration/common", "classpath:db/migration/postgresql"};
            } else {
                // For development, use common seed data and the sqlite schema
                locations = new String[]{"classpath:db/migration/common", "classpath:db/migration/sqlite"};
            }
            
            // Customize the locations for the auto-configured Flyway instance
            configuration.locations(locations);
        };
    }
}
