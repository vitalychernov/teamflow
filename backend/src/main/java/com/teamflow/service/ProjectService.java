package com.teamflow.service;

import com.teamflow.dto.request.CreateProjectRequest;
import com.teamflow.dto.request.UpdateProjectRequest;
import com.teamflow.dto.response.PageResponse;
import com.teamflow.dto.response.ProjectResponse;
import com.teamflow.entity.Project;
import com.teamflow.entity.User;
import com.teamflow.exception.ForbiddenException;
import com.teamflow.exception.ResourceNotFoundException;
import com.teamflow.mapper.ProjectMapper;
import com.teamflow.repository.ProjectRepository;
import com.teamflow.repository.UserRepository;
import com.teamflow.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Business logic for Project CRUD operations.
 *
 * Authorization rules (enforced at service level):
 * - Any authenticated user can create projects
 * - Only the owner OR an ADMIN can update/delete a project
 * - Any authenticated user can read projects (their own)
 * - ADMIN can read all projects
 *
 * Why pass CustomUserDetails instead of just userId?
 * It carries both userId and isAdmin() — avoids two separate parameters.
 * The service doesn't interact with Spring Security directly — it just
 * reads data from the already-authenticated principal.
 *
 * @Transactional(readOnly = true) on read methods:
 * - Keeps JPA session open → avoids LazyInitializationException
 *   when mappers access owner (LAZY-loaded association)
 * - Tells the DB driver it's a read-only transaction (can optimize)
 */
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;

    @Transactional(readOnly = true)
    public PageResponse<ProjectResponse> getProjects(CustomUserDetails currentUser,
                                                     String keyword,
                                                     Pageable pageable) {
        Page<Project> projects;

        if (StringUtils.hasText(keyword)) {
            // Search by name — always scoped to current user (even ADMIN sees own projects here)
            projects = projectRepository.searchByOwnerAndName(
                    currentUser.getId(), keyword, pageable);
        } else if (currentUser.isAdmin()) {
            // ADMIN with no keyword — see all projects in the system
            projects = projectRepository.findAll(pageable);
        } else {
            // Regular user — see only their own projects
            projects = projectRepository.findByOwnerId(currentUser.getId(), pageable);
        }

        return PageResponse.from(projects.map(projectMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long projectId, CustomUserDetails currentUser) {
        Project project = findProjectOrThrow(projectId);
        validateReadAccess(project, currentUser);
        return projectMapper.toResponse(project);
    }

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request,
                                         CustomUserDetails currentUser) {
        User owner = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(owner)
                .build();

        return projectMapper.toResponse(projectRepository.save(project));
    }

    @Transactional
    public ProjectResponse updateProject(Long projectId,
                                         UpdateProjectRequest request,
                                         CustomUserDetails currentUser) {
        Project project = findProjectOrThrow(projectId);
        validateWriteAccess(project, currentUser);

        project.setName(request.getName());
        project.setDescription(request.getDescription());

        return projectMapper.toResponse(projectRepository.save(project));
    }

    @Transactional
    public void deleteProject(Long projectId, CustomUserDetails currentUser) {
        Project project = findProjectOrThrow(projectId);
        validateWriteAccess(project, currentUser);
        // cascade = ALL → deletes all tasks automatically
        projectRepository.delete(project);
    }

    // ─────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────

    private Project findProjectOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id: " + projectId));
    }

    private void validateReadAccess(Project project, CustomUserDetails currentUser) {
        boolean isOwner = project.getOwner().getId().equals(currentUser.getId());
        if (!isOwner && !currentUser.isAdmin()) {
            throw new ForbiddenException("You don't have access to this project");
        }
    }

    private void validateWriteAccess(Project project, CustomUserDetails currentUser) {
        boolean isOwner = project.getOwner().getId().equals(currentUser.getId());
        if (!isOwner && !currentUser.isAdmin()) {
            throw new ForbiddenException("You don't have permission to modify this project");
        }
    }
}
