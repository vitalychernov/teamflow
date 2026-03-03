package com.teamflow.repository;

import com.teamflow.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for Project entity.
 *
 * Pagination via Pageable:
 * The Pageable parameter tells Spring Data to generate
 * LIMIT + OFFSET SQL automatically. Returns Page<T> which
 * contains the data + total count + page metadata.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * Returns a paginated list of all projects owned by a specific user.
     *
     * Generated SQL (approximately):
     * SELECT * FROM projects WHERE owner_id = ? LIMIT ? OFFSET ?
     *
     * The 'owner_id' column name comes from @JoinColumn(name = "owner_id")
     * on the Project entity. Spring Data resolves 'owner.id' via the
     * entity relationship automatically.
     */
    Page<Project> findByOwnerId(Long ownerId, Pageable pageable);

    /**
     * Checks if a project belongs to a specific user.
     * Used for authorization: only owner or ADMIN can edit/delete.
     */
    boolean existsByIdAndOwnerId(Long projectId, Long ownerId);

    /**
     * Search projects by name (case-insensitive) for a specific owner.
     *
     * LOWER() + LIKE '%keyword%' — simple text search.
     * Alternative for production: PostgreSQL full-text search with tsvector.
     *
     * @Query uses JPQL (Java Persistence Query Language), not SQL.
     * JPQL operates on entity fields (p.name), not column names.
     */
    @Query("SELECT p FROM Project p WHERE p.owner.id = :ownerId " +
           "AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Project> searchByOwnerAndName(@Param("ownerId") Long ownerId,
                                       @Param("keyword") String keyword,
                                       Pageable pageable);

    /**
     * Search projects by name across all owners (shared workspace).
     */
    @Query("SELECT p FROM Project p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Project> searchByName(@Param("keyword") String keyword, Pageable pageable);
}
