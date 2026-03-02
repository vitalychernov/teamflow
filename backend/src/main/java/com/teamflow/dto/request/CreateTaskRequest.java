package com.teamflow.dto.request;

import com.teamflow.entity.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for POST /api/projects/{projectId}/tasks.
 */
@Data
public class CreateTaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(min = 2, max = 300, message = "Title must be between 2 and 300 characters")
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    // Priority is optional — defaults to MEDIUM in the entity
    // If client sends null, service layer uses the entity default
    private TaskPriority priority;

    // Assignee is optional — task can be unassigned
    private Long assigneeId;
}
