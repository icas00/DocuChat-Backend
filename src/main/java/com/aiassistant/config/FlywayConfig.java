package com.aiassistant.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;

@Configuration
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy(Environment environment) {
        return flyway -> {
            String[] activeProfiles = environment.getActiveProfiles();
            String[] locations;

            if (Arrays.stream(activeProfiles).anyMatch(p -> p.equalsIgnoreCase("prod"))) {
                // For production, only use the postgresql migrations
                locations = new String[]{"classpath:db/migration/postgresql"};
            } else {
                // For development (or any other profile), use common and sqlite
                locations = new String[]{"classpath:db/migration/common", "classpath:db/migration/sqlite"};
            }
            
            // Manually set the locations, overriding any and all property-based configurations
            flyway.configure().locations(locations).load().migrate();
        };
    }
}
