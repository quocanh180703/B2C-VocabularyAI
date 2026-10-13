package com.vocabularyai.speaking.exception;

/**
 * FileSizeExceededException - Raised when uploaded file exceeds size limit
 */
public class FileSizeExceededException extends RuntimeException {
    
    private final long maxSize;
    private final long actualSize;
    
    public FileSizeExceededException(String message, long maxSize, long actualSize) {
        super(message);
        this.maxSize = maxSize;
        this.actualSize = actualSize;
    }
    
    public FileSizeExceededException(String message, long maxSize, long actualSize, Throwable cause) {
        super(message, cause);
        this.maxSize = maxSize;
        this.actualSize = actualSize;
    }
    
    public long getMaxSize() {
        return maxSize;
    }
    
    public long getActualSize() {
        return actualSize;
    }
}
