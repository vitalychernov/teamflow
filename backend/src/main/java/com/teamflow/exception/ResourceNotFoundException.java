package com.teamflow.exception;

/**
 * Thrown when a requested resource does not exist in the database.
 * Maps to HTTP 404 Not Found in GlobalExceptionHandler.
 *
 * Extends RuntimeException (unchecked) — callers don't need to
 * declare it in throws clauses. Standard practice for Spring apps.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
