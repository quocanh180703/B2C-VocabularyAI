package com.vocabularyai.speaking.service.storage;

/**
 * AudioStorageException
 * 
 * Exception được throw khi có lỗi trong quá trình upload, download hoặc delete audio file
 */
public class AudioStorageException extends RuntimeException {
    
    public AudioStorageException(String message) {
        super(message);
    }
    
    public AudioStorageException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public AudioStorageException(Throwable cause) {
        super(cause);
    }
}
