package com.timetable.problem_solver;

import com.timetable.problem_solver.model.ConstraintSettings;
import com.timetable.problem_solver.repository.ConstraintSettingsRepository;
import com.timetable.problem_solver.service.SolverService;
import com.timetable.problem_solver.validation.ConstraintValidationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to verify dynamic constraint creation and management
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DynamicConstraintTest {

    @Autowired
    private ConstraintSettingsRepository constraintSettingsRepository;

    @Autowired
    private ConstraintValidationService validationService;

    @Autowired
    private SolverService solverService;

    @Test
    void testCreateCustomConstraint() {
        // Create a custom constraint
        ConstraintSettings customConstraint = new ConstraintSettings();
        customConstraint.setConstraintName("customMorningPreference");
        customConstraint.setConstraintWeight(5);
        customConstraint.setConstraintType("Soft");
        customConstraint.setEnableFlag(true);
        customConstraint.setDescription("Custom constraint to prefer morning classes");

        // Validate the constraint
        ConstraintValidationService.ValidationResult validation = validationService.validateConstraint(customConstraint);
        assertTrue(validation.isValid(), "Custom constraint should be valid");

        // Save the constraint
        ConstraintSettings saved = constraintSettingsRepository.save(customConstraint);
        assertNotNull(saved.getId());
        assertEquals("customMorningPreference", saved.getConstraintName());

        // Verify it can be retrieved
        var found = constraintSettingsRepository.findByConstraintName("customMorningPreference");
        assertTrue(found.isPresent());
        assertEquals("Soft", found.get().getConstraintType());
    }

    @Test
    void testValidationRules() {
        // Test invalid constraint name
        ConstraintSettings invalidConstraint = new ConstraintSettings();
        invalidConstraint.setConstraintName("123invalid"); // Starts with number
        invalidConstraint.setConstraintWeight(1);
        invalidConstraint.setConstraintType("Hard");

        ConstraintValidationService.ValidationResult validation = validationService.validateConstraint(invalidConstraint);
        assertFalse(validation.isValid());
        assertTrue(validation.getErrorMessage().contains("start with a letter"));

        // Test reserved name
        ConstraintSettings reservedConstraint = new ConstraintSettings();
        reservedConstraint.setConstraintName("teacherConflict"); // Reserved name
        reservedConstraint.setConstraintWeight(1);
        reservedConstraint.setConstraintType("Hard");

        validation = validationService.validateConstraint(reservedConstraint);
        assertFalse(validation.isValid());
        assertTrue(validation.getErrorMessage().contains("reserved"));

        // Test invalid weight
        ConstraintSettings weightConstraint = new ConstraintSettings();
        weightConstraint.setConstraintName("validName");
        weightConstraint.setConstraintWeight(0); // Invalid weight
        weightConstraint.setConstraintType("Hard");

        validation = validationService.validateConstraint(weightConstraint);
        assertFalse(validation.isValid());
        assertTrue(validation.getErrorMessage().contains("weight must be 1 or greater"));
    }

    @Test
    void testSolverReloadWithCustomConstraints() {
        // Verify solver service can reload with custom constraints
        assertDoesNotThrow(() -> {
            solverService.reloadSolver();
        }, "Solver should reload successfully with custom constraints");

        // Verify solver info is available
        String solverInfo = solverService.getSolverInfo();
        assertNotNull(solverInfo);
        assertTrue(solverInfo.contains("TimeTableConstraintProvider"));
    }
}

