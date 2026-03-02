package com.teamflow.dto.request;

import com.teamflow.entity.TaskPriority;
import com.teamflow.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for PUT /api/tasks/{id}.
 * Unlike CreateTaskRequest, allows updating status.
 */
@Data
public class UpdateTaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(min = 2, max = 300, message = "Title must be between 2 and 300 characters")
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    // Status CAN be updated (move task through workflow)
    private TaskStatus status;

    private TaskPriority priority;

    // null means "unassign the task"
    private Long assigneeId;
}
