package com.teamflow.exception;

import com.teamflow.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Centralized exception handler for all REST controllers.
 *
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody.
 * Any exception thrown from a controller or service is caught here
 * and converted to a consistent JSON ErrorResponse.
 *
 * Without this, Spring would return its default HTML error page or
 * a partially structured error — inconsistent for API clients.
 *
 * Order of handlers: most specific → most general.
 * The catch-all Exception handler is last.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ─────────────────────────────────────────
    // 400 Bad Request
    // ─────────────────────────────────────────

    /**
     * Handles @Valid / @Validated failures.
     * Extracts field-level errors: { "email": "must be valid", "name": "required" }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null
                                ? error.getDefaultMessage()
                                : "Invalid value",
                        // If same field has multiple errors, keep first
                        (existing, replacement) -> existing
                ));

        return ResponseEntity.badRequest().body(
                ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message("Validation failed")
                        .timestamp(LocalDateTime.now())
                        .errors(errors)
                        .build()
        );
    }

    // ─────────────────────────────────────────
    // 401 Unauthorized
    // ─────────────────────────────────────────

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorResponse.builder()
                        .status(HttpStatus.UNAUTHORIZED.value())
                        .message("Invalid email or password")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    // ─────────────────────────────────────────
    // 403 Forbidden
    // ─────────────────────────────────────────

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ErrorResponse.builder()
                        .status(HttpStatus.FORBIDDEN.value())
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    // ─────────────────────────────────────────
    // 404 Not Found
    // ─────────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    // ─────────────────────────────────────────
    // 409 Conflict
    // ─────────────────────────────────────────

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailConflict(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ErrorResponse.builder()
                        .status(HttpStatus.CONFLICT.value())
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    // ─────────────────────────────────────────
    // 500 Internal Server Error — catch-all
    // ─────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        // Log the real exception but don't expose internals to the client
        // In production, use a proper logger here
        ex.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .message("An unexpected error occurred")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
}
