package com.vocabularyai.speaking.service.stt;

/**
 * SpeechToTextException
 * 
 * Exception được throw khi có lỗi trong quá trình transcription
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
