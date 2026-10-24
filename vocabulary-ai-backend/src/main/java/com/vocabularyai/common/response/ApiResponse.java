package com.vocabularyai.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Unified API Response wrapper for all endpoints
 * 
 * Success format:
 * {
 *   "success": true,
 *   "message": "Operation successful",
 *   "data": { ... },
 *   "timestamp": "2024-01-01T12:00:00"
 * }
 * 
 * Error format:
 * {
 *   "success": false,
 *   "message": "Error message",
 *   "errors": [...],
 *   "timestamp": "2024-01-01T12:00:00"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    private boolean success;
    private String code;
    private String message;
    private T data;
    private Object errors;
    private LocalDateTime timestamp;
    
    /**
     * Create successful response with data
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
            .success(true)
            .message(message)
            .data(data)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    /**
     * Create successful response without data
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
            .success(true)
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    /**
     * Create error response with error details
     */
    public static <T> ApiResponse<T> error(String message, Object errors) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .errors(errors)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    /**
     * Create error response without details
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();
    }

    /**
     * Create error response with specific error code and data (e.g. for QUOTA_EXCEEDED, UPGRADE_REQUIRED)
     */
    public static <T> ApiResponse<T> errorWithCode(String code, String message, T data) {
        return ApiResponse.<T>builder()
            .success(false)
            .code(code)
            .message(message)
            .data(data)
            .timestamp(LocalDateTime.now())
            .build();
    }
}
