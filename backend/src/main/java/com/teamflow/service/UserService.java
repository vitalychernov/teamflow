package com.teamflow.service;

import com.teamflow.dto.response.PageResponse;
import com.teamflow.dto.response.UserResponse;
import com.teamflow.mapper.UserMapper;
import com.teamflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for user management operations.
 * Currently used by AdminController only.
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
}
