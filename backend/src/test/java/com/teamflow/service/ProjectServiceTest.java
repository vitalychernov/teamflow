package com.teamflow.service;

import com.teamflow.dto.request.CreateProjectRequest;
import com.teamflow.dto.request.UpdateProjectRequest;
import com.teamflow.dto.response.PageResponse;
import com.teamflow.dto.response.ProjectResponse;
import com.teamflow.dto.response.UserResponse;
import com.teamflow.entity.Project;
import com.teamflow.entity.Role;
import com.teamflow.entity.User;
import com.teamflow.exception.ForbiddenException;
import com.teamflow.exception.ResourceNotFoundException;
import com.teamflow.mapper.ProjectMapper;
import com.teamflow.repository.ProjectRepository;
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
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectService Tests")
class ProjectServiceTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectService projectService;

    private User owner;
    private User otherUser;
    private User adminUser;
    private Project project;
    private ProjectResponse projectResponse;
    private CustomUserDetails ownerDetails;
    private CustomUserDetails otherDetails;
    private CustomUserDetails adminDetails;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).name("Owner").email("owner@example.com")
                .password("hashed").role(Role.USER).build();
        otherUser = User.builder().id(2L).name("Other").email("other@example.com")
                .password("hashed").role(Role.USER).build();
        adminUser = User.builder().id(3L).name("Admin").email("admin@example.com")
                .password("hashed").role(Role.ADMIN).build();

        project = Project.builder().id(10L).name("My Project")
                .description("Desc").owner(owner).build();

        projectResponse = ProjectResponse.builder()
                .id(10L).name("My Project").description("Desc")
                .owner(UserResponse.builder().id(1L).email("owner@example.com").build())
                .build();

        ownerDetails  = new CustomUserDetails(owner);
        otherDetails  = new CustomUserDetails(otherUser);
        adminDetails  = new CustomUserDetails(adminUser);
    }

    // ─────────────────────────────────────────
    // getProjectById tests
    // ─────────────────────────────────────────

    @Test
    @DisplayName("getProjectById: owner can access their own project")
    void getProjectById_owner_returnsProject() {
        given(projectRepository.findById(10L)).willReturn(Optional.of(project));
        given(projectMapper.toResponse(project)).willReturn(projectResponse);

        ProjectResponse result = projectService.getProjectById(10L, ownerDetails);

        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("getProjectById: ADMIN can access any project")
    void getProjectById_admin_returnsProject() {
        given(projectRepository.findById(10L)).willReturn(Optional.of(project));
        given(projectMapper.toResponse(project)).willReturn(projectResponse);

        ProjectResponse result = projectService.getProjectById(10L, adminDetails);

        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("getProjectById: other user gets ForbiddenException")
    void getProjectById_otherUser_throwsForbidden() {
        given(projectRepository.findById(10L)).willReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.getProjectById(10L, otherDetails))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("getProjectById: non-existent project throws ResourceNotFoundException")
    void getProjectById_notFound_throwsResourceNotFoundException() {
        given(projectRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProjectById(99L, ownerDetails))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ─────────────────────────────────────────
    // createProject tests
    // ─────────────────────────────────────────

    @Test
    @DisplayName("createProject: saves and returns project")
    void createProject_savesAndReturnsProject() {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("New Project");
        request.setDescription("Description");

        given(userRepository.findById(1L)).willReturn(Optional.of(owner));
        given(projectRepository.save(any(Project.class))).willReturn(project);
        given(projectMapper.toResponse(project)).willReturn(projectResponse);

        ProjectResponse result = projectService.createProject(request, ownerDetails);

        assertThat(result).isNotNull();
        verify(projectRepository).save(any(Project.class));
    }

    // ─────────────────────────────────────────
    // updateProject tests
    // ─────────────────────────────────────────

    @Test
    @DisplayName("updateProject: owner can update their project")
    void updateProject_owner_updatesSuccessfully() {
        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setName("Updated Name");
        request.setDescription("Updated Desc");

        given(projectRepository.findById(10L)).willReturn(Optional.of(project));
        given(projectRepository.save(any(Project.class))).willReturn(project);
        given(projectMapper.toResponse(project)).willReturn(projectResponse);

        projectService.updateProject(10L, request, ownerDetails);

        verify(projectRepository).save(project);
    }

    @Test
    @DisplayName("updateProject: non-owner gets ForbiddenException")
    void updateProject_nonOwner_throwsForbidden() {
        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setName("Hack");

        given(projectRepository.findById(10L)).willReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.updateProject(10L, request, otherDetails))
                .isInstanceOf(ForbiddenException.class);

        verify(projectRepository, never()).save(any());
    }

    // ─────────────────────────────────────────
    // deleteProject tests
    // ─────────────────────────────────────────

    @Test
    @DisplayName("deleteProject: owner can delete their project")
    void deleteProject_owner_deletesSuccessfully() {
        given(projectRepository.findById(10L)).willReturn(Optional.of(project));

        projectService.deleteProject(10L, ownerDetails);

        verify(projectRepository).delete(project);
    }

    @Test
    @DisplayName("deleteProject: ADMIN can delete any project")
    void deleteProject_admin_deletesSuccessfully() {
        given(projectRepository.findById(10L)).willReturn(Optional.of(project));

        projectService.deleteProject(10L, adminDetails);

        verify(projectRepository).delete(project);
    }

    @Test
    @DisplayName("deleteProject: non-owner gets ForbiddenException, delete not called")
    void deleteProject_nonOwner_throwsForbidden() {
        given(projectRepository.findById(10L)).willReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.deleteProject(10L, otherDetails))
                .isInstanceOf(ForbiddenException.class);

        verify(projectRepository, never()).delete(any());
    }

    // ─────────────────────────────────────────
    // getProjects pagination tests
    // ─────────────────────────────────────────

    @Test
    @DisplayName("getProjects: regular user sees only own projects")
    void getProjects_regularUser_seesOnlyOwnProjects() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> page = new PageImpl<>(List.of(project));

        given(projectRepository.findByOwnerId(eq(1L), any(Pageable.class))).willReturn(page);
        given(projectMapper.toResponse(project)).willReturn(projectResponse);

        PageResponse<ProjectResponse> result = projectService.getProjects(ownerDetails, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        // Verify ONLY own projects query was called, NOT findAll
        verify(projectRepository).findByOwnerId(eq(1L), any(Pageable.class));
        verify(projectRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("getProjects: ADMIN with no keyword sees all projects")
    void getProjects_admin_seesAllProjects() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> page = new PageImpl<>(List.of(project));

        given(projectRepository.findAll(any(Pageable.class))).willReturn(page);
        given(projectMapper.toResponse(project)).willReturn(projectResponse);

        PageResponse<ProjectResponse> result = projectService.getProjects(adminDetails, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(projectRepository).findAll(any(Pageable.class));
    }
}
