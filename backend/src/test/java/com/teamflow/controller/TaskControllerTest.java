package com.teamflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamflow.config.SecurityConfig;
import com.teamflow.dto.request.CreateTaskRequest;
import com.teamflow.dto.request.UpdateTaskRequest;
import com.teamflow.dto.response.PageResponse;
import com.teamflow.dto.response.TaskResponse;
import com.teamflow.entity.Role;
import com.teamflow.entity.TaskPriority;
import com.teamflow.entity.TaskStatus;
import com.teamflow.entity.User;
import com.teamflow.exception.ResourceNotFoundException;
import com.teamflow.security.CustomUserDetails;
import com.teamflow.security.CustomUserDetailsService;
import com.teamflow.security.JwtService;
import com.teamflow.service.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("TaskController Tests")
class TaskControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private TaskService taskService;
    @MockBean private JwtService jwtService;
    @MockBean private CustomUserDetailsService userDetailsService;

    private final User testUser = User.builder()
            .id(1L).name("Alice").email("alice@example.com")
            .password("hashed").role(Role.USER).build();

    private final CustomUserDetails userDetails = new CustomUserDetails(testUser);

    // ─────────────────────────────────────────
    // GET /api/projects/{projectId}/tasks
    // ─────────────────────────────────────────

    @Test
    @DisplayName("GET /projects/{id}/tasks: 200 with paginated response")
    void getTasks_authenticated_returns200() throws Exception {
        TaskResponse task = TaskResponse.builder()
                .id(1L).title("Fix bug").status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH).projectId(1L).build();

        PageResponse<TaskResponse> page = PageResponse.<TaskResponse>builder()
                .content(List.of(task)).page(0).size(10)
                .totalElements(1).totalPages(1).last(true).build();

        given(taskService.getTasks(any(), any(), any(), any(), any(), any())).willReturn(page);

        mockMvc.perform(get("/api/projects/1/tasks")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("Fix bug"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /projects/{id}/tasks: 401 without authentication")
    void getTasks_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/projects/1/tasks"))
                .andExpect(status().isUnauthorized());
    }

    // ─────────────────────────────────────────
    // POST /api/projects/{projectId}/tasks
    // ─────────────────────────────────────────

    @Test
    @DisplayName("POST /projects/{id}/tasks: 201 Created with valid request")
    void createTask_validRequest_returns201() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("New Task");
        request.setPriority(TaskPriority.MEDIUM);

        TaskResponse response = TaskResponse.builder()
                .id(1L).title("New Task").status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM).projectId(1L).build();

        given(taskService.createTask(eq(1L), any(), any())).willReturn(response);

        mockMvc.perform(post("/api/projects/1/tasks")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("New Task"));
    }

    @Test
    @DisplayName("POST /projects/{id}/tasks: 400 when title is blank")
    void createTask_blankTitle_returns400() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("");

        mockMvc.perform(post("/api/projects/1/tasks")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());
    }

    // ─────────────────────────────────────────
    // GET /api/tasks/{id}
    // ─────────────────────────────────────────

    @Test
    @DisplayName("GET /tasks/{id}: 200 OK")
    void getTaskById_found_returns200() throws Exception {
        TaskResponse response = TaskResponse.builder()
                .id(1L).title("Fix bug").status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.HIGH).projectId(1L).build();

        given(taskService.getTaskById(eq(1L), any())).willReturn(response);

        mockMvc.perform(get("/api/tasks/1")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Fix bug"));
    }

    @Test
    @DisplayName("GET /tasks/{id}: 404 when not found")
    void getTaskById_notFound_returns404() throws Exception {
        given(taskService.getTaskById(eq(99L), any()))
                .willThrow(new ResourceNotFoundException("Task not found with id: 99"));

        mockMvc.perform(get("/api/tasks/99")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found with id: 99"));
    }

    // ─────────────────────────────────────────
    // PUT /api/tasks/{id}
    // ─────────────────────────────────────────

    @Test
    @DisplayName("PUT /tasks/{id}: 200 OK with valid request")
    void updateTask_validRequest_returns200() throws Exception {
        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitle("Updated Task");
        request.setStatus(TaskStatus.IN_PROGRESS);
        request.setPriority(TaskPriority.HIGH);

        TaskResponse response = TaskResponse.builder()
                .id(1L).title("Updated Task").status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.HIGH).projectId(1L).build();

        given(taskService.updateTask(eq(1L), any(), any())).willReturn(response);

        mockMvc.perform(put("/api/tasks/1")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Task"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    // ─────────────────────────────────────────
    // DELETE /api/tasks/{id}
    // ─────────────────────────────────────────

    @Test
    @DisplayName("DELETE /tasks/{id}: 204 No Content on success")
    void deleteTask_success_returns204() throws Exception {
        doNothing().when(taskService).deleteTask(eq(1L), any());

        mockMvc.perform(delete("/api/tasks/1")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
