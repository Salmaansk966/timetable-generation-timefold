package com.timetable.problem_solver;

import com.timetable.problem_solver.model.TimeFoldTimetable;
import com.timetable.problem_solver.service.SolverService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify the solver null pointer exception fix
 */
@SpringBootTest
@ActiveProfiles("test")
class SolverFixTest {

    @Autowired
    private SolverService solverService;

    @Test
    void testSolverInitialization() {
        // Test that solver is properly initialized
        assertTrue(solverService.isSolverInitialized(), "Solver should be initialized");
        
        // Test that we can get the solver manager
        assertNotNull(solverService.getSolverManager(), "Solver manager should not be null");
        
        // Test solver info
        String solverInfo = solverService.getSolverInfo();
        assertNotNull(solverInfo, "Solver info should not be null");
        assertTrue(solverInfo.contains("TimeTableConstraintProvider"), "Solver info should contain constraint provider info");
        
        System.out.println("Solver info: " + solverInfo);
    }

    @Test
    void testSolverReload() {
        // Test that solver can be reloaded without errors
        assertDoesNotThrow(() -> {
            solverService.reloadSolver();
        }, "Solver reload should not throw exceptions");
        
        // Verify solver is still initialized after reload
        assertTrue(solverService.isSolverInitialized(), "Solver should still be initialized after reload");
        assertNotNull(solverService.getSolverManager(), "Solver manager should not be null after reload");
    }
}

