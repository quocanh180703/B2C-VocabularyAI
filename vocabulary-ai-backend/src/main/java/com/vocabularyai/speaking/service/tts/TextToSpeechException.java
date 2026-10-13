package com.vocabularyai.speaking.service.tts;

/**
 * Exception thrown when Text-to-Speech processing fails.
 */
public class TextToSpeechException extends RuntimeException {

    public TextToSpeechException(String message) {
        super(message);
    }

    public TextToSpeechException(String message, Throwable cause) {
        super(message, cause);
    }

    public TextToSpeechException(Throwable cause) {
        super(cause);
    }
}
