package com.vocabularyai.speaking.exception;

/**
 * InvalidAudioFormatException - Raised when audio file format is not supported
 */
public class InvalidAudioFormatException extends RuntimeException {
    
    public InvalidAudioFormatException(String message) {
        super(message);
    }
    
    public InvalidAudioFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
