package com.timetable.problem_solver.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.timetable.problem_solver.debug.ConstraintDebugService;
import com.timetable.problem_solver.model.ConstraintSettings;
import com.timetable.problem_solver.repository.ConstraintSettingsRepository;
import com.timetable.problem_solver.service.SolverService;
import com.timetable.problem_solver.validation.ConstraintValidationService;

/**
 * Controller for managing constraint settings.
 * Automatically triggers solver reload when constraints are updated to ensure
 * the new constraint configuration takes effect immediately.
 */
@RequestMapping("/api/constraint")
@RestController
public class ConstraintController {

    private static final Logger logger = LoggerFactory.getLogger(ConstraintController.class);

    private final ConstraintSettingsRepository constraintSettingsRepository;
    private final SolverService solverService;
    private final ConstraintValidationService validationService;
    private final ConstraintDebugService debugService;

    @Autowired
    public ConstraintController(ConstraintSettingsRepository repository, SolverService solverService, ConstraintValidationService validationService, ConstraintDebugService debugService) {
        this.constraintSettingsRepository = repository;
        this.solverService = solverService;
        this.validationService = validationService;
        this.debugService = debugService;
    }

    public record ConstraintRecord(Long id, String constraintName, int constraintWeight, String constraintType, boolean enableFlag, String description){}
    public record ToggleRequest(boolean enabled) {}
    public record CreateConstraintRequest(String constraintName, int constraintWeight, String constraintType, boolean enableFlag, String description) {}

    /**
     * Get all constraint settings from the database.
     * 
     * @return List of all constraint settings
     */
    @GetMapping
    public ResponseEntity<List<ConstraintRecord>> getConstraints() {
        try {
            List<ConstraintRecord> constraints = constraintSettingsRepository
                    .findAll().stream()
                    .map(constraintSettings ->
                            new ConstraintRecord(
                                    constraintSettings.getId(),
                                constraintSettings.getConstraintName(),
                                constraintSettings.getConstraintWeight(),
                                constraintSettings.getConstraintType(),
                                constraintSettings.isEnableFlag(),
                                    constraintSettings.getDescription()))
                    .toList();
            
            return ResponseEntity.ok(constraints);
            
        } catch (Exception e) {
            logger.error("Failed to get constraints: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Toggle a constraint's enabled state and automatically reload the solver.
     * This ensures that constraint changes take effect immediately for the next solve operation.
     * 
     * @param id The constraint ID to toggle
     * @param request The toggle request containing the new enabled state
     * @return The updated constraint record
     */
    @PutMapping("/{id}/toggle")
    public ResponseEntity<ConstraintRecord> toggleConstraint(@PathVariable Long id, @RequestBody ToggleRequest request) {
        try {
            logger.info("Toggling constraint {} to enabled: {}", id, request.enabled());
            
            // Find and update the constraint
        var entity = constraintSettingsRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Constraint not found with id: " + id));
            
            boolean previousState = entity.isEnableFlag();
        entity.setEnableFlag(request.enabled());
        var saved = constraintSettingsRepository.save(entity);
            
            logger.info("Constraint '{}' toggled from {} to {}", 
                    saved.getConstraintName(), previousState, request.enabled());
            
            // Automatically reload the solver to apply the new constraint configuration
            try {
                solverService.reloadSolver();
                logger.info("Solver reloaded successfully after constraint toggle");
            } catch (Exception e) {
                logger.error("Failed to reload solver after constraint toggle: {}", e.getMessage(), e);
                // Don't fail the request if solver reload fails, but log the error
            }
            
            ConstraintRecord result = new ConstraintRecord(
                    saved.getId(),
                    saved.getConstraintName(),
                    saved.getConstraintWeight(),
                    saved.getConstraintType(),
                    saved.isEnableFlag(),
                    saved.getDescription());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Failed to toggle constraint {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update constraint weight and automatically reload the solver.
     * 
     * @param id The constraint ID to update
     * @param weight The new weight value
     * @return The updated constraint record
     */
    @PutMapping("/{id}/weight")
    public ResponseEntity<ConstraintRecord> updateConstraintWeight(@PathVariable Long id, @RequestBody Integer weight) {
        try {
            logger.info("Updating constraint {} weight to {}", id, weight);
            
            var entity = constraintSettingsRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Constraint not found with id: " + id));
            
            entity.setConstraintWeight(weight);
            var saved = constraintSettingsRepository.save(entity);
            
            // Reload solver to apply new weight
            try {
                solverService.reloadSolver();
                logger.info("Solver reloaded successfully after weight update");
            } catch (Exception e) {
                logger.error("Failed to reload solver after weight update: {}", e.getMessage(), e);
            }
            
            ConstraintRecord result = new ConstraintRecord(
                    saved.getId(),
                    saved.getConstraintName(),
                    saved.getConstraintWeight(),
                    saved.getConstraintType(),
                    saved.isEnableFlag(),
                    saved.getDescription());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Failed to update constraint weight {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Create a new constraint dynamically.
     * This allows users to add custom constraints through the UI.
     * 
     * @param request The constraint creation request
     * @return The created constraint record
     */
    @PostMapping
    public ResponseEntity<ConstraintRecord> createConstraint(@RequestBody CreateConstraintRequest request) {
        try {
            logger.info("Creating new constraint: {}", request.constraintName());
            
            // Create constraint object for validation
            ConstraintSettings newConstraint = new ConstraintSettings();
            newConstraint.setConstraintName(request.constraintName());
            newConstraint.setConstraintWeight(request.constraintWeight());
            newConstraint.setConstraintType(request.constraintType());
            newConstraint.setEnableFlag(request.enableFlag());
            newConstraint.setDescription(request.description());
            
            // Validate constraint
            ConstraintValidationService.ValidationResult validation = validationService.validateConstraint(newConstraint);
            if (!validation.isValid()) {
                logger.warn("Constraint validation failed: {}", validation.getErrorMessage());
                return ResponseEntity.badRequest()
                        .body(null); // You might want to return error details
            }
            
            // Validate constraint name is unique
            if (constraintSettingsRepository.findByConstraintName(request.constraintName()).isPresent()) {
                return ResponseEntity.badRequest().build();
            }
            
            var saved = constraintSettingsRepository.save(newConstraint);
            
            logger.info("Created new constraint: {} with ID: {}", saved.getConstraintName(), saved.getId());
            
            // Reload solver to include the new constraint
            try {
                solverService.reloadSolver();
                logger.info("Solver reloaded successfully after creating new constraint");
            } catch (Exception e) {
                logger.error("Failed to reload solver after creating constraint: {}", e.getMessage(), e);
                // Don't fail the request if solver reload fails
            }
            
            ConstraintRecord result = new ConstraintRecord(
                    saved.getId(),
                    saved.getConstraintName(),
                    saved.getConstraintWeight(),
                    saved.getConstraintType(),
                    saved.isEnableFlag(),
                    saved.getDescription());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Failed to create constraint: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete a constraint by ID.
     * This allows users to remove custom constraints.
     * 
     * @param id The constraint ID to delete
     * @return Success response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteConstraint(@PathVariable Long id) {
        try {
            logger.info("Deleting constraint with ID: {}", id);
            
            // Check if constraint exists
            var constraint = constraintSettingsRepository.findById(id);
            if (constraint.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            String constraintName = constraint.get().getConstraintName();
            
            // Don't allow deletion of core system constraints
            if (isCoreConstraint(constraintName)) {
                logger.warn("Attempted to delete core constraint: {}", constraintName);
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Cannot delete core system constraints"));
            }
            
            // Delete the constraint
            constraintSettingsRepository.deleteById(id);
            
            logger.info("Deleted constraint: {} with ID: {}", constraintName, id);
            
            // Reload solver to remove the constraint
            try {
                solverService.reloadSolver();
                logger.info("Solver reloaded successfully after deleting constraint");
            } catch (Exception e) {
                logger.error("Failed to reload solver after deleting constraint: {}", e.getMessage(), e);
                // Don't fail the request if solver reload fails
            }
            
            return ResponseEntity.ok(Map.of(
                    "message", "Constraint deleted successfully",
                    "constraintName", constraintName
            ));
            
        } catch (Exception e) {
            logger.error("Failed to delete constraint {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to delete constraint: " + e.getMessage()));
        }
    }

    /**
     * Update constraint details (name, weight, type, description).
     * 
     * @param id The constraint ID to update
     * @param request The update request
     * @return The updated constraint record
     */
    @PutMapping("/{id}")
    public ResponseEntity<ConstraintRecord> updateConstraint(@PathVariable Long id, @RequestBody CreateConstraintRequest request) {
        try {
            logger.info("Updating constraint {}: {}", id, request.constraintName());
            
            var entity = constraintSettingsRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Constraint not found with id: " + id));
            
            // Check if name is being changed and if new name is unique
            if (!entity.getConstraintName().equals(request.constraintName())) {
                if (constraintSettingsRepository.findByConstraintName(request.constraintName()).isPresent()) {
                    return ResponseEntity.badRequest().build();
                }
            }
            
            // Update constraint details
            entity.setConstraintName(request.constraintName());
            entity.setConstraintWeight(request.constraintWeight());
            entity.setConstraintType(request.constraintType());
            entity.setEnableFlag(request.enableFlag());
            entity.setDescription(request.description());
            
            var saved = constraintSettingsRepository.save(entity);
            
            logger.info("Updated constraint: {} with ID: {}", saved.getConstraintName(), saved.getId());
            
            // Reload solver to apply changes
            try {
                solverService.reloadSolver();
                logger.info("Solver reloaded successfully after updating constraint");
            } catch (Exception e) {
                logger.error("Failed to reload solver after updating constraint: {}", e.getMessage(), e);
            }
            
            ConstraintRecord result = new ConstraintRecord(
                    saved.getId(),
                saved.getConstraintName(),
                saved.getConstraintWeight(),
                saved.getConstraintType(),
                saved.isEnableFlag(),
                saved.getDescription());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Failed to update constraint {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Check if a constraint is a core system constraint that cannot be deleted.
     */
    private boolean isCoreConstraint(String constraintName) {
        List<String> coreConstraints = List.of(
                "teacherConflict", "studentGroupConflict", "noFreeTimeslots",
                "teacherDailyWorkloadLimit", "noBackToBackSameSubject",
                "teacherNoThreeConsecutive", "practicalSubjectConsecutivePeriods",
                "teacherTimeEfficiency", "practicalSubjectsWeekdaysOnly",
                "onePracticalPerDayPerSection", "practicalSubjectTwoDaysPerWeek",
                "lowDifficultySubjectWeeklyLimit", "lowDifficultyOncePerDay",
                "preferHighPriorityTheory"
        );
        return coreConstraints.contains(constraintName);
    }

    /**
     * Debug endpoint to check constraint states in database.
     * This helps diagnose why disabled constraints might still be active.
     */
    @GetMapping("/debug")
    public ResponseEntity<Map<String, Object>> debugConstraints() {
        try {
            logger.info("Debug endpoint called - checking constraint states");
            
            // Log detailed constraint info
            debugService.logAllConstraints();
            
            Map<String, Object> debugInfo = Map.of(
                "enabledConstraints", debugService.getEnabledConstraintNames(),
                "disabledConstraints", debugService.getDisabledConstraintNames(),
                "totalConstraints", constraintSettingsRepository.count(),
                "solverInfo", solverService.getSolverInfo()
            );
            
            return ResponseEntity.ok(debugInfo);
            
        } catch (Exception e) {
            logger.error("Failed to get debug info: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get debug info: " + e.getMessage()));
        }
    }
}
