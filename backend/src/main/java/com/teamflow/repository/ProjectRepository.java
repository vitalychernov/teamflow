package com.teamflow.repository;

import com.teamflow.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    Page<Project> findByOwnerId(Long ownerId, Pageable pageable);

    /**
     * Checks if a project belongs to a specific user.
     * Used for authorization: only owner or ADMIN can edit/delete.
     */
    boolean existsByIdAndOwnerId(Long projectId, Long ownerId);

    /**
     * LOWER() + LIKE '%keyword%' — simple text search.
     * Alternative for production: PostgreSQL full-text search with tsvector.
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
