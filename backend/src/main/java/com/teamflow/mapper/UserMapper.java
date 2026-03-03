package com.teamflow.mapper;

import com.teamflow.dto.response.UserResponse;
import com.teamflow.entity.User;
import org.springframework.stereotype.Component;

/** Maps User entity to UserResponse DTO. */
@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        if (user == null) return null;

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
