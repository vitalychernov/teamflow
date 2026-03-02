package com.teamflow.exception;

/**
 * Thrown during registration when the email is already taken.
 * Maps to HTTP 409 Conflict in GlobalExceptionHandler.
 */
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("Email already in use: " + email);
    }
}
