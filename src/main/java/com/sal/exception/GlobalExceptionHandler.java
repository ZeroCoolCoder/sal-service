package com.sal.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for REST API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ObjectNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleObjectNotFound(ObjectNotFoundException ex) {
        logger.warn("Object not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "OBJECT_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<Map<String, Object>> handleStorageError(StorageException ex) {
        logger.error("Storage error: {}", ex.getMessage(), ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE_ERROR", ex.getMessage());
    }

    @ExceptionHandler(ChecksumMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleChecksumMismatch(ChecksumMismatchException ex) {
        logger.error("Checksum mismatch: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, "CHECKSUM_MISMATCH", ex.getMessage());
    }

    @ExceptionHandler(InvalidStatusException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidStatus(InvalidStatusException ex) {
        logger.warn("Invalid status for operation: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, "INVALID_STATUS", ex.getMessage());
    }

    @ExceptionHandler(ConcurrencyException.class)
    public ResponseEntity<Map<String, Object>> handleConcurrency(ConcurrencyException ex) {
        logger.warn("Concurrency conflict: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, "CONCURRENCY_CONFLICT", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        logger.warn("Validation error: {}", ex.getMessage());
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "VALIDATION_ERROR");
        
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            fieldErrors.put(error.getField(), error.getDefaultMessage()));
        response.put("fieldErrors", fieldErrors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "INTERNAL_ERROR");
        response.put("message", ex.getMessage());
        response.put("exceptionType", ex.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status, String error, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", error);
        response.put("message", message);
        return ResponseEntity.status(status).body(response);
    }
}
