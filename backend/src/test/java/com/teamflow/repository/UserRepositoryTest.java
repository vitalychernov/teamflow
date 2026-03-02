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
 * Unit tests for UserRepository.
 *
 * @DataJpaTest:
 * - Loads only JPA beans (fast — no web layer, no security, no services)
 * - Replaces PostgreSQL with H2 in-memory database automatically
 * - Each @Test runs in a transaction that ROLLS BACK after the test
 *   → test data is never persisted between tests
 *
 * We test only OUR custom methods (findByEmail, existsByEmail).
 * We do NOT test save(), findById() etc. — those come from Spring Data
 * and are already tested by the framework.
 *
 * AssertJ (assertThat) is preferred over JUnit assertEquals:
 * - More readable: assertThat(result).isPresent().hasValueSatisfying(...)
 * - Better error messages on failure
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    // Test data — created fresh before each test
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

    // ─────────────────────────────────────────
    // findByEmail tests
    // ─────────────────────────────────────────

    @Test
    @DisplayName("findByEmail: should return user when email exists")
    void findByEmail_whenEmailExists_returnsUser() {
        Optional<User> result = userRepository.findByEmail("john@example.com");

        // isPresent() checks Optional is not empty
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("john@example.com");
        assertThat(result.get().getName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("findByEmail: should return empty Optional when email not found")
    void findByEmail_whenEmailNotFound_returnsEmpty() {
        Optional<User> result = userRepository.findByEmail("nonexistent@example.com");

        // isEmpty() checks Optional IS empty
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

    // ─────────────────────────────────────────
    // existsByEmail tests
    // ─────────────────────────────────────────

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
