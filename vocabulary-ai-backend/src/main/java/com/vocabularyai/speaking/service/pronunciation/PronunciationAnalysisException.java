package com.vocabularyai.speaking.service.pronunciation;

/**
 * Exception thrown when Pronunciation Analysis fails.
 */
public class PronunciationAnalysisException extends RuntimeException {

    public PronunciationAnalysisException(String message) {
        super(message);
    }

    public PronunciationAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }

    public PronunciationAnalysisException(Throwable cause) {
        super(cause);
    }
}
