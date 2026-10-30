package com.vocabularyai.ai.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuration for RestTemplate beans
 * 
 * Provides RestTemplate for making HTTP requests to external APIs
 */
@Configuration
public class RestTemplateConfig {
    
    /**
     * Create RestTemplate bean with timeout configuration
     * 
     * @param builder RestTemplateBuilder
     * @return Configured RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .setConnectTimeout(Duration.ofSeconds(15))
            .setReadTimeout(Duration.ofSeconds(120))
            .requestFactory(this::clientHttpRequestFactory)
            .build();
    }
    
    /**
     * Create ClientHttpRequestFactory with buffering support
     * 
     * @return Configured ClientHttpRequestFactory
     */
    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000); // 15 seconds
        factory.setReadTimeout(120000); // 120 seconds (increased from 30s to prevent AI batch classification timeout)
        factory.setBufferRequestBody(true);
        
        return new BufferingClientHttpRequestFactory(factory);
    }
}
