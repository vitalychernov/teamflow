package com.teamflow.entity;

/**
 * User roles for role-based access control (RBAC).
 *
 * Spring Security expects role names prefixed with "ROLE_"
 * when using hasRole(). We store the raw name (USER, ADMIN)
 * and Spring adds the prefix automatically.
 *
 * Alternative: store roles in a separate 'roles' table (ManyToMany).
 * For this project a single enum field is sufficient — we only
 * have two roles and they don't change at runtime.
 */
public enum Role {
    USER,
    ADMIN
}
