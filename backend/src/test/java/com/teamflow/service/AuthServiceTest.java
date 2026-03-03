package com.teamflow.service;

import com.teamflow.dto.request.LoginRequest;
import com.teamflow.dto.request.RegisterRequest;
import com.teamflow.dto.response.AuthResponse;
import com.teamflow.entity.Role;
import com.teamflow.entity.User;
import com.teamflow.exception.EmailAlreadyExistsException;
import com.teamflow.repository.UserRepository;
import com.teamflow.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/** Unit tests for AuthService. */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    // ─────────────────────────────────────────
    // register() tests
    // ─────────────────────────────────────────

    @Test
    @DisplayName("register: should save user and return token when email is available")
    void register_whenEmailAvailable_savesUserAndReturnsToken() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Alice");
        request.setEmail("alice@example.com");
        request.setPassword("password123");

        given(userRepository.existsByEmail("alice@example.com")).willReturn(false);
        given(passwordEncoder.encode("password123")).willReturn("hashed-password");
        given(jwtService.generateToken("alice@example.com")).willReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getEmail()).isEqualTo("alice@example.com");
        assertThat(response.getName()).isEqualTo("Alice");
        assertThat(response.getRole()).isEqualTo("USER");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register: should throw EmailAlreadyExistsException when email is taken")
    void register_whenEmailTaken_throwsEmailAlreadyExistsException() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Bob");
        request.setEmail("taken@example.com");
        request.setPassword("password123");

        given(userRepository.existsByEmail("taken@example.com")).willReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("taken@example.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register: should store email in lower case")
    void register_normalizesEmailToLowerCase() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Alice");
        request.setEmail("Alice@EXAMPLE.COM");
        request.setPassword("password123");

        given(userRepository.existsByEmail("alice@example.com")).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn("hashed");
        given(jwtService.generateToken("alice@example.com")).willReturn("token");

        AuthResponse response = authService.register(request);

        assertThat(response.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    @DisplayName("register: should encode password — never store plain text")
    void register_encodesPassword() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Alice");
        request.setEmail("alice@example.com");
        request.setPassword("plain-password");

        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(passwordEncoder.encode("plain-password")).willReturn("$2a$10$hashed");
        given(jwtService.generateToken(anyString())).willReturn("token");

        authService.register(request);

        verify(passwordEncoder).encode("plain-password");
    }

    // ─────────────────────────────────────────
    // login() tests
    // ─────────────────────────────────────────

    @Test
    @DisplayName("login: should return token when credentials are valid")
    void login_withValidCredentials_returnsToken() {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("password123");

        User user = User.builder()
                .id(1L)
                .name("Alice")
                .email("alice@example.com")
                .password("hashed")
                .role(Role.USER)
                .build();

        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(null);
        given(userRepository.findByEmail("alice@example.com")).willReturn(Optional.of(user));
        given(jwtService.generateToken("alice@example.com")).willReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getEmail()).isEqualTo("alice@example.com");
        assertThat(response.getRole()).isEqualTo("USER");
    }

    @Test
    @DisplayName("login: should throw BadCredentialsException when password is wrong")
    void login_withWrongPassword_throwsBadCredentialsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("wrong-password");

        given(authenticationManager.authenticate(any()))
                .willThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(userRepository, never()).findByEmail(anyString());
    }
}
