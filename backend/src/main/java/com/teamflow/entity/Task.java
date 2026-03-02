package com.teamflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Task entity — maps to the 'tasks' table.
 *
 * Relationships:
 * - ManyToOne → Project: many tasks belong to one project
 * - ManyToOne → User (assignee): nullable — task may be unassigned
 *
 * Note on @Data with bidirectional relationships:
 * @Data generates toString() and equals/hashCode using all fields,
 * which causes StackOverflowError with bidirectional refs (Task → Project → tasks → Task...).
 * Solution: exclude collection side from toString/equals.
 * Here Task → Project is unidirectional from Task's side, so it's safe.
 */
@Entity
@Table(name = "tasks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Task status — defaults to TODO on creation.
     * @Builder.Default ensures Lombok's builder also uses this default.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TaskStatus status = TaskStatus.TODO;

    /**
     * Task priority — defaults to MEDIUM.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIUM;

    /**
     * Owning side of the Project ↔ Task relationship.
     * The FK 'project_id' lives in the 'tasks' table.
     * nullable = false: every task must belong to a project.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /**
     * Optional assignee — can be null (unassigned task).
     * nullable = true (default) — no NOT NULL constraint.
     * When the assigned user is deleted, we set this to NULL
     * rather than deleting the task (ON DELETE SET NULL behavior
     * handled at application level via cascade settings).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id", nullable = true)
    private User assignee;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
