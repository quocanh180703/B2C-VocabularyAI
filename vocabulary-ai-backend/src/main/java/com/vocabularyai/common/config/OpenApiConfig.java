package com.vocabularyai.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger Configuration
 * 
 * Configures Swagger/OpenAPI documentation for VocabularyAI Backend API
 * - Accessible at: http://localhost:8080/api/swagger-ui.html
 * - JSON spec at: http://localhost:8080/api/v3/api-docs
 */
@Configuration
public class OpenApiConfig {
    
    /**
     * Configure OpenAPI documentation
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("VocabularyAI Backend API")
                .version("1.0.0")
                .description("RESTful API for VocabularyAI - English Learning Platform with AI Integration\n\n" +
                    "# Features\n" +
                    "- User Authentication & Authorization (JWT)\n" +
                    "- Vocabulary Management (CRUD with search & filter)\n" +
                    "- Learning Progress Tracking (Spaced Repetition System)\n" +
                    "- Quiz System with Auto-grading\n" +
                    "- Speaking Practice (Coming soon)\n" +
                    "- AI-powered Features (Gemini) (Coming soon)\n\n" +
                    "# Base URL\n" +
                    "All endpoints are prefixed with `/api`\n\n" +
                    "# Authentication\n" +
                    "Use JWT Bearer token in Authorization header:\n" +
                    "`Authorization: Bearer <jwt_token>`")
                .contact(new Contact()
                    .name("VocabularyAI Team")
                    .url("https://vocabularyai.com")
                    .email("support@vocabularyai.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
            .components(new Components()
                .addSecuritySchemes("bearer-jwt",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT token issued after successful login"))
                .addSecuritySchemes("bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT token issued after successful login")));
    }
}
