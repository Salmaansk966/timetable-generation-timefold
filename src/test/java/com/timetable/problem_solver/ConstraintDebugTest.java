package com.timetable.problem_solver;

import com.timetable.problem_solver.debug.ConstraintDebugService;
import com.timetable.problem_solver.model.ConstraintSettings;
import com.timetable.problem_solver.repository.ConstraintSettingsRepository;
import com.timetable.problem_solver.service.SolverService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to debug constraint loading issues
 */
@SpringBootTest
@ActiveProfiles("test")
class ConstraintDebugTest {

    @Autowired
    private ConstraintSettingsRepository constraintSettingsRepository;

    @Autowired
    private ConstraintDebugService debugService;

    @Autowired
    private SolverService solverService;

    @Test
    void debugConstraintStates() {
        System.out.println("=== DEBUGGING CONSTRAINT STATES ===");
        
        // Get all constraints from database
        List<ConstraintSettings> allConstraints = constraintSettingsRepository.findAll();
        System.out.println("Total constraints in database: " + allConstraints.size());
        
        // Check each constraint
        for (ConstraintSettings constraint : allConstraints) {
            System.out.println("Constraint: " + constraint.getConstraintName() + 
                             " - Enabled: " + constraint.isEnableFlag() + 
                             " - Type: " + constraint.getConstraintType() + 
                             " - Weight: " + constraint.getConstraintWeight());
        }
        
        // Get enabled and disabled lists
        List<String> enabledConstraints = debugService.getEnabledConstraintNames();
        List<String> disabledConstraints = debugService.getDisabledConstraintNames();
        
        System.out.println("\nEnabled constraints (" + enabledConstraints.size() + "):");
        enabledConstraints.forEach(name -> System.out.println("  ✓ " + name));
        
        System.out.println("\nDisabled constraints (" + disabledConstraints.size() + "):");
        disabledConstraints.forEach(name -> System.out.println("  ✗ " + name));
        
        // Test solver info
        String solverInfo = solverService.getSolverInfo();
        System.out.println("\nSolver info: " + solverInfo);
        
        // Force solver reload to see debug output
        System.out.println("\n=== FORCING SOLVER RELOAD ===");
        solverService.reloadSolver();
        
        System.out.println("=== DEBUG COMPLETE ===");
    }
}

