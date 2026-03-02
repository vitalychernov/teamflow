package com.teamflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Full project response — used for GET /api/projects/{id}.
 * Contains nested owner info (UserResponse).
 *
 * Why not embed the full list of tasks here?
 * Tasks are fetched separately via GET /api/projects/{id}/tasks
 * with pagination. Embedding all tasks in the project response
 * would cause performance issues for projects with many tasks.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {

    private Long id;
    private String name;
    private String description;
    // Nested owner — only public fields (no password)
    private UserResponse owner;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
