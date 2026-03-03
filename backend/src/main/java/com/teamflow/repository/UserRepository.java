package com.teamflow.repository;

import com.teamflow.entity.Role;
import com.teamflow.entity.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity.
 *
 * JpaRepository<User, Long> provides out of the box:
 * - save(), findById(), findAll(), deleteById(), count(), existsById(), etc.
 * Spring Data generates the SQL implementation at startup — no boilerplate needed.
 *
 * Method naming convention:
 * findBy{FieldName} → SELECT * FROM users WHERE field_name = ?
 * existsBy{FieldName} → SELECT COUNT(*) > 0 FROM users WHERE field_name = ?
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Used during login: look up user by email to verify credentials.
     * Returns Optional to force callers to handle the "not found" case explicitly.
     */
    Optional<User> findByEmail(String email);

    /**
     * Used during registration: check if email is already taken
     * before trying to insert (avoids catching unique constraint violations).
     */
    boolean existsByEmail(String email);

    /** Returns all users with a given role, sorted. Used for assignee dropdown (excludes ADMIN). */
    List<User> findByRoleNot(Role role, Sort sort);
}
