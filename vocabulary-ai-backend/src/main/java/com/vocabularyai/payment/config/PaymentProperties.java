package com.vocabularyai.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Payment Module Configuration Properties
 * 
 * Loaded from application.yml
 * payment:
 *   payos:
 *     api-key: ...
 *     client-id: ...
 *     sandbox: true/false
 *   retry:
 *     max-attempts: 3
 *     initial-delay-ms: 1000
 *     backoff-multiplier: 2.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "payment")
public class PaymentProperties {
    
    private PayOS payos = new PayOS();
    private Retry retry = new Retry();
    
    /**
     * PayOS Gateway Configuration
     */
    @Data
    public static class PayOS {
        private String apiKey;
        private String clientId;
        private boolean sandbox = true;
        private String baseUrl;
    }
    
    /**
     * Retry Policy Configuration for payment operations
     */
    @Data
    public static class Retry {
        private int maxAttempts = 3;
        private long initialDelayMs = 1000;
        private double backoffMultiplier = 2.0;
    }
}
