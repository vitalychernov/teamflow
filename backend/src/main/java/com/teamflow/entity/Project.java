package com.teamflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Project entity — maps to the 'projects' table.
 *
 * Relationships:
 * - ManyToOne → User (owner): many projects can belong to one user
 * - OneToMany → Task: one project has many tasks
 *
 * Why not @ManyToMany for members?
 * For this scope, project ownership is sufficient.
 * A full team-membership feature would use a join table.
 */
@Entity
@Table(name = "projects")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    /**
     * columnDefinition = "TEXT": unlimited length in PostgreSQL.
     * Alternative: length = 2000 with VARCHAR. TEXT is more flexible.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * ManyToOne — many projects belong to one owner.
     *
     * fetch = LAZY (default for ManyToOne in some configs, explicit here):
     * Owner is NOT loaded from DB until accessed. Avoids N+1 problems.
     *
     * @JoinColumn: creates 'owner_id' FK column in 'projects' table.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /**
     * OneToMany — one project has many tasks.
     *
     * mappedBy = "project": the 'project' field in Task owns the FK.
     * cascade = ALL: persist/delete tasks when project is persisted/deleted.
     * orphanRemoval = true: delete tasks that are removed from this list.
     *
     * fetch = LAZY: tasks are NOT loaded until accessed (critical for performance).
     * Always use LAZY for collections.
     *
     * @Builder.Default: Lombok requires explicit default for collections.
     */
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Task> tasks = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * JPA lifecycle callback — runs automatically before UPDATE.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
