package com.teamflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User entity — maps to the 'users' table in PostgreSQL.
 *
 * Why 'users' and not 'user'?
 * 'user' is a reserved word in PostgreSQL — it would require
 * quoting everywhere. 'users' is a common convention.
 *
 * Lombok annotations used:
 * - @Data: generates getters, setters, equals, hashCode, toString
 * - @Builder: enables builder pattern (User.builder().name("...").build())
 * - @NoArgsConstructor: required by JPA (must have no-arg constructor)
 * - @AllArgsConstructor: required by @Builder when @NoArgsConstructor is present
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * nullable = false → NOT NULL constraint in DB.
     * length = 100 → VARCHAR(100).
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * unique = true → UNIQUE constraint in DB.
     * We look up users by email during login.
     */
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /**
     * Stored as BCrypt hash (60 chars), never plain text.
     * We set length = 255 for safety.
     */
    @Column(nullable = false, length = 255)
    private String password;

    /**
     * @Enumerated(STRING): stores "USER" or "ADMIN" as a string in DB.
     * Alternative: EnumType.ORDINAL stores 0 or 1 (integer).
     * STRING is preferred — safe if enum order changes.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.USER;

    /**
     * @Column(updatable = false): Hibernate never updates this field.
     * Set once on insert via @PrePersist lifecycle callback.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * JPA lifecycle callback — runs automatically before INSERT.
     * This avoids needing a separate audit framework for simple cases.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
