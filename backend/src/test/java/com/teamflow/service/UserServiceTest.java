package com.teamflow.service;

import com.teamflow.dto.response.PageResponse;
import com.teamflow.dto.response.UserResponse;
import com.teamflow.entity.Role;
import com.teamflow.entity.User;
import com.teamflow.mapper.UserMapper;
import com.teamflow.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/** Unit tests for UserService. */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private final User alice = User.builder()
            .id(1L).name("Alice").email("alice@example.com").role(Role.USER).build();

    private final User bob = User.builder()
            .id(2L).name("Bob").email("bob@example.com").role(Role.USER).build();

    private final UserResponse aliceResponse = UserResponse.builder()
            .id(1L).name("Alice").email("alice@example.com").role("USER").build();

    private final UserResponse bobResponse = UserResponse.builder()
            .id(2L).name("Bob").email("bob@example.com").role("USER").build();

    // ─────────────────────────────────────────
    // getAllUsers() — paginated
    // ─────────────────────────────────────────

    @Test
    @DisplayName("getAllUsers: returns PageResponse with mapped users")
    void getAllUsers_returnsPageResponse() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<User> page = new PageImpl<>(List.of(alice, bob), pageable, 2);

        given(userRepository.findAll(pageable)).willReturn(page);
        given(userMapper.toResponse(alice)).willReturn(aliceResponse);
        given(userMapper.toResponse(bob)).willReturn(bobResponse);

        PageResponse<UserResponse> result = userService.getAllUsers(pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    @DisplayName("getAllUsers: delegates to userRepository.findAll(pageable)")
    void getAllUsers_delegatesToRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of());

        given(userRepository.findAll(pageable)).willReturn(page);

        userService.getAllUsers(pageable);

        verify(userRepository).findAll(pageable);
    }

    // ─────────────────────────────────────────
    // getAllUsersList() — dropdown
    // ─────────────────────────────────────────

    @Test
    @DisplayName("getAllUsersList: returns list of non-admin users")
    void getAllUsersList_returnsNonAdminUsers() {
        given(userRepository.findByRoleNot(eq(Role.ADMIN), any(Sort.class)))
                .willReturn(List.of(alice, bob));
        given(userMapper.toResponse(alice)).willReturn(aliceResponse);
        given(userMapper.toResponse(bob)).willReturn(bobResponse);

        List<UserResponse> result = userService.getAllUsersList();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(UserResponse::getRole).containsOnly("USER");
    }

    @Test
    @DisplayName("getAllUsersList: calls findByRoleNot with ADMIN and sort by name")
    void getAllUsersList_callsFindByRoleNotAdmin() {
        given(userRepository.findByRoleNot(eq(Role.ADMIN), any(Sort.class)))
                .willReturn(List.of());

        userService.getAllUsersList();

        verify(userRepository).findByRoleNot(eq(Role.ADMIN), eq(Sort.by("name")));
    }
}
