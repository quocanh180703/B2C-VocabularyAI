package com.vocabularyai.speaking.exception;

/**
 * AudioDurationException - Raised when audio duration is not within acceptable range
 */
public class AudioDurationException extends RuntimeException {
    
    private final long minDurationSeconds;
    private final long maxDurationSeconds;
    private final long actualDurationSeconds;
    
    public AudioDurationException(String message, long minDurationSeconds, long maxDurationSeconds, long actualDurationSeconds) {
        super(message);
        this.minDurationSeconds = minDurationSeconds;
        this.maxDurationSeconds = maxDurationSeconds;
        this.actualDurationSeconds = actualDurationSeconds;
    }
    
    public AudioDurationException(String message, long minDurationSeconds, long maxDurationSeconds, long actualDurationSeconds, Throwable cause) {
        super(message, cause);
        this.minDurationSeconds = minDurationSeconds;
        this.maxDurationSeconds = maxDurationSeconds;
        this.actualDurationSeconds = actualDurationSeconds;
    }
    
    public long getMinDurationSeconds() {
        return minDurationSeconds;
    }
    
    public long getMaxDurationSeconds() {
        return maxDurationSeconds;
    }
    
    public long getActualDurationSeconds() {
        return actualDurationSeconds;
    }
}
