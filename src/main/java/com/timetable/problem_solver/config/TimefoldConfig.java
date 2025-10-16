package com.timetable.problem_solver.config;

import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Timefold Solver.
 * 
 * Note: SolverManager and SolverFactory are now managed dynamically by SolverService
 * to allow for constraint reloading without application restart.
 * The TimeTableConstraintProvider is still created as a Spring bean and injected
 * into the SolverService for dynamic solver creation.
 */
@Configuration
public class TimefoldConfig {
    
    // SolverManager and SolverFactory are now managed by SolverService
    // This allows for dynamic reloading of constraint configurations
    
}