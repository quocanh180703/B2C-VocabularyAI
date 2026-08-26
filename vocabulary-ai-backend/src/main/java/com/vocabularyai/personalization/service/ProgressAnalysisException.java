package com.vocabularyai.personalization.service;

/**
 * ProgressAnalysisException
 * 
 * Exception cho TASK 7: Progress Analysis
 */
public class ProgressAnalysisException extends RuntimeException {
    
    public ProgressAnalysisException(String message) {
        super(message);
    }
    
    public ProgressAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}
