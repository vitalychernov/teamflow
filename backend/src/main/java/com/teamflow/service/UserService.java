package com.teamflow.service;

import com.teamflow.dto.response.PageResponse;
import com.teamflow.dto.response.UserResponse;
import com.teamflow.entity.Role;
import com.teamflow.mapper.UserMapper;
import com.teamflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for user management operations.
 * Used by AdminController (paginated) and UserController (full list for dropdowns).
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getAllUsers(Pageable pageable) {
        return PageResponse.from(
                userRepository.findAll(pageable).map(userMapper::toResponse)
        );
    }

    /** Returns all non-admin users sorted by name — used for assignee dropdown. */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsersList() {
        return userRepository.findByRoleNot(Role.ADMIN, Sort.by("name"))
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }
}
