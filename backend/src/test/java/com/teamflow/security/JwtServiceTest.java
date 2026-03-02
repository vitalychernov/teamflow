package com.teamflow.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for JwtService.
 *
 * Challenge: JwtService uses @Value fields (secret, expiration).
 * @Value injection only works inside Spring context, but we want
 * a fast unit test without Spring.
 *
 * Solution: ReflectionTestUtils.setField() — Spring's test utility
 * that injects values into private fields via reflection.
 * This keeps the test fast while still testing real behavior.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService Tests")
class JwtServiceTest {

    private JwtService jwtService;

    // Must be at least 32 characters for HS256 (256-bit key)
    private static final String TEST_SECRET = "test-secret-key-for-unit-tests-minimum-32-chars!!";
    private static final long TEST_EXPIRATION = 3_600_000L; // 1 hour

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Inject @Value fields manually via reflection
        ReflectionTestUtils.setField(jwtService, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", TEST_EXPIRATION);
    }

    @Test
    @DisplayName("generateToken: should return a non-blank token")
    void generateToken_returnsNonBlankToken() {
        String token = jwtService.generateToken("user@example.com");

        assertThat(token).isNotBlank();
        // JWT always has 3 parts separated by dots
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("extractEmail: should return correct email from token")
    void extractEmail_returnsCorrectEmail() {
        String email = "user@example.com";
        String token = jwtService.generateToken(email);

        String extracted = jwtService.extractEmail(token);

        assertThat(extracted).isEqualTo(email);
    }

    @Test
    @DisplayName("extractEmail: should return null for invalid token")
    void extractEmail_invalidToken_returnsNull() {
        String result = jwtService.extractEmail("this.is.not.a.valid.jwt");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("isTokenValid: should return true for valid token and matching user")
    void isTokenValid_validTokenMatchingUser_returnsTrue() {
        String email = "user@example.com";
        String token = jwtService.generateToken(email);
        UserDetails userDetails = buildUserDetails(email);

        boolean valid = jwtService.isTokenValid(token, userDetails);

        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("isTokenValid: should return false when email does not match")
    void isTokenValid_emailMismatch_returnsFalse() {
        // Token generated for one user, validated against another
        String token = jwtService.generateToken("alice@example.com");
        UserDetails differentUser = buildUserDetails("bob@example.com");

        boolean valid = jwtService.isTokenValid(token, differentUser);

        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("isTokenValid: should return false for expired token")
    void isTokenValid_expiredToken_returnsFalse() {
        // Create a JwtService with 0ms expiration → token expires instantly
        JwtService expiredJwtService = new JwtService();
        ReflectionTestUtils.setField(expiredJwtService, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(expiredJwtService, "expiration", -1000L); // already expired

        String token = expiredJwtService.generateToken("user@example.com");
        UserDetails userDetails = buildUserDetails("user@example.com");

        boolean valid = expiredJwtService.isTokenValid(token, userDetails);

        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("isTokenValid: should return false for tampered token")
    void isTokenValid_tamperedToken_returnsFalse() {
        String token = jwtService.generateToken("user@example.com");
        // Tamper the signature (last part after final dot)
        String tampered = token.substring(0, token.lastIndexOf('.')) + ".invalidsignature";
        UserDetails userDetails = buildUserDetails("user@example.com");

        boolean valid = jwtService.isTokenValid(tampered, userDetails);

        assertThat(valid).isFalse();
    }

    // Creates a Spring Security UserDetails with the given username
    private UserDetails buildUserDetails(String email) {
        return User.builder()
                .username(email)
                .password("password")
                .authorities(List.of())
                .build();
    }
}
