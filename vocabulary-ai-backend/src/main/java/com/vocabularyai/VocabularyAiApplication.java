package com.vocabularyai;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.Contact;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * VocabularyAI Backend Application
 * 
 * English Learning Platform with AI Integration
 * Using Spring Boot 3.x, MySQL, JPA/Hibernate, JWT, Redis, Gemini AI
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableJpaRepositories(basePackages = {"com.vocabularyai.**.repository"})
@OpenAPIDefinition(
    info = @Info(
        title = "VocabularyAI Backend API",
        version = "1.0.0",
        description = "RESTful API for VocabularyAI - English Learning Platform with AI Integration",
        contact = @Contact(
            name = "VocabularyAI Team",
            url = "https://vocabularyai.com"
        )
    )
)
public class VocabularyAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(VocabularyAiApplication.class, args);
    }
}
