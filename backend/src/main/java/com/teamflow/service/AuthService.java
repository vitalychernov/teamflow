package com.teamflow.service;

import com.teamflow.dto.request.LoginRequest;
import com.teamflow.dto.request.RegisterRequest;
import com.teamflow.dto.response.AuthResponse;
import com.teamflow.entity.Role;
import com.teamflow.entity.User;
import com.teamflow.exception.EmailAlreadyExistsException;
import com.teamflow.repository.UserRepository;
import com.teamflow.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles user registration and login.
 *
 * Registration flow:
 *   1. Check email uniqueness
 *   2. Encode password with BCrypt
 *   3. Save user to DB
 *   4. Generate JWT
 *   5. Return token + user info
 *
 * Login flow:
 *   1. AuthenticationManager verifies credentials (delegates to
 *      DaoAuthenticationProvider → CustomUserDetailsService → BCrypt check)
 *      Throws BadCredentialsException automatically if wrong
 *   2. Load user from DB
 *   3. Generate JWT
 *   4. Return token + user info
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Normalize email first — before any check or save.
        // This ensures "Alice@EXAMPLE.COM" and "alice@example.com"
        // are treated as the same user at every step.
        String email = request.getEmail().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        User user = User.builder()
                .name(request.getName())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail());

        return buildAuthResponse(token, user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // authenticate() throws BadCredentialsException if wrong credentials —
        // Spring Security handles this automatically, no manual check needed
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

        String token = jwtService.generateToken(user.getEmail());

        return buildAuthResponse(token, user);
    }

    private AuthResponse buildAuthResponse(String token, User user) {
        return AuthResponse.builder()
                .id(user.getId())
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }
}
