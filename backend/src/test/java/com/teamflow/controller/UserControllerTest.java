package com.teamflow.controller;

import com.teamflow.config.SecurityConfig;
import com.teamflow.dto.response.UserResponse;
import com.teamflow.entity.Role;
import com.teamflow.entity.User;
import com.teamflow.security.CustomUserDetails;
import com.teamflow.security.CustomUserDetailsService;
import com.teamflow.security.JwtService;
import com.teamflow.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("UserController Tests")
class UserControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private UserService userService;
    @MockBean private JwtService jwtService;
    @MockBean private CustomUserDetailsService userDetailsService;

    private final User testUser = User.builder()
            .id(1L).name("Alice").email("alice@example.com")
            .password("hashed").role(Role.USER).build();

    // ─────────────────────────────────────────
    // GET /api/users
    // ─────────────────────────────────────────

    @Test
    @DisplayName("GET /users: 200 with list of users")
    void getUsers_authenticated_returns200() throws Exception {
        List<UserResponse> users = List.of(
                UserResponse.builder().id(1L).name("Alice").email("alice@example.com").role("USER").build(),
                UserResponse.builder().id(2L).name("Bob").email("bob@example.com").role("USER").build()
        );

        given(userService.getAllUsersList()).willReturn(users);

        mockMvc.perform(get("/api/users")
                        .with(SecurityMockMvcRequestPostProcessors.user(new CustomUserDetails(testUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Alice"));
    }

    @Test
    @DisplayName("GET /users: 401 without authentication")
    void getUsers_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }
}
