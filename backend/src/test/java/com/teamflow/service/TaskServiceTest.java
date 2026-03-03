package com.teamflow.service;

import com.teamflow.dto.request.CreateTaskRequest;
import com.teamflow.dto.request.UpdateTaskRequest;
import com.teamflow.dto.response.TaskResponse;
import com.teamflow.entity.*;
import com.teamflow.exception.ResourceNotFoundException;
import com.teamflow.mapper.TaskMapper;
import com.teamflow.repository.ProjectRepository;
import com.teamflow.repository.TaskRepository;
import com.teamflow.repository.UserRepository;
import com.teamflow.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Tests")
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private UserRepository userRepository;
    @Mock private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    private User owner;
    private User assignee;
    private User stranger;
    private Project project;
    private Task task;
    private TaskResponse taskResponse;
    private CustomUserDetails ownerDetails;
    private CustomUserDetails assigneeDetails;
    private CustomUserDetails strangerDetails;

    @BeforeEach
    void setUp() {
        owner    = User.builder().id(1L).name("Owner").email("owner@test.com")
                .password("h").role(Role.USER).build();
        assignee = User.builder().id(2L).name("Assignee").email("assignee@test.com")
                .password("h").role(Role.USER).build();
        stranger = User.builder().id(3L).name("Stranger").email("stranger@test.com")
                .password("h").role(Role.USER).build();

        project = Project.builder().id(10L).name("Project").owner(owner).build();

        task = Task.builder()
                .id(100L).title("Fix bug")
                .project(project).assignee(assignee)
                .status(TaskStatus.TODO).priority(TaskPriority.MEDIUM)
                .build();

        taskResponse = TaskResponse.builder()
                .id(100L).title("Fix bug")
                .status(TaskStatus.TODO).priority(TaskPriority.MEDIUM)
                .projectId(10L)
                .build();

        ownerDetails    = new CustomUserDetails(owner);
        assigneeDetails = new CustomUserDetails(assignee);
        strangerDetails = new CustomUserDetails(stranger);
    }

    // ─────────────────────────────────────────
    // createTask tests
    // ─────────────────────────────────────────

    @Test
    @DisplayName("createTask: project owner can create a task")
    void createTask_projectOwner_createsSuccessfully() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("New Task");
        request.setPriority(TaskPriority.HIGH);

        given(projectRepository.findById(10L)).willReturn(Optional.of(project));
        given(taskRepository.save(any(Task.class))).willReturn(task);
        given(taskMapper.toResponse(task)).willReturn(taskResponse);

        TaskResponse result = taskService.createTask(10L, request, ownerDetails);

        assertThat(result).isNotNull();
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("createTask: any authenticated user can create a task (shared workspace)")
    void createTask_anyUser_canCreate() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("New task");

        given(projectRepository.findById(10L)).willReturn(Optional.of(project));
        given(taskRepository.save(any(Task.class))).willReturn(task);
        given(taskMapper.toResponse(task)).willReturn(taskResponse);

        TaskResponse result = taskService.createTask(10L, request, strangerDetails);

        assertThat(result).isNotNull();
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("createTask: with assigneeId resolves the assignee user")
    void createTask_withAssigneeId_resolvesAssignee() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Assigned Task");
        request.setAssigneeId(2L);  // assign to 'assignee' user

        given(projectRepository.findById(10L)).willReturn(Optional.of(project));
        given(userRepository.findById(2L)).willReturn(Optional.of(assignee));
        given(taskRepository.save(any(Task.class))).willReturn(task);
        given(taskMapper.toResponse(task)).willReturn(taskResponse);

        taskService.createTask(10L, request, ownerDetails);

        verify(userRepository).findById(2L);
    }

    @Test
    @DisplayName("createTask: throws ResourceNotFoundException for invalid assigneeId")
    void createTask_invalidAssigneeId_throwsResourceNotFoundException() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Task");
        request.setAssigneeId(999L);

        given(projectRepository.findById(10L)).willReturn(Optional.of(project));
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createTask(10L, request, ownerDetails))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    // ─────────────────────────────────────────
    // updateTask authorization tests
    // ─────────────────────────────────────────

    @Test
    @DisplayName("updateTask: project owner can update any task")
    void updateTask_projectOwner_updatesSuccessfully() {
        UpdateTaskRequest request = buildUpdateRequest("Updated", TaskStatus.IN_PROGRESS);

        given(taskRepository.findById(100L)).willReturn(Optional.of(task));
        given(taskRepository.save(task)).willReturn(task);
        given(taskMapper.toResponse(task)).willReturn(taskResponse);

        taskService.updateTask(100L, request, ownerDetails);

        verify(taskRepository).save(task);
    }

    @Test
    @DisplayName("updateTask: assignee can update their own task")
    void updateTask_assignee_updatesSuccessfully() {
        UpdateTaskRequest request = buildUpdateRequest("In progress", TaskStatus.IN_PROGRESS);

        given(taskRepository.findById(100L)).willReturn(Optional.of(task));
        given(taskRepository.save(task)).willReturn(task);
        given(taskMapper.toResponse(task)).willReturn(taskResponse);

        taskService.updateTask(100L, request, assigneeDetails);

        verify(taskRepository).save(task);
    }

    @Test
    @DisplayName("updateTask: any authenticated user can update any task (shared workspace)")
    void updateTask_anyUser_canUpdate() {
        UpdateTaskRequest request = buildUpdateRequest("Updated", TaskStatus.DONE);

        given(taskRepository.findById(100L)).willReturn(Optional.of(task));
        given(taskRepository.save(task)).willReturn(task);
        given(taskMapper.toResponse(task)).willReturn(taskResponse);

        taskService.updateTask(100L, request, strangerDetails);

        verify(taskRepository).save(task);
    }

    // ─────────────────────────────────────────
    // deleteTask tests
    // ─────────────────────────────────────────

    @Test
    @DisplayName("deleteTask: project owner can delete task")
    void deleteTask_projectOwner_deletesSuccessfully() {
        given(taskRepository.findById(100L)).willReturn(Optional.of(task));

        taskService.deleteTask(100L, ownerDetails);

        verify(taskRepository).delete(task);
    }

    @Test
    @DisplayName("deleteTask: any authenticated user can delete any task (shared workspace)")
    void deleteTask_anyUser_canDelete() {
        given(taskRepository.findById(100L)).willReturn(Optional.of(task));

        taskService.deleteTask(100L, assigneeDetails);

        verify(taskRepository).delete(task);
    }

    // ─────────────────────────────────────────
    // getTasks tests
    // ─────────────────────────────────────────

    @Test
    @DisplayName("getTasks: returns filtered task page for project owner")
    void getTasks_projectOwner_returnsPage() {
        Page<Task> taskPage = new PageImpl<>(List.of(task));

        given(projectRepository.findById(10L)).willReturn(Optional.of(project));
        given(taskRepository.findByProjectIdWithFilters(
                any(), any(), any(), any(), any())).willReturn(taskPage);
        given(taskMapper.toResponse(task)).willReturn(taskResponse);

        var result = taskService.getTasks(10L, null, null, null,
                PageRequest.of(0, 10), ownerDetails);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    // ─────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────

    private UpdateTaskRequest buildUpdateRequest(String title, TaskStatus status) {
        UpdateTaskRequest req = new UpdateTaskRequest();
        req.setTitle(title);
        req.setStatus(status);
        return req;
    }
}
