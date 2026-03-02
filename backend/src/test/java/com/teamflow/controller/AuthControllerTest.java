package com.teamflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamflow.dto.request.LoginRequest;
import com.teamflow.dto.request.RegisterRequest;
import com.teamflow.dto.response.AuthResponse;
import com.teamflow.exception.EmailAlreadyExistsException;
import com.teamflow.config.SecurityConfig;
import com.teamflow.security.CustomUserDetailsService;
import com.teamflow.security.JwtService;
import com.teamflow.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for AuthController using @WebMvcTest.
 *
 * @WebMvcTest loads ONLY the web layer:
 * - Controllers, filters, security config, MockMvc
 * - Does NOT load services, repositories, JPA
 *
 * @MockBean: replaces real beans with Mockito mocks in the Spring context.
 * We must mock JwtService and CustomUserDetailsService because
 * JwtAuthenticationFilter depends on them.
 *
 * MockMvc lets us simulate HTTP requests without starting a real server.
 * We verify: status codes, JSON response structure, headers.
 */
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    // Required by JwtAuthenticationFilter (loaded by @WebMvcTest)
    @MockBean
    private JwtService jwtService;
    @MockBean
    private CustomUserDetailsService userDetailsService;

    // ─────────────────────────────────────────
    // POST /api/auth/register
    // ─────────────────────────────────────────

    @Test
    @DisplayName("POST /register: 201 Created with valid request")
    void register_validRequest_returns201() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Alice");
        request.setEmail("alice@example.com");
        request.setPassword("password123");

        AuthResponse response = AuthResponse.builder()
                .token("jwt-token").email("alice@example.com")
                .name("Alice").role("USER").build();

        given(authService.register(any())).willReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("POST /register: 400 when email is invalid")
    void register_invalidEmail_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Alice");
        request.setEmail("not-an-email");  // invalid
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    @DisplayName("POST /register: 400 when name is blank")
    void register_blankName_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("");  // blank
        request.setEmail("alice@example.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    @DisplayName("POST /register: 400 when password too short")
    void register_shortPassword_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Alice");
        request.setEmail("alice@example.com");
        request.setPassword("123");  // < 6 chars

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    @DisplayName("POST /register: 409 when email already exists")
    void register_duplicateEmail_returns409() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Alice");
        request.setEmail("taken@example.com");
        request.setPassword("password123");

        given(authService.register(any()))
                .willThrow(new EmailAlreadyExistsException("taken@example.com"));

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    // ─────────────────────────────────────────
    // POST /api/auth/login
    // ─────────────────────────────────────────

    @Test
    @DisplayName("POST /login: 200 OK with valid credentials")
    void login_validCredentials_returns200() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("password123");

        AuthResponse response = AuthResponse.builder()
                .token("jwt-token").email("alice@example.com")
                .name("Alice").role("USER").build();

        given(authService.login(any())).willReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    @DisplayName("POST /login: 401 with wrong credentials")
    void login_wrongCredentials_returns401() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("wrong");

        given(authService.login(any()))
                .willThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }
}
