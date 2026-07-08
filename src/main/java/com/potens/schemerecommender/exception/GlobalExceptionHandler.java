package com.potens.schemerecommender.exception;

import com.potens.schemerecommender.dto.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ──────────────── Validation Errors (400) ────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        log.warn("Validation failed on {}: {}", request.getRequestURI(), errors);

        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed",
                "VALIDATION_ERROR", errors, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleBadJson(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.warn("Malformed request body on {}: {}", request.getRequestURI(), ex.getMessage());

        String message = "Malformed request body. Please check JSON syntax and field values.";

        if (ex.getCause() != null && ex.getCause().getMessage() != null
                && ex.getCause().getMessage().contains("not one of the values accepted")) {
            message = "Invalid enum value in request. " + extractEnumHint(ex);
        }

        return buildResponse(HttpStatus.BAD_REQUEST, message,
                "MALFORMED_REQUEST", null, request);
    }

    @ExceptionHandler(InvalidRuleException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRule(
            InvalidRuleException ex, HttpServletRequest request) {

        log.warn("Invalid rule configuration: {}", ex.getMessage());

        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(),
                "INVALID_RULE", null, request);
    }

    // ──────────────── Authentication Errors (401) ────────────────

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {

        log.warn("Failed login attempt on {}", request.getRequestURI());

        return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid username or password",
                "BAD_CREDENTIALS", null, request);
    }

    // ──────────────── Authorization Errors (403) ────────────────

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {

        log.warn("Access denied on {}: {}", request.getRequestURI(), ex.getMessage());

        return buildResponse(HttpStatus.FORBIDDEN,
                "You do not have permission to access this resource",
                "ACCESS_DENIED", null, request);
    }

    // ──────────────── Resource Errors (404, 409) ────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {

        log.warn("Resource not found: {}", ex.getMessage());

        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(),
                "RESOURCE_NOT_FOUND", null, request);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(
            DuplicateResourceException ex, HttpServletRequest request) {

        log.warn("Duplicate resource: {}", ex.getMessage());

        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(),
                "DUPLICATE_RESOURCE", null, request);
    }

    // ──────────────── Catch-All (500) ────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex, HttpServletRequest request) {

        log.error("Unhandled exception on {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.",
                "INTERNAL_ERROR", null, request);
    }

    // ──────────────── Helpers ────────────────

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status, String message, String errorCode,
            List<String> errors, HttpServletRequest request) {

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .errors(errors)
                .timestamp(LocalDateTime.now().toString())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(status).body(response);
    }

    private String extractEnumHint(HttpMessageNotReadableException ex) {
        String cause = ex.getCause().getMessage();
        try {
            int fromIndex = cause.indexOf("from String");
            if (fromIndex > 0) {
                int endIndex = cause.indexOf("]", fromIndex);
                if (endIndex > 0) {
                    return cause.substring(fromIndex, endIndex + 1);
                }
            }
        } catch (Exception ignored) {
            // Fall through
        }
        return "Check that all enum fields have valid values.";
    }
}
