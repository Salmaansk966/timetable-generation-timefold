package com.timetable.problem_solver.controller;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;
import com.timetable.problem_solver.model.TimeFoldTimetable;
import com.timetable.problem_solver.service.SolverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controller for managing solver operations including solving and reloading.
 * Provides endpoints to start solving with the latest constraint configuration
 * and to reload the solver when constraints are updated.
 */
@RestController
@RequestMapping("/api/solver")
public class SolverController {

    private static final Logger logger = LoggerFactory.getLogger(SolverController.class);

    private final SolverService solverService;
    
    // Track active solving jobs
    private final Map<String, TimeFoldTimetable> activeJobs = new ConcurrentHashMap<>();

    @Autowired
    public SolverController(SolverService solverService) {
        this.solverService = solverService;
    }

    /**
     * Start solving a timetable problem using the current solver configuration.
     * The solver will use the latest constraint settings from the database.
     * 
     * @param problem The timetable problem to solve
     * @return Job ID for tracking the solving progress
     */
    @PostMapping(value = "/solve", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> solve(@RequestBody TimeFoldTimetable problem) {
        try {
            logger.info("Starting timetable solving with current constraint configuration...");
            
            // Generate unique job ID
            String jobId = UUID.randomUUID().toString();
            
            // Store the problem for tracking
            activeJobs.put(jobId, problem);
            
            // Get the current solver manager (with latest constraint settings)
            SolverManager<TimeFoldTimetable, String> solverManager = solverService.getSolverManager();
            
            // Start solving with callback to update the stored solution
            solverManager.solveAndListen(
                jobId,
                problem,
                solution -> {
                    logger.debug("Solution updated for job {}: score = {}", jobId, solution.getScore());
                    activeJobs.put(jobId, solution);
                }
            );
            
            logger.info("Solving started successfully for job ID: {}", jobId);
            return ResponseEntity.ok(jobId);
            
        } catch (Exception e) {
            logger.error("Failed to start solving: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to start solving: " + e.getMessage());
        }
    }

    /**
     * Get the current solution for a solving job.
     * 
     * @param jobId The job ID returned from the solve endpoint
     * @return The current timetable solution with solver status
     */
    @GetMapping(value = "/{jobId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TimeFoldTimetable> getSolution(@PathVariable String jobId) {
        try {
            TimeFoldTimetable solution = activeJobs.get(jobId);
            if (solution == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Get current solver status
            SolverManager<TimeFoldTimetable, String> solverManager = solverService.getSolverManager();
            SolverStatus status = solverManager.getSolverStatus(jobId);
            solution.setSolverStatus(status);
            
            return ResponseEntity.ok(solution);
            
        } catch (Exception e) {
            logger.error("Failed to get solution for job {}: {}", jobId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get the solver status for a specific job.
     * 
     * @param jobId The job ID to check
     * @return The current solver status
     */
    @GetMapping(value = "/{jobId}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getStatus(@PathVariable String jobId) {
        try {
            SolverManager<TimeFoldTimetable, String> solverManager = solverService.getSolverManager();
            SolverStatus status = solverManager.getSolverStatus(jobId);
            
            Map<String, Object> response = Map.of(
                "jobId", jobId,
                "status", status.toString(),
                "isSolving", status == SolverStatus.SOLVING_ACTIVE,
                "isFinished", status == SolverStatus.NOT_SOLVING
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get status for job {}: {}", jobId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Terminate a running solving job.
     * 
     * @param jobId The job ID to terminate
     * @return The final solution before termination
     */
    @DeleteMapping(value = "/{jobId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TimeFoldTimetable> terminateSolving(@PathVariable String jobId) {
        try {
            logger.info("Terminating solving job: {}", jobId);
            
            SolverManager<TimeFoldTimetable, String> solverManager = solverService.getSolverManager();
            solverManager.terminateEarly(jobId);
            
            // Return the current solution
            TimeFoldTimetable solution = activeJobs.get(jobId);
            if (solution != null) {
                SolverStatus status = solverManager.getSolverStatus(jobId);
                solution.setSolverStatus(status);
                return ResponseEntity.ok(solution);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Failed to terminate job {}: {}", jobId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Reload the solver configuration with fresh constraint settings from database.
     * This endpoint should be called after constraint settings are updated to ensure
     * the next solve operation uses the new configuration.
     * 
     * @return Success message with solver information
     */
    @PostMapping("/reload")
    public ResponseEntity<Map<String, String>> reloadSolver() {
        try {
            logger.info("Reloading solver configuration...");
            
            // Reload the solver with fresh constraint settings
            solverService.reloadSolver();
            
            // Get updated solver information
            String solverInfo = solverService.getSolverInfo();
            
            Map<String, String> response = Map.of(
                "message", "Solver configuration reloaded successfully",
                "solverInfo", solverInfo,
                "timestamp", java.time.Instant.now().toString()
            );
            
            logger.info("Solver reload completed: {}", solverInfo);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to reload solver: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to reload solver: " + e.getMessage()));
        }
    }

    /**
     * Get information about the current solver configuration.
     * 
     * @return Solver information including constraint provider details
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> getSolverInfo() {
        try {
            String solverInfo = solverService.getSolverInfo();
            boolean isBusy = solverService.isSolverBusy();
            
            Map<String, String> response = Map.of(
                "solverInfo", solverInfo,
                "isBusy", String.valueOf(isBusy),
                "activeJobs", String.valueOf(activeJobs.size())
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get solver info: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get solver info: " + e.getMessage()));
        }
    }

    /**
     * List all active solving jobs.
     * 
     * @return List of active job IDs
     */
    @GetMapping("/jobs")
    public ResponseEntity<Map<String, Object>> listActiveJobs() {
        try {
            Map<String, Object> response = Map.of(
                "activeJobs", activeJobs.keySet(),
                "jobCount", activeJobs.size()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to list active jobs: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to list active jobs: " + e.getMessage()));
        }
    }
}
