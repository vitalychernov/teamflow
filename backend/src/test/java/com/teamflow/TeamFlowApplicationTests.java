package com.teamflow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test — verifies that the Spring application context
 * loads without errors. Uses 'test' profile with H2 in-memory DB
 * so no real PostgreSQL is needed during CI.
 */
@SpringBootTest
@ActiveProfiles("test")
class TeamFlowApplicationTests {

    @Test
    void contextLoads() {
        // If Spring context starts without exceptions, the test passes.
    }
}
