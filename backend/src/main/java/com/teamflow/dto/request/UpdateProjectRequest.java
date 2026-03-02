package com.teamflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for PUT /api/projects/{id}.
 *
 * Why a separate UpdateRequest instead of reusing CreateRequest?
 * - Update may have different required fields (e.g., name could be optional on update)
 * - Keeps validation rules separate and explicit
 * - Easier to evolve independently (add 'status' to project later, etc.)
 */
@Data
public class UpdateProjectRequest {

    @NotBlank(message = "Project name is required")
    @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
    private String name;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;
}
