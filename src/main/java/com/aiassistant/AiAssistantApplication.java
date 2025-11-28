package com.aiassistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableAsync
@EnableScheduling
public class AiAssistantApplication {

	public static void main(String[] args) {
		// The active profile will now be determined by the SPRING_PROFILES_ACTIVE environment variable.
		// For local dev, set it to "dev". For production, set it to "prod".
		SpringApplication.run(AiAssistantApplication.class, args);
	}

}
