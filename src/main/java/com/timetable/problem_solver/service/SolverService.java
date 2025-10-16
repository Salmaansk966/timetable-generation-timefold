package com.timetable.problem_solver.service;

import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.config.solver.SolverConfig;
import com.timetable.problem_solver.constraints.TimeTableConstraintProvider;
import com.timetable.problem_solver.model.TimeFoldLesson;
import com.timetable.problem_solver.model.TimeFoldTimetable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Service that manages dynamic SolverManager creation and reloading.
 * This allows the solver to be reconfigured with new constraint settings
 * without restarting the application.
 */
@Service
public class SolverService {

    private static final Logger logger = LoggerFactory.getLogger(SolverService.class);

    private final TimeTableConstraintProvider constraintProvider;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    private volatile SolverManager<TimeFoldTimetable, String> solverManager;
    private volatile SolverFactory<TimeFoldTimetable> solverFactory;

    @Autowired
    public SolverService(TimeTableConstraintProvider constraintProvider) {
        this.constraintProvider = constraintProvider;
        // Initialize the solver manager on startup
        reloadSolver();
    }

    /**
     * Get the current SolverManager instance.
     * This method is thread-safe and will return the latest configured solver.
     */
    public SolverManager<TimeFoldTimetable, String> getSolverManager() {
        lock.readLock().lock();
        try {
            if (solverManager == null) {
                logger.warn("SolverManager is null, attempting to reload...");
                lock.readLock().unlock();
                reloadSolver();
                lock.readLock().lock();
            }
            return solverManager;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Check if the solver manager is properly initialized
     */
    public boolean isSolverInitialized() {
        lock.readLock().lock();
        try {
            return solverManager != null;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Reload the solver configuration with fresh constraint settings from database.
     * This method creates a new SolverManager and SolverFactory with the latest
     * constraint configuration, ensuring that constraint changes take effect immediately.
     */
    public void reloadSolver() {
        lock.writeLock().lock();
        try {
            logger.info("Reloading solver configuration with fresh constraint settings...");
            
            // Close old solver manager first to free resources
            if (solverManager != null) {
                try {
                    logger.info("Closing existing solver manager...");
                    solverManager.close();
                    // Wait a bit for cleanup
                    Thread.sleep(100);
                } catch (Exception e) {
                    logger.warn("Error closing old solver manager: {}", e.getMessage());
                }
            }
            
            // Create new solver configuration with constraint provider class
            // The constraint provider will be instantiated by Timefold with no-arg constructor
            SolverConfig solverConfig = new SolverConfig()
                    .withSolutionClass(TimeFoldTimetable.class)
                    .withEntityClasses(TimeFoldLesson.class)
                    .withConstraintProviderClass(constraintProvider.getClass());

            logger.info("Creating new solver factory...");
            // Create new solver factory
            SolverFactory<TimeFoldTimetable> newSolverFactory = SolverFactory.create(solverConfig);
            
            logger.info("Creating new solver manager...");
            // Create new solver manager with proper configuration
            SolverManager<TimeFoldTimetable, String> newSolverManager = SolverManager.create(newSolverFactory);
            
            // Verify the solver manager is properly initialized
            if (newSolverManager == null) {
                throw new RuntimeException("Failed to create SolverManager - returned null");
            }
            
            // Update references atomically
            this.solverFactory = newSolverFactory;
            this.solverManager = newSolverManager;
            
            logger.info("Solver configuration reloaded successfully. New constraint settings are now active.");
            
        } catch (Exception e) {
            logger.error("Failed to reload solver configuration: {}", e.getMessage(), e);
            // Don't throw exception to prevent application startup failure
            // Just log the error and keep the old solver manager if it exists
            logger.warn("Keeping existing solver manager due to reload failure");
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Check if the solver is currently running any jobs
     */
    public boolean isSolverBusy() {
        lock.readLock().lock();
        try {
            // Note: SolverManager doesn't provide a direct way to check if it's busy
            // This is a simplified check - in production you might want to track active jobs
            return solverManager != null;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Get information about the current solver configuration
     */
    public String getSolverInfo() {
        lock.readLock().lock();
        try {
            if (solverManager == null) {
                return "Solver not initialized";
            }
            return String.format("Solver active with constraint provider: %s", 
                    constraintProvider.getClass().getSimpleName());
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Clean up resources when the service is destroyed
     */
    @PreDestroy
    public void cleanup() {
        lock.writeLock().lock();
        try {
            if (solverManager != null) {
                logger.info("Closing solver manager during application shutdown...");
                solverManager.close();
            }
        } catch (Exception e) {
            logger.warn("Error during solver manager cleanup: {}", e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }
}
