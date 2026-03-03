package com.teamflow.repository;

import com.teamflow.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("TaskRepository Tests")
class TaskRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskRepository taskRepository;

    private Project project;
    private User assignee;

    @BeforeEach
    void setUp() {
        User owner = entityManager.persistAndFlush(User.builder()
                .name("Owner")
                .email("owner@example.com")
                .password("hashed")
                .role(Role.USER)
                .build());

        assignee = entityManager.persistAndFlush(User.builder()
                .name("Assignee")
                .email("assignee@example.com")
                .password("hashed")
                .role(Role.USER)
                .build());

        project = entityManager.persistAndFlush(Project.builder()
                .name("Test Project")
                .owner(owner)
                .build());

        // 5 tasks with various status/priority combinations
        entityManager.persistAndFlush(Task.builder()
                .title("Task 1 - TODO HIGH")
                .project(project)
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .assignee(assignee)
                .build());

        entityManager.persistAndFlush(Task.builder()
                .title("Task 2 - TODO MEDIUM")
                .project(project)
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .build());

        entityManager.persistAndFlush(Task.builder()
                .title("Task 3 - IN_PROGRESS HIGH")
                .project(project)
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.HIGH)
                .assignee(assignee)
                .build());

        entityManager.persistAndFlush(Task.builder()
                .title("Task 4 - DONE LOW")
                .project(project)
                .status(TaskStatus.DONE)
                .priority(TaskPriority.LOW)
                .build());

        entityManager.persistAndFlush(Task.builder()
                .title("Task 5 - DONE MEDIUM")
                .project(project)
                .status(TaskStatus.DONE)
                .priority(TaskPriority.MEDIUM)
                .build());
    }

    // ─────────────────────────────────────────
    // findByProjectIdWithFilters — the core filter query
    // ─────────────────────────────────────────

    @Test
    @DisplayName("filter: no filters → returns all tasks for project")
    void findWithFilters_noFilters_returnsAll() {
        Page<Task> result = taskRepository.findByProjectIdWithFilters(
                project.getId(), null, null, null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(5);
    }

    @Test
    @DisplayName("filter: status=TODO → returns only TODO tasks")
    void findWithFilters_statusFilter_returnsTodoOnly() {
        Page<Task> result = taskRepository.findByProjectIdWithFilters(
                project.getId(), TaskStatus.TODO, null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Task::getStatus)
                .containsOnly(TaskStatus.TODO);
    }

    @Test
    @DisplayName("filter: priority=HIGH → returns only HIGH priority tasks")
    void findWithFilters_priorityFilter_returnsHighOnly() {
        Page<Task> result = taskRepository.findByProjectIdWithFilters(
                project.getId(), null, TaskPriority.HIGH, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Task::getPriority)
                .containsOnly(TaskPriority.HIGH);
    }

    @Test
    @DisplayName("filter: status=TODO AND priority=HIGH → returns exactly 1 task")
    void findWithFilters_bothFilters_returnsExactMatch() {
        Page<Task> result = taskRepository.findByProjectIdWithFilters(
                project.getId(), TaskStatus.TODO, TaskPriority.HIGH, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Task 1 - TODO HIGH");
    }

    @Test
    @DisplayName("filter: status=DONE → returns 2 done tasks")
    void findWithFilters_statusDone_returnsTwoDone() {
        Page<Task> result = taskRepository.findByProjectIdWithFilters(
                project.getId(), TaskStatus.DONE, null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("filter: no matching combination → returns empty page")
    void findWithFilters_noMatch_returnsEmpty() {
        // No task is IN_PROGRESS + LOW
        Page<Task> result = taskRepository.findByProjectIdWithFilters(
                project.getId(), TaskStatus.IN_PROGRESS, TaskPriority.LOW, null, PageRequest.of(0, 10));

        assertThat(result.isEmpty()).isTrue();
    }

    // ─────────────────────────────────────────
    // findByAssigneeId tests
    // ─────────────────────────────────────────

    @Test
    @DisplayName("findByAssigneeId: returns tasks assigned to user")
    void findByAssigneeId_returnsAssigneeTasks() {
        // assignee has 2 tasks (Task 1 and Task 3)
        Page<Task> result = taskRepository.findByAssigneeId(
                assignee.getId(), PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(t -> t.getAssignee().getId())
                .containsOnly(assignee.getId());
    }

    // ─────────────────────────────────────────
    // countByProjectIdAndStatus tests
    // ─────────────────────────────────────────

    @Test
    @DisplayName("countByProjectIdAndStatus: returns correct count per status")
    void countByProjectIdAndStatus_returnsCorrectCounts() {
        long todoCount = taskRepository.countByProjectIdAndStatus(
                project.getId(), TaskStatus.TODO);
        long doneCount = taskRepository.countByProjectIdAndStatus(
                project.getId(), TaskStatus.DONE);
        long inProgressCount = taskRepository.countByProjectIdAndStatus(
                project.getId(), TaskStatus.IN_PROGRESS);

        assertThat(todoCount).isEqualTo(2);
        assertThat(doneCount).isEqualTo(2);
        assertThat(inProgressCount).isEqualTo(1);
    }

    // ─────────────────────────────────────────
    // existsByIdAndProjectId tests
    // ─────────────────────────────────────────

    @Test
    @DisplayName("existsByIdAndProjectId: returns true for task belonging to project")
    void existsByIdAndProjectId_correctProject_returnsTrue() {
        Task task = taskRepository.findByProjectIdWithFilters(
                project.getId(), null, null, null, PageRequest.of(0, 1))
                .getContent().get(0);

        boolean exists = taskRepository.existsByIdAndProjectId(
                task.getId(), project.getId());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByIdAndProjectId: returns false for wrong project")
    void existsByIdAndProjectId_wrongProject_returnsFalse() {
        Task task = taskRepository.findByProjectIdWithFilters(
                project.getId(), null, null, null, PageRequest.of(0, 1))
                .getContent().get(0);

        boolean exists = taskRepository.existsByIdAndProjectId(
                task.getId(), 99999L); // non-existent project

        assertThat(exists).isFalse();
    }
}
