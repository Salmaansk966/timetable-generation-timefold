package com.timetable.problem_solver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Simple integration test to verify the application starts correctly
 * with the dynamic constraint system
 */
@SpringBootTest
@ActiveProfiles("test")
class IntegrationTest {

    @Test
    void contextLoads() {
        // This test will pass if the Spring context loads successfully
        // which means all our beans are properly configured
    }
}
