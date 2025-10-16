package com.timetable.problem_solver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.timetable.problem_solver.model.ConstraintSettings;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing constraint settings in the database.
 * Provides methods to query constraint configurations dynamically.
 */
@Repository
public interface ConstraintSettingsRepository extends JpaRepository<ConstraintSettings, Long> {

    /**
     * Find constraint setting by constraint name
     */
    Optional<ConstraintSettings> findByConstraintName(String constraintName);

    /**
     * Find all enabled constraints
     */
    @Query("SELECT c FROM ConstraintSettings c WHERE c.enableFlag = true")
    List<ConstraintSettings> findAllEnabled();

    /**
     * Find all disabled constraints
     */
    @Query("SELECT c FROM ConstraintSettings c WHERE c.enableFlag = false")
    List<ConstraintSettings> findAllDisabled();

    /**
     * Check if a constraint is enabled by name
     */
    @Query("SELECT c.enableFlag FROM ConstraintSettings c WHERE c.constraintName = :constraintName")
    Optional<Boolean> isConstraintEnabled(String constraintName);
}
