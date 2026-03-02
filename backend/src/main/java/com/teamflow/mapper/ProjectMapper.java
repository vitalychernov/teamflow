package com.teamflow.mapper;

import com.teamflow.dto.response.ProjectResponse;
import com.teamflow.entity.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Maps Project entity to ProjectResponse DTO.
 * Depends on UserMapper to map the nested owner field.
 *
 * Constructor injection via @RequiredArgsConstructor (Lombok):
 * generates a constructor for all 'final' fields.
 * This is the recommended injection style — avoids @Autowired on fields,
 * makes dependencies explicit, easier to test.
 */
@Component
@RequiredArgsConstructor
public class ProjectMapper {

    private final UserMapper userMapper;

    public ProjectResponse toResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                // Delegate owner mapping to UserMapper
                .owner(userMapper.toResponse(project.getOwner()))
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}
