package com.teamflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamflow.dto.request.CreateProjectRequest;
import com.teamflow.dto.response.PageResponse;
import com.teamflow.dto.response.ProjectResponse;
import com.teamflow.entity.Role;
import com.teamflow.entity.User;
import com.teamflow.exception.ForbiddenException;
import com.teamflow.exception.ResourceNotFoundException;
import com.teamflow.security.CustomUserDetails;
import com.teamflow.security.CustomUserDetailsService;
import com.teamflow.security.JwtService;
import com.teamflow.service.ProjectService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for ProjectController.
 *
 * Key technique: SecurityMockMvcRequestPostProcessors.user(userDetails)
 * Injects a CustomUserDetails into the SecurityContext for this request.
 * This simulates an authenticated user without going through JWT filter.
 *
 * This is cleaner than @WithMockUser because it uses our actual
 * CustomUserDetails class, making @AuthenticationPrincipal work correctly.
 */
@WebMvcTest(ProjectController.class)
@ActiveProfiles("test")
@DisplayName("ProjectController Tests")
class ProjectControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private ProjectService projectService;
    @MockBean private JwtService jwtService;
    @MockBean private CustomUserDetailsService userDetailsService;

    private final User testUser = User.builder()
            .id(1L).name("Alice").email("alice@example.com")
            .password("hashed").role(Role.USER).build();

    private final CustomUserDetails userDetails = new CustomUserDetails(testUser);

    // ─────────────────────────────────────────
    // GET /api/projects
    // ─────────────────────────────────────────

    @Test
    @DisplayName("GET /projects: 200 with paginated response")
    void getProjects_authenticated_returns200() throws Exception {
        ProjectResponse project = ProjectResponse.builder()
                .id(1L).name("My Project").build();

        PageResponse<ProjectResponse> page = PageResponse.<ProjectResponse>builder()
                .content(List.of(project)).page(0).size(10)
                .totalElements(1).totalPages(1).last(true).build();

        given(projectService.getProjects(any(), any(), any())).willReturn(page);

        mockMvc.perform(get("/api/projects")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("My Project"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /projects: 401 without authentication")
    void getProjects_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isUnauthorized());
    }

    // ─────────────────────────────────────────
    // POST /api/projects
    // ─────────────────────────────────────────

    @Test
    @DisplayName("POST /projects: 201 Created with valid request")
    void createProject_validRequest_returns201() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("New Project");

        ProjectResponse response = ProjectResponse.builder()
                .id(1L).name("New Project").build();

        given(projectService.createProject(any(), any())).willReturn(response);

        mockMvc.perform(post("/api/projects")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("New Project"));
    }

    @Test
    @DisplayName("POST /projects: 400 when name is blank")
    void createProject_blankName_returns400() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("");

        mockMvc.perform(post("/api/projects")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    // ─────────────────────────────────────────
    // GET /api/projects/{id}
    // ─────────────────────────────────────────

    @Test
    @DisplayName("GET /projects/{id}: 404 when not found")
    void getProjectById_notFound_returns404() throws Exception {
        given(projectService.getProjectById(any(), any()))
                .willThrow(new ResourceNotFoundException("Project not found with id: 99"));

        mockMvc.perform(get("/api/projects/99")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Project not found with id: 99"));
    }

    @Test
    @DisplayName("GET /projects/{id}: 403 when user doesn't have access")
    void getProjectById_forbidden_returns403() throws Exception {
        given(projectService.getProjectById(any(), any()))
                .willThrow(new ForbiddenException("You don't have access to this project"));

        mockMvc.perform(get("/api/projects/1")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    // ─────────────────────────────────────────
    // DELETE /api/projects/{id}
    // ─────────────────────────────────────────

    @Test
    @DisplayName("DELETE /projects/{id}: 204 No Content on success")
    void deleteProject_success_returns204() throws Exception {
        mockMvc.perform(delete("/api/projects/1")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isNoContent());
    }
}
