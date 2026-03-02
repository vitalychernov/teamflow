package com.teamflow.service;

import com.teamflow.dto.request.CreateTaskRequest;
import com.teamflow.dto.request.UpdateTaskRequest;
import com.teamflow.dto.response.PageResponse;
import com.teamflow.dto.response.TaskResponse;
import com.teamflow.entity.Project;
import com.teamflow.entity.Task;
import com.teamflow.entity.TaskPriority;
import com.teamflow.entity.TaskStatus;
import com.teamflow.entity.User;
import com.teamflow.exception.ForbiddenException;
import com.teamflow.exception.ResourceNotFoundException;
import com.teamflow.mapper.TaskMapper;
import com.teamflow.repository.ProjectRepository;
import com.teamflow.repository.TaskRepository;
import com.teamflow.repository.UserRepository;
import com.teamflow.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for Task CRUD with filtering.
 *
 * Authorization rules:
 * - Any member can read tasks within a project they have access to
 * - Any authenticated user can create tasks in a project they own
 * - Assignee OR project owner OR ADMIN can update a task
 * - Project owner OR ADMIN can delete a task
 */
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> getTasks(Long projectId,
                                               TaskStatus status,
                                               TaskPriority priority,
                                               Pageable pageable,
                                               CustomUserDetails currentUser) {
        Project project = findProjectOrThrow(projectId);
        validateProjectAccess(project, currentUser);

        Page<Task> tasks = taskRepository.findByProjectIdWithFilters(
                projectId, status, priority, pageable);

        return PageResponse.from(tasks.map(taskMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long taskId, CustomUserDetails currentUser) {
        Task task = findTaskOrThrow(taskId);
        validateProjectAccess(task.getProject(), currentUser);
        return taskMapper.toResponse(task);
    }

    @Transactional
    public TaskResponse createTask(Long projectId,
                                   CreateTaskRequest request,
                                   CustomUserDetails currentUser) {
        Project project = findProjectOrThrow(projectId);
        validateProjectAccess(project, currentUser);

        Task.TaskBuilder builder = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .project(project);

        // Apply priority from request, or use entity default (MEDIUM)
        if (request.getPriority() != null) {
            builder.priority(request.getPriority());
        }

        // Resolve assignee if provided
        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Assignee not found with id: " + request.getAssigneeId()));
            builder.assignee(assignee);
        }

        return taskMapper.toResponse(taskRepository.save(builder.build()));
    }

    @Transactional
    public TaskResponse updateTask(Long taskId,
                                   UpdateTaskRequest request,
                                   CustomUserDetails currentUser) {
        Task task = findTaskOrThrow(taskId);
        validateTaskWriteAccess(task, currentUser);

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());

        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }

        // Handle assignee update: null → unassign, id → reassign
        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Assignee not found with id: " + request.getAssigneeId()));
            task.setAssignee(assignee);
        } else {
            task.setAssignee(null);  // explicit unassign
        }

        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(Long taskId, CustomUserDetails currentUser) {
        Task task = findTaskOrThrow(taskId);
        // Only project owner or ADMIN can delete
        validateProjectWriteAccess(task.getProject(), currentUser);
        taskRepository.delete(task);
    }

    // ─────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────

    private Project findProjectOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id: " + projectId));
    }

    private Task findTaskOrThrow(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Task not found with id: " + taskId));
    }

    private void validateProjectAccess(Project project, CustomUserDetails currentUser) {
        boolean isOwner = project.getOwner().getId().equals(currentUser.getId());
        if (!isOwner && !currentUser.isAdmin()) {
            throw new ForbiddenException("You don't have access to this project");
        }
    }

    private void validateProjectWriteAccess(Project project, CustomUserDetails currentUser) {
        boolean isOwner = project.getOwner().getId().equals(currentUser.getId());
        if (!isOwner && !currentUser.isAdmin()) {
            throw new ForbiddenException("You don't have permission to modify tasks in this project");
        }
    }

    private void validateTaskWriteAccess(Task task, CustomUserDetails currentUser) {
        boolean isProjectOwner = task.getProject().getOwner().getId().equals(currentUser.getId());
        boolean isAssignee = task.getAssignee() != null
                && task.getAssignee().getId().equals(currentUser.getId());
        if (!isProjectOwner && !isAssignee && !currentUser.isAdmin()) {
            throw new ForbiddenException("You don't have permission to modify this task");
        }
    }
}
