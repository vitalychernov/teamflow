package com.teamflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response for POST /api/auth/login and /api/auth/register.
 * Contains the JWT token and basic user info so the frontend
 * doesn't need a separate /me request after login.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private Long id;
    private String token;
    private String email;
    private String name;
    // Role as String so the frontend doesn't need to know the enum
    private String role;
}
