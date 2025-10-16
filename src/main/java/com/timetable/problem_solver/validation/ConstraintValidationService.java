package com.timetable.problem_solver.validation;

import com.timetable.problem_solver.model.ConstraintSettings;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Service for validating constraint creation and updates.
 * Ensures that custom constraints meet the required criteria.
 */
@Service
public class ConstraintValidationService {

    private static final Pattern CONSTRAINT_NAME_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*$");
    private static final List<String> RESERVED_NAMES = List.of(
            "teacherConflict", "studentGroupConflict", "noFreeTimeslots",
            "teacherDailyWorkloadLimit", "noBackToBackSameSubject",
            "teacherNoThreeConsecutive", "practicalSubjectConsecutivePeriods",
            "teacherTimeEfficiency", "practicalSubjectsWeekdaysOnly",
            "onePracticalPerDayPerSection", "practicalSubjectTwoDaysPerWeek",
            "lowDifficultySubjectWeeklyLimit", "lowDifficultyOncePerDay",
            "preferHighPriorityTheory"
    );

    /**
     * Validate constraint settings for creation or update.
     */
    public ValidationResult validateConstraint(ConstraintSettings constraint) {
        ValidationResult result = new ValidationResult();

        // Validate constraint name
        if (constraint.getConstraintName() == null || constraint.getConstraintName().trim().isEmpty()) {
            result.addError("Constraint name is required");
        } else {
            String name = constraint.getConstraintName().trim();
            
            // Check name format
            if (!CONSTRAINT_NAME_PATTERN.matcher(name).matches()) {
                result.addError("Constraint name must start with a letter and contain only letters, numbers, and underscores");
            }
            
            // Check name length
            if (name.length() > 50) {
                result.addError("Constraint name must be 50 characters or less");
            }
            
            // Check if name is reserved
            if (RESERVED_NAMES.contains(name)) {
                result.addError("Constraint name '" + name + "' is reserved for system constraints");
            }
        }

        // Validate constraint type
        if (constraint.getConstraintType() == null || constraint.getConstraintType().trim().isEmpty()) {
            result.addError("Constraint type is required");
        } else {
            String type = constraint.getConstraintType().trim();
            if (!"Hard".equalsIgnoreCase(type) && !"Soft".equalsIgnoreCase(type)) {
                result.addError("Constraint type must be 'Hard' or 'Soft'");
            }
        }

        // Validate constraint weight
        if (constraint.getConstraintWeight() < 1) {
            result.addError("Constraint weight must be 1 or greater");
        }
        if (constraint.getConstraintWeight() > 1000) {
            result.addError("Constraint weight must be 1000 or less");
        }

        // Validate description
        if (constraint.getDescription() != null && constraint.getDescription().length() > 500) {
            result.addError("Description must be 500 characters or less");
        }

        return result;
    }

    /**
     * Check if a constraint name is valid for creation.
     */
    public boolean isValidConstraintName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        String trimmedName = name.trim();
        return CONSTRAINT_NAME_PATTERN.matcher(trimmedName).matches() 
                && !RESERVED_NAMES.contains(trimmedName)
                && trimmedName.length() <= 50;
    }

    /**
     * Check if a constraint name is reserved for system use.
     */
    public boolean isReservedConstraintName(String name) {
        return RESERVED_NAMES.contains(name);
    }

    /**
     * Get list of reserved constraint names.
     */
    public List<String> getReservedConstraintNames() {
        return List.copyOf(RESERVED_NAMES);
    }

    /**
     * Validation result class to hold validation errors.
     */
    public static class ValidationResult {
        private final List<String> errors = new java.util.ArrayList<>();

        public void addError(String error) {
            errors.add(error);
        }

        public boolean isValid() {
            return errors.isEmpty();
        }

        public List<String> getErrors() {
            return List.copyOf(errors);
        }

        public String getErrorMessage() {
            return String.join("; ", errors);
        }
    }
}

