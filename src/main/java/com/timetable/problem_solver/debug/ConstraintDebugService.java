package com.timetable.problem_solver.debug;

import com.timetable.problem_solver.model.ConstraintSettings;
import com.timetable.problem_solver.repository.ConstraintSettingsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Debug service to help diagnose constraint loading issues.
 * This service provides detailed logging of constraint states.
 */
@Service
public class ConstraintDebugService {

    private static final Logger logger = LoggerFactory.getLogger(ConstraintDebugService.class);

    private final ConstraintSettingsRepository constraintSettingsRepository;

    @Autowired
    public ConstraintDebugService(ConstraintSettingsRepository constraintSettingsRepository) {
        this.constraintSettingsRepository = constraintSettingsRepository;
    }

    /**
     * Log detailed information about all constraints in the database.
     */
    public void logAllConstraints() {
        logger.info("=== CONSTRAINT DEBUG INFO ===");
        
        List<ConstraintSettings> allConstraints = constraintSettingsRepository.findAll();
        logger.info("Total constraints in database: {}", allConstraints.size());
        
        Map<String, ConstraintSettings> constraintMap = allConstraints.stream()
                .collect(Collectors.toMap(
                        ConstraintSettings::getConstraintName,
                        settings -> settings
                ));
        
        logger.info("=== ENABLED CONSTRAINTS ===");
        constraintMap.values().stream()
                .filter(ConstraintSettings::isEnableFlag)
                .forEach(settings -> logger.info("✓ {} - {} (weight: {})", 
                        settings.getConstraintName(), 
                        settings.getDescription(),
                        settings.getConstraintWeight()));
        
        logger.info("=== DISABLED CONSTRAINTS ===");
        constraintMap.values().stream()
                .filter(settings -> !settings.isEnableFlag())
                .forEach(settings -> logger.info("✗ {} - {} (weight: {})", 
                        settings.getConstraintName(), 
                        settings.getDescription(),
                        settings.getConstraintWeight()));
        
        logger.info("=== CONSTRAINT DEBUG INFO END ===");
    }

    /**
     * Check if a specific constraint is enabled in the database.
     */
    public boolean isConstraintEnabledInDatabase(String constraintName) {
        return constraintSettingsRepository.findByConstraintName(constraintName)
                .map(ConstraintSettings::isEnableFlag)
                .orElse(false);
    }

    /**
     * Get all enabled constraint names.
     */
    public List<String> getEnabledConstraintNames() {
        return constraintSettingsRepository.findAllEnabled().stream()
                .map(ConstraintSettings::getConstraintName)
                .toList();
    }

    /**
     * Get all disabled constraint names.
     */
    public List<String> getDisabledConstraintNames() {
        return constraintSettingsRepository.findAllDisabled().stream()
                .map(ConstraintSettings::getConstraintName)
                .toList();
    }
}

