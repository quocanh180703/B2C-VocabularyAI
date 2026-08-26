package com.vocabularyai.personalization.service;

/**
 * AdaptiveDifficultyException
 * 
 * Exception cho TASK 6: Adaptive Difficulty
 */
public class AdaptiveDifficultyException extends RuntimeException {
    
    public AdaptiveDifficultyException(String message) {
        super(message);
    }
    
    public AdaptiveDifficultyException(String message, Throwable cause) {
        super(message, cause);
    }
}
