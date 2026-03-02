package com.teamflow.controller;

import com.teamflow.dto.request.LoginRequest;
import com.teamflow.dto.request.RegisterRequest;
import com.teamflow.dto.response.AuthResponse;
import com.teamflow.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication endpoints — public (no JWT required).
 *
 * Controller responsibilities (thin layer):
 * 1. Parse and validate the request body (@Valid)
 * 2. Call the service
 * 3. Return the appropriate HTTP status + body
 *
 * NO business logic here. All logic lives in AuthService.
 *
 * @Valid triggers Jakarta Validation on the request DTO.
 * If validation fails → MethodArgumentNotValidException
 * → caught by GlobalExceptionHandler → 400 response.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register and login endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new USER account and returns a JWT token")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate with email and password, returns a JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
