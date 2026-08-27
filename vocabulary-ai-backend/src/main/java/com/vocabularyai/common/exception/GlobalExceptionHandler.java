package com.vocabularyai.common.exception;

import com.vocabularyai.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global Exception Handler for all REST endpoints
 * 
 * Xử lý các exception được throw từ toàn bộ application
 * và trả về response API thống nhất
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    
    /**
     * Handle ResourceNotFoundException -> 404
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        
        ApiResponse<?> response = ApiResponse.error(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
    
    /**
     * Handle BadRequestException -> 400
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<?>> handleBadRequestException(
            BadRequestException ex, WebRequest request) {
        log.warn("Bad request: {}", ex.getMessage());
        
        ApiResponse<?> response = ApiResponse.error(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle UnauthorizedException -> 401
     * 
     * ✅ TASK 5: User Status validation
     * - "Your account is inactive (suspended). Please contact support."
     * - "Your account is banned. You cannot refresh your token."
     * 
     * ✅ TASK 6: Standardized error response
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<?>> handleUnauthorizedException(
            UnauthorizedException ex, WebRequest request) {
        log.warn("Unauthorized: {}", ex.getMessage());
        
        // TASK 6: Standardized error response with error details
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("code", "UNAUTHORIZED");
        errorDetails.put("details", ex.getMessage());
        errorDetails.put("timestamp", LocalDateTime.now());
        
        ApiResponse<?> response = ApiResponse.error(ex.getMessage(), errorDetails);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
    
    /**
     * Handle ForbiddenException -> 403
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<?>> handleForbiddenException(
            ForbiddenException ex, WebRequest request) {
        log.warn("Forbidden: {}", ex.getMessage());
        
        // TASK 6: Standardized error response
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("code", "FORBIDDEN");
        errorDetails.put("details", ex.getMessage());
        errorDetails.put("timestamp", LocalDateTime.now());
        
        ApiResponse<?> response = ApiResponse.error(ex.getMessage(), errorDetails);
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }
    
    /**
     * Handle AccessDeniedException (Spring Security) -> 403
     * 
     * ✅ TASK 6: Standardized 403 Forbidden response
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        
        // TASK 6: Standardized error response
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("code", "FORBIDDEN");
        errorDetails.put("details", "You don't have permission to access this resource");
        errorDetails.put("timestamp", LocalDateTime.now());
        
        ApiResponse<?> response = ApiResponse.error("Access denied", errorDetails);
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }
    
    /**
     * Handle DuplicateResourceException -> 409
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<?>> handleDuplicateResourceException(
            DuplicateResourceException ex, WebRequest request) {
        log.warn("Duplicate resource: {}", ex.getMessage());
        
        ApiResponse<?> response = ApiResponse.error(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }
    
    /**
     * Handle ValidationException -> 400
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(
            ValidationException ex, WebRequest request) {
        log.warn("Validation failed: {}", ex.getMessage());
        
        ApiResponse<?> response = ApiResponse.error(ex.getMessage(), ex.getErrors());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    
    /**
     * Handle AIException -> 500
     */
    @ExceptionHandler(AIException.class)
    public ResponseEntity<ApiResponse<?>> handleAIException(
            AIException ex, WebRequest request) {
        log.error("AI service error: {}", ex.getMessage(), ex);
        
        ApiResponse<?> response = ApiResponse.error("AI service error: " + ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Handle RateLimitExceededException -> 429
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiResponse<?>> handleRateLimitExceededException(
            RateLimitExceededException ex, WebRequest request) {
        log.warn("Rate limit exceeded for user. Limit type: {}, Reset time: {}", 
                ex.getLimitType(), ex.getResetTimeUnix());
        
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("resetTime", ex.getResetTimeUnix());
        errorData.put("limitType", ex.getLimitType());
        errorData.put("retryAfterSeconds", ex.getRetryAfterSeconds());
        
        ApiResponse<?> response = ApiResponse.error("Rate limit exceeded", errorData);
        
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(ex.getRetryAfterSeconds()))
                .body(response);
    }
    
    /**
     * Handle ExternalServiceException -> 503
     */
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ApiResponse<?>> handleExternalServiceException(
            ExternalServiceException ex, WebRequest request) {
        log.error("External service error: {}", ex.getMessage(), ex);
        
        ApiResponse<?> response = ApiResponse.error("External service unavailable");
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }
    
    /**
     * Handle all other exceptions -> 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGlobalException(
            Exception ex, WebRequest request) {
        log.error("Unexpected error", ex);
        
        ApiResponse<?> response = ApiResponse.error("Internal server error");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
