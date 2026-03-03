package com.teamflow.mapper;

import com.teamflow.dto.response.ProjectResponse;
import com.teamflow.entity.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Maps Project entity to ProjectResponse DTO. */
@Component
@RequiredArgsConstructor
public class ProjectMapper {

    private final UserMapper userMapper;

    public ProjectResponse toResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .owner(userMapper.toResponse(project.getOwner()))
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}
