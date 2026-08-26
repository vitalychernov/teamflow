package com.teamflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Why 'users' and not 'user'?
 * 'user' is a reserved word in PostgreSQL — it would require
 * quoting everywhere. 'users' is a common convention.
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

    @Column(nullable = false, length = 100)
    private String name;

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

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
