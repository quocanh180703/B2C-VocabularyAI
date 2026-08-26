package com.vocabularyai.common.exception;

import lombok.Getter;

/**
 * Exception thrown when API rate limit is exceeded
 * HTTP Status: 429 Too Many Requests
 * 
 * Được throw khi user đã vượt quá quota hàng ngày
 * hoặc request count limit
 */
@Getter
public class RateLimitExceededException extends RuntimeException {
    
    private final Long resetTimeUnix;  // Unix timestamp khi quota reset
    private final String limitType;     // DAILY, HOURLY, etc.
    private final Integer retryAfterSeconds;
    
    public RateLimitExceededException(String message, Long resetTimeUnix, String limitType) {
        super(message);
        this.resetTimeUnix = resetTimeUnix;
        this.limitType = limitType;
        // Calculate retry-after in seconds
        this.retryAfterSeconds = (int) ((resetTimeUnix - System.currentTimeMillis() / 1000) + 1);
    }
    
    public RateLimitExceededException(String message, Long resetTimeUnix) {
        this(message, resetTimeUnix, "DAILY");
    }
}
