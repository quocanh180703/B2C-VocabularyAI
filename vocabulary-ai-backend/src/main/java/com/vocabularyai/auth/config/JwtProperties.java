package com.vocabularyai.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT Configuration Properties
 * 
 * Loaded from application.yml
 * jwt.access-token.expiration
 * jwt.access-token.secret
 * jwt.refresh-token.expiration
 * jwt.refresh-token.secret
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    
    private AccessToken accessToken = new AccessToken();
    private RefreshToken refreshToken = new RefreshToken();
    
    @Data
    public static class AccessToken {
        private Long expiration = 900000L; // 15 minutes
        private String secret;
    }
    
    @Data
    public static class RefreshToken {
        private Long expiration = 2592000000L; // 30 days
        private String secret;
    }
}
