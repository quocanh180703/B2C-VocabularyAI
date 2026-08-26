package com.vocabularyai.auth.security;

import com.vocabularyai.auth.config.JwtProperties;
import com.vocabularyai.common.exception.UnauthorizedException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Provider
 * 
 * Responsible for generating and validating JWT tokens
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {
    
    private final JwtProperties jwtProperties;
    
    /**
     * Generate Access Token
     */
    public String generateAccessToken(String username, Long userId, String role) {
        return generateToken(
            username,
            jwtProperties.getAccessToken().getSecret(),
            jwtProperties.getAccessToken().getExpiration(),
            userId,
            role,
            "access"
        );
    }
    
    /**
     * Generate Refresh Token
     */
    public String generateRefreshToken(String username, Long userId) {
        return generateToken(
            username,
            jwtProperties.getRefreshToken().getSecret(),
            jwtProperties.getRefreshToken().getExpiration(),
            userId,
            null,
            "refresh"
        );
    }
    
    /**
     * Generate JWT Token
     */
    private String generateToken(
            String username,
            String secret,
            Long expiration,
            Long userId,
            String role,
            String tokenType) {
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        claims.put("type", tokenType);
        
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(username)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSigningKey(secret), SignatureAlgorithm.HS512)
            .compact();
    }
    
    /**
     * Validate and extract claims from token
     */
    public Claims validateAndGetClaims(String token, String secret) {
        try {
            return Jwts.parser()
                .setSigningKey(getSigningKey(secret))
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (SecurityException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            throw new UnauthorizedException("Invalid JWT signature");
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw new UnauthorizedException("Invalid JWT token");
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token: {}", e.getMessage());
            throw new UnauthorizedException("JWT token has expired");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
            throw new UnauthorizedException("Unsupported JWT token");
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
            throw new UnauthorizedException("JWT claims string is empty");
        }
    }
    
    /**
     * Extract username from token
     */
    public String getUsername(String token, String secret) {
        Claims claims = validateAndGetClaims(token, secret);
        return claims.getSubject();
    }
    
    /**
     * Extract user ID from token
     */
    public Long getUserId(String token, String secret) {
        Claims claims = validateAndGetClaims(token, secret);
        Object userId = claims.get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return (Long) userId;
    }
    
    /**
     * Extract role from token
     */
    public String getRole(String token, String secret) {
        Claims claims = validateAndGetClaims(token, secret);
        return (String) claims.get("role");
    }
    
    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token, String secret) {
        try {
            Claims claims = validateAndGetClaims(token, secret);
            return claims.getExpiration().before(new Date());
        } catch (UnauthorizedException e) {
            return true;
        }
    }
    
    /**
     * Get signing key from secret
     */
    private SecretKey getSigningKey(String secret) {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    /**
     * Get token expiration time
     */
    public long getTokenExpiration(String tokenType) {
        if ("refresh".equalsIgnoreCase(tokenType)) {
            return jwtProperties.getRefreshToken().getExpiration();
        }
        return jwtProperties.getAccessToken().getExpiration();
    }
}
