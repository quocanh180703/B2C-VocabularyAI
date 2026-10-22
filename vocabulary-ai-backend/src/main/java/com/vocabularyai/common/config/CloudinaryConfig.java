package com.vocabularyai.common.config;

import com.cloudinary.Cloudinary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Global Cloudinary Configuration.
 * Initializes a shared Cloudinary client using the configured CLOUDINARY_URL.
 */
@Slf4j
@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.url:${CLOUDINARY_URL:}}")
    private String cloudinaryUrl;

    @Bean
    @ConditionalOnMissingBean(Cloudinary.class)
    public Cloudinary cloudinary() {
        if (cloudinaryUrl == null || cloudinaryUrl.isBlank()) {
            log.warn("Cloudinary URL is not configured. Cloudinary upload operations will not be functional.");
            return new Cloudinary();
        }
        log.info("Initializing global Cloudinary bean successfully");
        return new Cloudinary(cloudinaryUrl);
    }
}
