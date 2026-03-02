package com.teamflow.controller;

import com.teamflow.dto.response.PageResponse;
import com.teamflow.dto.response.UserResponse;
import com.teamflow.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin-only endpoints.
 * Access is restricted to ROLE_ADMIN in SecurityConfig:
 *   .requestMatchers("/api/admin/**").hasRole("ADMIN")
 *
 * Spring Security enforces this at the filter level —
 * no @PreAuthorize needed here (but could be added for extra safety).
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin-only endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    @Operation(summary = "Get all users (ADMIN only)")
    public ResponseEntity<PageResponse<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                userService.getAllUsers(PageRequest.of(page, size, Sort.by("createdAt").descending()))
        );
    }
}
