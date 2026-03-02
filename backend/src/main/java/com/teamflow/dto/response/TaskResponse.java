package com.teamflow.dto.response;

import com.teamflow.entity.TaskPriority;
import com.teamflow.entity.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Full task response — used for all task endpoints.
 * Contains nested assignee info (nullable).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    // Project ID only — not the full project object (avoid circular nesting)
    private Long projectId;
    // Assignee is nullable — task may be unassigned
    private UserResponse assignee;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
