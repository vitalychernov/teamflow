package com.teamflow.exception;

/**
 * Thrown when a user tries to access or modify a resource they don't own.
 * Maps to HTTP 403 Forbidden in GlobalExceptionHandler.
 *
 * Why not use Spring's AccessDeniedException?
 * Spring's AccessDeniedException triggers Spring Security's access denied
 * handler, which may redirect to a login page. Our ForbiddenException
 * is handled by our own GlobalExceptionHandler for a clean JSON response.
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
