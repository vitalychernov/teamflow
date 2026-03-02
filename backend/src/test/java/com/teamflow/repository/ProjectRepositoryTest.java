package com.teamflow.repository;

import com.teamflow.entity.Project;
import com.teamflow.entity.Role;
import com.teamflow.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ProjectRepository Tests")
class ProjectRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProjectRepository projectRepository;

    private User owner;
    private User otherUser;

    @BeforeEach
    void setUp() {
        owner = entityManager.persistAndFlush(User.builder()
                .name("Alice")
                .email("alice@example.com")
                .password("hashed")
                .role(Role.USER)
                .build());

        otherUser = entityManager.persistAndFlush(User.builder()
                .name("Bob")
                .email("bob@example.com")
                .password("hashed")
                .role(Role.USER)
                .build());

        // Create 3 projects for 'owner' and 1 for 'otherUser'
        entityManager.persistAndFlush(Project.builder()
                .name("Alpha Project")
                .description("First project")
                .owner(owner)
                .build());

        entityManager.persistAndFlush(Project.builder()
                .name("Beta Project")
                .description("Second project")
                .owner(owner)
                .build());

        entityManager.persistAndFlush(Project.builder()
                .name("Gamma Project")
                .description("Third project")
                .owner(owner)
                .build());

        entityManager.persistAndFlush(Project.builder()
                .name("Bob's Project")
                .description("Belongs to Bob")
                .owner(otherUser)
                .build());
    }

    // ─────────────────────────────────────────
    // findByOwnerId tests
    // ─────────────────────────────────────────

    @Test
    @DisplayName("findByOwnerId: should return only owner's projects")
    void findByOwnerId_returnsOnlyOwnerProjects() {
        // PageRequest.of(page, size) → page 0, 10 items per page
        Page<Project> result = projectRepository.findByOwnerId(
                owner.getId(), PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(3);
        // Verify all returned projects belong to the owner
        assertThat(result.getContent())
                .extracting(p -> p.getOwner().getId())
                .containsOnly(owner.getId());
    }

    @Test
    @DisplayName("findByOwnerId: should return empty page for user with no projects")
    void findByOwnerId_whenNoProjects_returnsEmptyPage() {
        User newUser = entityManager.persistAndFlush(User.builder()
                .name("Charlie")
                .email("charlie@example.com")
                .password("hashed")
                .role(Role.USER)
                .build());

        Page<Project> result = projectRepository.findByOwnerId(
                newUser.getId(), PageRequest.of(0, 10));

        // isEmpty() on Page checks if content list is empty
        assertThat(result.isEmpty()).isTrue();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("findByOwnerId: should respect pagination size")
    void findByOwnerId_respectsPaginationSize() {
        // Request page 0, size 2 → should return 2 out of 3 projects
        Page<Project> page1 = projectRepository.findByOwnerId(
                owner.getId(), PageRequest.of(0, 2));

        assertThat(page1.getContent()).hasSize(2);
        assertThat(page1.getTotalElements()).isEqualTo(3);
        assertThat(page1.getTotalPages()).isEqualTo(2);
        assertThat(page1.hasNext()).isTrue();
    }

    @Test
    @DisplayName("findByOwnerId: should support sorting")
    void findByOwnerId_supportsSorting() {
        // Sort by name ASC
        Page<Project> result = projectRepository.findByOwnerId(
                owner.getId(),
                PageRequest.of(0, 10, Sort.by("name").ascending()));

        assertThat(result.getContent())
                .extracting(Project::getName)
                .containsExactly("Alpha Project", "Beta Project", "Gamma Project");
    }

    // ─────────────────────────────────────────
    // existsByIdAndOwnerId tests
    // ─────────────────────────────────────────

    @Test
    @DisplayName("existsByIdAndOwnerId: should return true for actual owner")
    void existsByIdAndOwnerId_forActualOwner_returnsTrue() {
        Project project = projectRepository.findByOwnerId(
                owner.getId(), PageRequest.of(0, 1)).getContent().get(0);

        boolean exists = projectRepository.existsByIdAndOwnerId(
                project.getId(), owner.getId());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByIdAndOwnerId: should return false for wrong owner")
    void existsByIdAndOwnerId_forWrongOwner_returnsFalse() {
        Project ownerProject = projectRepository.findByOwnerId(
                owner.getId(), PageRequest.of(0, 1)).getContent().get(0);

        // otherUser does NOT own this project
        boolean exists = projectRepository.existsByIdAndOwnerId(
                ownerProject.getId(), otherUser.getId());

        assertThat(exists).isFalse();
    }

    // ─────────────────────────────────────────
    // searchByOwnerAndName tests
    // ─────────────────────────────────────────

    @Test
    @DisplayName("searchByOwnerAndName: should find by partial name case-insensitively")
    void searchByOwnerAndName_partialMatch_returnsResults() {
        // "alpha" should match "Alpha Project" (case-insensitive)
        Page<Project> result = projectRepository.searchByOwnerAndName(
                owner.getId(), "alpha", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Alpha Project");
    }

    @Test
    @DisplayName("searchByOwnerAndName: should not return other user's projects")
    void searchByOwnerAndName_doesNotLeakOtherUsersProjects() {
        // "project" matches all 4 projects, but should only return owner's 3
        Page<Project> result = projectRepository.searchByOwnerAndName(
                owner.getId(), "project", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent())
                .extracting(p -> p.getOwner().getId())
                .containsOnly(owner.getId());
    }

    @Test
    @DisplayName("searchByOwnerAndName: should return empty when no match")
    void searchByOwnerAndName_noMatch_returnsEmpty() {
        Page<Project> result = projectRepository.searchByOwnerAndName(
                owner.getId(), "nonexistent", PageRequest.of(0, 10));

        assertThat(result.isEmpty()).isTrue();
    }
}
