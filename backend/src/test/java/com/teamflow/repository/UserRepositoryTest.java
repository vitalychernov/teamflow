package com.teamflow.repository;

import com.teamflow.entity.Role;
import com.teamflow.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * We test only OUR custom methods (findByEmail, existsByEmail).
 * We do NOT test save(), findById() etc. — those come from Spring Data
 * and are already tested by the framework.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Use TestEntityManager to persist test data directly,
        // bypassing the repository we are testing.
        // persistAndFlush() saves to DB and flushes the session
        // so the data is visible in queries within the same transaction.
        testUser = entityManager.persistAndFlush(User.builder()
                .name("John Doe")
                .email("john@example.com")
                .password("hashed-password-123")
                .role(Role.USER)
                .build());
    }

    @Test
    @DisplayName("findByEmail: should return user when email exists")
    void findByEmail_whenEmailExists_returnsUser() {
        Optional<User> result = userRepository.findByEmail("john@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("john@example.com");
        assertThat(result.get().getName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("findByEmail: should return empty Optional when email not found")
    void findByEmail_whenEmailNotFound_returnsEmpty() {
        Optional<User> result = userRepository.findByEmail("nonexistent@example.com");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByEmail: should be case-sensitive")
    void findByEmail_isCaseSensitive() {
        // Emails are stored as-is. UPPER-CASE should NOT match.
        // This documents intentional behavior — normalization
        // happens in the service layer before saving.
        Optional<User> result = userRepository.findByEmail("JOHN@EXAMPLE.COM");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("existsByEmail: should return true when email exists")
    void existsByEmail_whenEmailExists_returnsTrue() {
        boolean exists = userRepository.existsByEmail("john@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByEmail: should return false when email not found")
    void existsByEmail_whenEmailNotFound_returnsFalse() {
        boolean exists = userRepository.existsByEmail("nobody@example.com");

        assertThat(exists).isFalse();
    }
}
