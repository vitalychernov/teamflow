package com.teamflow.repository;

import com.teamflow.entity.Role;
import com.teamflow.entity.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** Used during login: look up user by email to verify credentials. */
    Optional<User> findByEmail(String email);

    /**
     * Used during registration: check if email is already taken
     * before trying to insert (avoids catching unique constraint violations).
     */
    boolean existsByEmail(String email);

    /** Used for assignee dropdown (excludes ADMIN). */
    List<User> findByRoleNot(Role role, Sort sort);
}
