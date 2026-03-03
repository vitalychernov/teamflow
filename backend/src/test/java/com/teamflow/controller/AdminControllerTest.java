package com.teamflow.controller;

import com.teamflow.config.SecurityConfig;
import com.teamflow.dto.response.PageResponse;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("AdminController Tests")
class AdminControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private UserService userService;
    @MockBean private JwtService jwtService;
    @MockBean private CustomUserDetailsService userDetailsService;

    private final User adminUser = User.builder()
            .id(1L).name("Admin").email("admin@teamflow.com")
            .password("hashed").role(Role.ADMIN).build();

    private final User regularUser = User.builder()
            .id(2L).name("Alice").email("alice@example.com")
            .password("hashed").role(Role.USER).build();

    // ─────────────────────────────────────────
    // GET /api/admin/users
    // ─────────────────────────────────────────

    @Test
    @DisplayName("GET /admin/users: 200 when authenticated as ADMIN")
    void getUsers_asAdmin_returns200() throws Exception {
        UserResponse userResponse = UserResponse.builder()
                .id(2L).name("Alice").email("alice@example.com").role("USER").build();

        PageResponse<UserResponse> page = PageResponse.<UserResponse>builder()
                .content(List.of(userResponse)).page(0).size(20)
                .totalElements(1).totalPages(1).last(true).build();

        given(userService.getAllUsers(any())).willReturn(page);

        mockMvc.perform(get("/api/admin/users")
                        .with(SecurityMockMvcRequestPostProcessors.user(new CustomUserDetails(adminUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].email").value("alice@example.com"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /admin/users: 403 when authenticated as regular USER")
    void getUsers_asUser_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .with(SecurityMockMvcRequestPostProcessors.user(new CustomUserDetails(regularUser))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /admin/users: 401 without authentication")
    void getUsers_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }
}
