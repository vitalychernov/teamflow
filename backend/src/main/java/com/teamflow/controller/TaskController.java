package com.teamflow.controller;

import com.teamflow.dto.request.CreateTaskRequest;
import com.teamflow.dto.request.UpdateTaskRequest;
import com.teamflow.dto.response.PageResponse;
import com.teamflow.dto.response.TaskResponse;
import com.teamflow.entity.TaskPriority;
import com.teamflow.entity.TaskStatus;
import com.teamflow.security.CustomUserDetails;
import com.teamflow.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Task CRUD with filtering.
 *
 * Task endpoints are nested under projects for creation/listing:
 *   GET  /api/projects/{projectId}/tasks   (with filters)
 *   POST /api/projects/{projectId}/tasks
 *
 * Individual task operations use flat paths:
 *   GET    /api/tasks/{id}
 *   PUT    /api/tasks/{id}
 *   DELETE /api/tasks/{id}
 *
 * Why mixed nesting?
 * Nested paths express "tasks belong to a project" for creation/listing.
 * Flat paths avoid redundancy for single-resource operations where
 * the task ID is already unique globally.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/api/projects/{projectId}/tasks")
    @Operation(summary = "Get tasks for a project",
               description = "Supports filtering by status and priority, with pagination")
    public ResponseEntity<PageResponse<TaskResponse>> getTasks(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) TaskStatus status,       // e.g. ?status=IN_PROGRESS
            @RequestParam(required = false) TaskPriority priority,   // e.g. ?priority=HIGH
            @RequestParam(required = false) Long assigneeId,         // e.g. ?assigneeId=3
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(
                taskService.getTasks(projectId, status, priority, assigneeId, pageable, currentUser));
    }

    @PostMapping("/api/projects/{projectId}/tasks")
    @Operation(summary = "Create a task in a project")
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.createTask(projectId, request, currentUser));
    }

    @GetMapping("/api/tasks/{id}")
    @Operation(summary = "Get task by ID")
    public ResponseEntity<TaskResponse> getTaskById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(taskService.getTaskById(id, currentUser));
    }

    @PutMapping("/api/tasks/{id}")
    @Operation(summary = "Update task")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(taskService.updateTask(id, request, currentUser));
    }

    @DeleteMapping("/api/tasks/{id}")
    @Operation(summary = "Delete task")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        taskService.deleteTask(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
