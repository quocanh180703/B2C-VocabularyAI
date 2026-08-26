package com.vocabularyai.personalization.service;

/**
 * WeaknessStrengthException
 * 
 * Thrown when weakness & strength detection fails:
 * - Gemini AI service unavailable
 * - Analysis timeout
 * - Invalid learning profile data
 * - JSON parsing errors
 * - Rate limit exceeded
 */
public class WeaknessStrengthException extends RuntimeException {
    
    public WeaknessStrengthException(String message) {
        super(message);
    }
    
    public WeaknessStrengthException(String message, Throwable cause) {
        super(message, cause);
    }
}
