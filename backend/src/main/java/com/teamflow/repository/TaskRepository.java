package com.teamflow.repository;

import com.teamflow.entity.Task;
import com.teamflow.entity.TaskPriority;
import com.teamflow.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for Task entity.
 *
 * Key challenge: filtering by optional parameters (status, priority).
 * If we used method naming, we'd need a separate method for every combination:
 * - findByProjectId
 * - findByProjectIdAndStatus
 * - findByProjectIdAndPriority
 * - findByProjectIdAndStatusAndPriority
 * ... that's unmaintainable.
 *
 * Solution: @Query with conditional JPQL using (:param IS NULL OR t.field = :param).
 * This pattern lets a single query handle all filter combinations.
 * When null is passed, the condition is always true (filter ignored).
 *
 * Alternative for complex filtering: Spring Data JPA Specifications (Criteria API).
 * More flexible but more verbose. For this scope, @Query is sufficient.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Fetch tasks for a project with optional status and priority filters.
     *
     * Filter logic:
     * - status = null → ignore status filter (return all statuses)
     * - status = TaskStatus.TODO → return only TODO tasks
     * Same for priority.
     *
     * The 'cast(null as string)' trick: H2 (test DB) needs explicit cast
     * for null comparisons. For PostgreSQL this works as-is.
     */
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:priority IS NULL OR t.priority = :priority)")
    Page<Task> findByProjectIdWithFilters(@Param("projectId") Long projectId,
                                          @Param("status") TaskStatus status,
                                          @Param("priority") TaskPriority priority,
                                          Pageable pageable);

    /**
     * Fetch all tasks assigned to a specific user across all projects.
     * Useful for a "My Tasks" dashboard view.
     */
    Page<Task> findByAssigneeId(Long assigneeId, Pageable pageable);

    /**
     * Count tasks by status for a project — useful for board statistics.
     * Returns a single long value, no pagination needed.
     */
    long countByProjectIdAndStatus(Long projectId, TaskStatus status);

    /**
     * Check if a task belongs to a specific project.
     * Used for authorization validation in the service layer.
     */
    boolean existsByIdAndProjectId(Long taskId, Long projectId);
}
