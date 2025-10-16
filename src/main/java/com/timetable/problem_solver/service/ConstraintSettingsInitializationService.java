package com.timetable.problem_solver.service;

import com.timetable.problem_solver.model.ConstraintSettings;
import com.timetable.problem_solver.repository.ConstraintSettingsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Service to initialize constraint settings in the database on application startup.
 * This ensures that all required constraints are available in the database
 * with their default enabled/disabled states.
 */
@Service
public class ConstraintSettingsInitializationService implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ConstraintSettingsInitializationService.class);

    private final ConstraintSettingsRepository constraintSettingsRepository;

    @Autowired
    public ConstraintSettingsInitializationService(ConstraintSettingsRepository constraintSettingsRepository) {
        this.constraintSettingsRepository = constraintSettingsRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        initializeConstraintSettings();
    }

    /**
     * Initialize constraint settings in the database if they don't exist.
     * This method creates default constraint settings for all available constraints.
     */
    private void initializeConstraintSettings() {
        logger.info("Initializing constraint settings in database...");

        List<ConstraintSettings> defaultConstraints = Arrays.asList(
            // Hard constraints
            createConstraintSetting("teacherConflict", 1, "Hard", true, 
                "Prevents a teacher from being assigned to more than one lesson in the same timeslot."),
            createConstraintSetting("studentGroupConflict", 1, "Hard", true, 
                "Prevents a student group (section) from having two lessons scheduled in the same timeslot."),
            createConstraintSetting("noFreeTimeslots", 1, "Hard", true, 
                "Ensures every lesson is assigned to a valid timeslot. Lessons without timeslot are penalized."),
            createConstraintSetting("teacherDailyWorkloadLimit", 1, "Hard", true, 
                "Limits each teacher to a maximum of 5 lessons per day."),
            createConstraintSetting("noBackToBackSameSubject", 1, "Hard", true, 
                "Prevents the same subject from being scheduled in consecutive periods for the same section on the same day."),

            // Semi-hard / flexible constraints
            createConstraintSetting("teacherNoThreeConsecutive", 1, "Soft", true, 
                "Avoids assigning a teacher to three consecutive classes without a break."),
            createConstraintSetting("practicalSubjectConsecutivePeriods", 1, "Soft", true, 
                "Ensures practical subjects are scheduled in consecutive periods (e.g., 2-hour lab sessions)."),

            // Soft constraints
            createConstraintSetting("teacherTimeEfficiency", 1, "Soft", true, 
                "Rewards teacher schedules with minimal gaps between lessons on the same day."),
            createConstraintSetting("practicalSubjectsWeekdaysOnly", 1, "Soft", true, 
                "Avoids assigning practical subjects on Saturdays, preferring weekdays."),
            createConstraintSetting("onePracticalPerDayPerSection", 1, "Soft", true, 
                "Ensures that each section has at most one practical subject scheduled per day."),
            createConstraintSetting("practicalSubjectTwoDaysPerWeek", 1, "Soft", true, 
                "Limits practical subjects to occur only two days per week per section."),
            createConstraintSetting("lowDifficultySubjectWeeklyLimit", 1, "Soft", true, 
                "Restricts low-difficulty subjects to a maximum of 2 sessions per week."),
            createConstraintSetting("lowDifficultyOncePerDay", 1, "Soft", true, 
                "Ensures low-difficulty subjects are not repeated more than once per day."),
            createConstraintSetting("preferHighPriorityTheory", 1, "Soft", true, 
                "Rewards scheduling of high-difficulty theory subjects, improving timetable quality.")
        );

        int createdCount = 0;
        int existingCount = 0;

        for (ConstraintSettings constraint : defaultConstraints) {
            if (!constraintSettingsRepository.findByConstraintName(constraint.getConstraintName()).isPresent()) {
                constraintSettingsRepository.save(constraint);
                createdCount++;
                logger.debug("Created constraint setting: {}", constraint.getConstraintName());
            } else {
                existingCount++;
                logger.debug("Constraint setting already exists: {}", constraint.getConstraintName());
            }
        }

        logger.info("Constraint settings initialization completed. Created: {}, Existing: {}", 
                createdCount, existingCount);
    }

    /**
     * Create a constraint setting with the specified parameters.
     */
    private ConstraintSettings createConstraintSetting(String name, int weight, String type, 
                                                      boolean enabled, String description) {
        ConstraintSettings constraint = new ConstraintSettings();
        constraint.setConstraintName(name);
        constraint.setConstraintWeight(weight);
        constraint.setConstraintType(type);
        constraint.setEnableFlag(enabled);
        constraint.setDescription(description);
        return constraint;
    }
}
