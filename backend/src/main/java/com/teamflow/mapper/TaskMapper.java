package com.teamflow.mapper;

import com.teamflow.dto.response.TaskResponse;
import com.teamflow.entity.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Maps Task entity to TaskResponse DTO.
 * Depends on UserMapper for the optional assignee field.
 */
@Component
@RequiredArgsConstructor
public class TaskMapper {

    private final UserMapper userMapper;

    public TaskResponse toResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                // We expose only projectId, not the full project object
                // This avoids circular nesting: Task → Project → Tasks → Task...
                .projectId(task.getProject().getId())
                // userMapper.toResponse handles null gracefully (returns null)
                .assignee(userMapper.toResponse(task.getAssignee()))
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
