package com.teamflow.mapper;

import com.teamflow.dto.response.UserResponse;
import com.teamflow.entity.User;
import org.springframework.stereotype.Component;

/**
 * Maps User entity to UserResponse DTO.
 *
 * Why @Component and not static methods?
 * - Spring can inject UserMapper where needed (@Autowired / constructor injection)
 * - Easier to mock in unit tests
 * - Consistent with how ProjectMapper and TaskMapper work
 *
 * Alternative: MapStruct — generates mapper implementations at compile time.
 * Faster at runtime, less code to write, but adds build complexity.
 * For learning purposes, manual mappers are more transparent.
 */
@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        if (user == null) return null;

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())  // enum → String
                .createdAt(user.getCreatedAt())
                .build();
    }
}
