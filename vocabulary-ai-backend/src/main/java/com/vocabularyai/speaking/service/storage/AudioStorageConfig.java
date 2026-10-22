package com.vocabularyai.speaking.service.storage;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.Map;

/**
 * Configuration for Audio Storage Services.
 * Handles initialization of storage backends based on application properties.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AudioStorageConfig {

    @Value("${speaking.storage.type:local}")
    private String storageType;

    /**
     * Initialize S3 client for S3 storage provider
     */
    @Bean
    @ConditionalOnProperty(name = "speaking.storage.type", havingValue = "s3")
    public S3Client s3Client(@Value("${speaking.storage.s3-region:us-east-1}") String region) {
        log.info("Initializing S3 client for region: {}", region);

        return S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
    }

    /**
     * Initialize Cloudinary client for Cloudinary storage provider
     */
    @Bean
    @ConditionalOnMissingBean(Cloudinary.class)
    @ConditionalOnProperty(name = "speaking.storage.type", havingValue = "cloudinary")
    public Cloudinary cloudinary(@Value("${speaking.storage.cloudinary-url:}") String cloudinaryUrl) {
        log.info("Initializing Cloudinary client");

        // Parse cloudinary URL
        // Format: cloudinary://api_key:api_secret@cloud_name
        if (cloudinaryUrl == null || cloudinaryUrl.isEmpty()) {
            throw new IllegalArgumentException("CLOUDINARY_URL environment variable or cloudinary-url config not set");
        }

        return new Cloudinary(cloudinaryUrl);
    }

    /**
     * Log active storage configuration
     */
    @Bean
    public AudioStorageServiceLogger audioStorageServiceLogger() {
        log.info("Speaking Module Audio Storage initialized with provider: {}", storageType);
        return new AudioStorageServiceLogger();
    }

    public static class AudioStorageServiceLogger {
        // Placeholder for logging
    }
}
