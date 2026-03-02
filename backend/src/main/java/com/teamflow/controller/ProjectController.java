package com.teamflow.controller;

import com.teamflow.dto.request.CreateProjectRequest;
import com.teamflow.dto.request.UpdateProjectRequest;
import com.teamflow.dto.response.PageResponse;
import com.teamflow.dto.response.ProjectResponse;
import com.teamflow.security.CustomUserDetails;
import com.teamflow.service.ProjectService;
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
 * REST controller for Project CRUD.
 *
 * @AuthenticationPrincipal CustomUserDetails currentUser:
 * Spring Security injects the currently authenticated user from
 * the SecurityContext. This is set by JwtAuthenticationFilter.
 *
 * Pagination params from query string:
 * GET /api/projects?page=0&size=10&sort=createdAt,desc&keyword=alpha
 *
 * We build Pageable manually instead of using @PageableDefault
 * to have explicit control over defaults and to avoid exposing
 * Spring's Pageable resolver in the Swagger UI.
 */
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Project management endpoints")
@SecurityRequirement(name = "bearerAuth")  // tells Swagger this needs JWT
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    @Operation(summary = "Get all projects", description = "Returns paginated list of projects. ADMIN sees all, USER sees own.")
    public ResponseEntity<PageResponse<ProjectResponse>> getProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(projectService.getProjects(currentUser, keyword, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID")
    public ResponseEntity<ProjectResponse> getProjectById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(projectService.getProjectById(id, currentUser));
    }

    @PostMapping
    @Operation(summary = "Create a new project")
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.createProject(request, currentUser));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update project")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(projectService.updateProject(id, request, currentUser));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete project")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        projectService.deleteProject(id, currentUser);
        return ResponseEntity.noContent().build();  // 204 No Content
    }
}
