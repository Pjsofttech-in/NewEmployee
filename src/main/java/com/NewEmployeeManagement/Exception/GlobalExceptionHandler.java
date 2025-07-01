package com.NewEmployeeManagement.Exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        return createDetailedErrorResponse(HttpStatus.FORBIDDEN, ex, request.getRequestURI());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return createDetailedErrorResponse(HttpStatus.NOT_FOUND, ex, request.getRequestURI());
    }

    @ExceptionHandler({AuthenticationException.class, AuthenticationCredentialsNotFoundException.class})
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(Exception ex, HttpServletRequest request) {
        return createDetailedErrorResponse(HttpStatus.UNAUTHORIZED, ex, request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex, HttpServletRequest request) {
        return createDetailedErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex, request.getRequestURI());
    }

    private ResponseEntity<Map<String, Object>> createDetailedErrorResponse(HttpStatus status, Exception ex, String path) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", ex.getMessage());
        body.put("exception", ex.getClass().getSimpleName());
        body.put("path", path);

        Throwable rootCause = ex.getCause();
        if (rootCause != null) {
            body.put("cause", rootCause.getMessage());
        }

        return new ResponseEntity<>(body, status);
    }

    // New handlers added from your second snippet:

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Bad Request");
        error.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Internal Server Error");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}