package com.vocabularyai.speaking.service.stt;

/**
 * Exception thrown when Speech-to-Text processing fails.
 */
public class SpeechToTextException extends RuntimeException {

    public SpeechToTextException(String message) {
        super(message);
    }

    public SpeechToTextException(String message, Throwable cause) {
        super(message, cause);
    }

    public SpeechToTextException(Throwable cause) {
        super(cause);
    }
}
