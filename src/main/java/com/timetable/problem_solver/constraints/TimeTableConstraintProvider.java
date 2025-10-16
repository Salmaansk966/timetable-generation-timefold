package com.timetable.problem_solver.constraints;

import java.time.DayOfWeek;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.timetable.problem_solver.model.ConstraintSettings;
import com.timetable.problem_solver.model.DifficultyLevel;
import com.timetable.problem_solver.model.TimeFoldLesson;
import com.timetable.problem_solver.repository.ConstraintSettingsRepository;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;

/**
 * Dynamic constraint provider that reads constraint settings from the database.
 * This allows constraints to be enabled/disabled without application restart.
 */
@Component
public class TimeTableConstraintProvider implements ConstraintProvider, ApplicationContextAware {

    private static ApplicationContext applicationContext;
    private ConstraintSettingsRepository constraintSettingsRepository;

    // Public no-arg constructor required by Timefold Solver
    public TimeTableConstraintProvider() {
        // Repository will be injected by Spring context
    }

    @Autowired
    public void setConstraintSettingsRepository(ConstraintSettingsRepository constraintSettingsRepository) {
        this.constraintSettingsRepository = constraintSettingsRepository;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }

    /**
     * Get the repository instance from Spring context
     */
    private ConstraintSettingsRepository getRepository() {
        if (constraintSettingsRepository != null) {
            return constraintSettingsRepository;
        }
        if (applicationContext != null) {
            return applicationContext.getBean(ConstraintSettingsRepository.class);
        }
        throw new IllegalStateException("Cannot access ConstraintSettingsRepository - Spring context not available");
    }

    @Override
    public Constraint @NonNull [] defineConstraints(@NonNull ConstraintFactory factory) {
        List<Constraint> constraintList = new ArrayList<>();

        // Load constraint settings from database dynamically
        Map<String, ConstraintSettings> constraintSettingsMap = loadConstraintSettings();

        // Process all constraints from database (both predefined and custom)
        for (ConstraintSettings settings : constraintSettingsMap.values()) {
            if (settings.isEnableFlag()) {
                Constraint constraint = createConstraintByName(factory, settings);
                if (constraint != null) {
                    constraintList.add(constraint);
                }
            }
        }

        return constraintList.toArray(new Constraint[0]);
    }

    /**
     * Create a constraint by name from the database settings.
     * This method handles both predefined constraints and custom constraints.
     */
    private Constraint createConstraintByName(ConstraintFactory factory, ConstraintSettings settings) {
        String constraintName = settings.getConstraintName();
        int weight = settings.getConstraintWeight();

        return switch (constraintName) {
            // Hard constraints (must never be violated)
            case "teacherConflict" -> teacherConflict(factory, weight);
            case "studentGroupConflict" -> studentGroupConflict(factory);
            case "noFreeTimeslots" -> noFreeTimeslots(factory);
            case "teacherDailyWorkloadLimit" -> teacherDailyWorkloadLimit(factory);
            case "noBackToBackSameSubject" -> noBackToBackSameSubject(factory);

            // Semi-hard / flexible constraints
            case "teacherNoThreeConsecutive" -> teacherNoThreeConsecutive(factory);
            case "practicalSubjectConsecutivePeriods" -> practicalSubjectConsecutivePeriods(factory);

            // Soft constraints (optimization goals)
            case "teacherTimeEfficiency" -> teacherTimeEfficiency(factory);
            case "practicalSubjectsWeekdaysOnly" -> practicalSubjectsWeekdaysOnly(factory);
            case "onePracticalPerDayPerSection" -> onePracticalPerDayPerSection(factory);
            case "practicalSubjectTwoDaysPerWeek" -> practicalSubjectTwoDaysPerWeek(factory);
            case "lowDifficultySubjectWeeklyLimit" -> lowDifficultySubjectWeeklyLimit(factory);
            case "lowDifficultyOncePerDay" -> lowDifficultyOncePerDay(factory);
            case "preferHighPriorityTheory" -> preferHighPriorityTheory(factory);

            // Custom constraints - create generic constraint based on type
            default -> createCustomConstraint(factory, settings);
        };
    }

    /**
     * Create a custom constraint based on the settings.
     * This allows users to add constraints that don't have predefined implementations.
     */
    private Constraint createCustomConstraint(ConstraintFactory factory, ConstraintSettings settings) {
        String constraintName = settings.getConstraintName();
        int weight = settings.getConstraintWeight();
        String type = settings.getConstraintType();

        // For custom constraints, we create a simple generic constraint
        // In a real implementation, you might want to store constraint logic in the database
        // or have a plugin system for custom constraints
        
        if ("Hard".equalsIgnoreCase(type)) {
            // Create a hard constraint that penalizes unassigned lessons
            return factory.forEach(TimeFoldLesson.class)
                    .filter(lesson -> lesson.getTimeslot() == null)
                    .penalize(HardSoftScore.ofHard(weight))
                    .asConstraint("Custom Hard: " + constraintName);
        } else {
            // Create a soft constraint that rewards assigned lessons
            return factory.forEach(TimeFoldLesson.class)
                    .filter(lesson -> lesson.getTimeslot() != null)
                    .reward(HardSoftScore.ofSoft(weight))
                    .asConstraint("Custom Soft: " + constraintName);
        }
    }

    /**
     * Load all constraint settings from database and create a map for quick lookup
     */
    private Map<String, ConstraintSettings> loadConstraintSettings() {
        return getRepository().findAll().stream()
                .collect(Collectors.toMap(
                        ConstraintSettings::getConstraintName,
                        settings -> settings
                ));
    }

    /**
     * Check if a constraint is enabled by name
     */
    private boolean isConstraintEnabled(Map<String, ConstraintSettings> settingsMap, String constraintName) {
        ConstraintSettings settings = settingsMap.get(constraintName);
        return settings != null && settings.isEnableFlag();
    }

    /**
     * Get constraint weight by name, defaulting to 1 if not found
     */
    private int getConstraintWeight(Map<String, ConstraintSettings> settingsMap, String constraintName) {
        ConstraintSettings settings = settingsMap.get(constraintName);
        return settings != null ? settings.getConstraintWeight() : 1;
    }

    // --- HARD CONSTRAINTS ---

    Constraint teacherConflict(ConstraintFactory factory, int weight) {
        return factory.forEachUniquePair(TimeFoldLesson.class,
                        Joiners.equal(TimeFoldLesson::getTimeslot),
                        Joiners.equal(l -> l.getTeacher() != null ? l.getTeacher().getId() : null))
                .penalize(HardSoftScore.ofHard(weight))
                .asConstraint("Teacher conflict");
    }

    Constraint studentGroupConflict(ConstraintFactory factory) {
        return factory.forEachUniquePair(TimeFoldLesson.class,
                        Joiners.equal(TimeFoldLesson::getTimeslot),
                        Joiners.equal(l -> l.getSection() != null ? l.getSection().getId() : null))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Student group conflict");
    }

    Constraint noFreeTimeslots(ConstraintFactory factory) {
        return factory.forEach(TimeFoldLesson.class)
                .filter(lesson -> lesson.getTimeslot() == null)
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Unassigned lesson");
    }

    Constraint teacherDailyWorkloadLimit(ConstraintFactory factory) {
        return factory.forEach(TimeFoldLesson.class)
                .filter(l -> l.getTimeslot() != null && l.getTeacher() != null)
                .groupBy(l -> l.getTeacher().getId(),
                        l -> l.getTimeslot().getDayOfWeek(),
                        ConstraintCollectors.count())
                .filter((teacherId, day, count) -> count > 5)
                .penalize(HardSoftScore.ONE_HARD,
                        (teacherId, day, count) -> Math.toIntExact(count - 5))
                .asConstraint("Teacher daily workload limit");
    }

    Constraint noBackToBackSameSubject(ConstraintFactory factory) {
        return factory.forEach(TimeFoldLesson.class)
                .join(TimeFoldLesson.class,
                        Joiners.equal(l -> l.getSubject() != null ? l.getSubject().getId() : null),
                        Joiners.equal(l -> l.getSection() != null ? l.getSection().getId() : null),
                        Joiners.equal(l -> l.getTimeslot() != null ? l.getTimeslot().getDayOfWeek() : null))
                .filter((l1, l2) -> {
                    if (l1.getTimeslot() == null || l2.getTimeslot() == null) return false;
                    if (l1.getTimeslot().equals(l2.getTimeslot())) return false;
                    Duration gap = Duration.between(l1.getTimeslot().getEndTime(), l2.getTimeslot().getStartTime());
                    return !gap.isNegative() && gap.compareTo(Duration.ofMinutes(30)) <= 0;
                })
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("No back-to-back same subject");
    }

    // --- SEMI-HARD / FLEXIBLE RULES ---

    Constraint teacherNoThreeConsecutive(ConstraintFactory factory) {
        return factory.forEach(TimeFoldLesson.class)
                .filter(l -> l.getTimeslot() != null && l.getTeacher() != null)
                .groupBy(l -> l.getTeacher().getId(),
                        l -> l.getTimeslot().getDayOfWeek(),
                        ConstraintCollectors.toList())
                .filter((teacherId, day, lessons) -> {
                    if (lessons.size() < 3) return false;
                    lessons.sort(Comparator.comparing(l -> l.getTimeslot().getStartTime()));
                    for (int i = 0; i < lessons.size() - 2; i++) {
                        Duration gap1 = Duration.between(
                                lessons.get(i).getTimeslot().getEndTime(),
                                lessons.get(i + 1).getTimeslot().getStartTime());
                        Duration gap2 = Duration.between(
                                lessons.get(i + 1).getTimeslot().getEndTime(),
                                lessons.get(i + 2).getTimeslot().getStartTime());
                        if (gap1.compareTo(Duration.ofMinutes(30)) <= 0 &&
                                gap2.compareTo(Duration.ofMinutes(30)) <= 0) {
                            return true;
                        }
                    }
                    return false;
                })
                .penalize(HardSoftScore.ONE_SOFT)
                .asConstraint("Teacher no three consecutive periods");
    }

    Constraint practicalSubjectConsecutivePeriods(ConstraintFactory factory) {
        return factory.forEach(TimeFoldLesson.class)
                .filter(l -> l.getSubject() != null && Boolean.TRUE.equals(l.getSubject().getIsPractical()))
                .groupBy(l -> l.getSubject().getId(),
                        l -> l.getSection().getId(),
                        l -> l.getTimeslot().getDayOfWeek(),
                        ConstraintCollectors.toList())
                .filter((subjectId, sectionId, day, lessons) -> {
                    if (lessons.size() != 2) return true;
                    lessons.sort(Comparator.comparing(l -> l.getTimeslot().getStartTime()));
                    Duration gap = Duration.between(lessons.get(0).getTimeslot().getEndTime(),
                            lessons.get(1).getTimeslot().getStartTime());
                    return gap.compareTo(Duration.ofMinutes(30)) > 0;
                })
                .penalize(HardSoftScore.ONE_SOFT)
                .asConstraint("Practical subject consecutive periods");
    }

    // --- SOFT CONSTRAINTS ---

    Constraint teacherTimeEfficiency(ConstraintFactory factory) {
        return factory.forEach(TimeFoldLesson.class)
                .join(TimeFoldLesson.class,
                        Joiners.equal(l -> l.getTeacher() != null ? l.getTeacher().getId() : null),
                        Joiners.equal(l -> l.getTimeslot() != null ? l.getTimeslot().getDayOfWeek() : null))
                .filter((l1, l2) -> {
                    if (l1.getTimeslot() == null || l2.getTimeslot() == null) return false;
                    if (l1.getTimeslot().equals(l2.getTimeslot())) return false;
                    Duration gap = Duration.between(l1.getTimeslot().getEndTime(), l2.getTimeslot().getStartTime());
                    return !gap.isNegative() && gap.compareTo(Duration.ofMinutes(30)) <= 0;
                })
                .reward(HardSoftScore.ONE_SOFT)
                .asConstraint("Teacher time efficiency");
    }

    Constraint practicalSubjectsWeekdaysOnly(ConstraintFactory factory) {
        return factory.forEach(TimeFoldLesson.class)
                .filter(l -> l.getSubject() != null &&
                        Boolean.TRUE.equals(l.getSubject().getIsPractical()) &&
                        l.getTimeslot() != null &&
                        l.getTimeslot().getDayOfWeek() == DayOfWeek.SATURDAY)
                .penalize(HardSoftScore.ONE_SOFT)
                .asConstraint("Practical subjects weekdays only");
    }

    Constraint onePracticalPerDayPerSection(ConstraintFactory factory) {
        return factory.forEach(TimeFoldLesson.class)
                .filter(l -> l.getSubject() != null && Boolean.TRUE.equals(l.getSubject().getIsPractical()))
                .groupBy(l -> l.getSection().getId(),
                        l -> l.getTimeslot().getDayOfWeek(),
                        ConstraintCollectors.count())
                .filter((sectionId, day, count) -> count > 1)
                .penalize(HardSoftScore.ONE_SOFT,
                        (sectionId, day, count) -> Math.toIntExact(count - 1))
                .asConstraint("One practical per day per section");
    }

    Constraint practicalSubjectTwoDaysPerWeek(ConstraintFactory factory) {
        return factory.forEach(TimeFoldLesson.class)
                .filter(l -> l.getSubject() != null && Boolean.TRUE.equals(l.getSubject().getIsPractical()))
                .groupBy(l -> l.getSection().getId(),
                        l -> l.getSubject().getId(),
                        ConstraintCollectors.toList())
                .filter((sectionId, subjectId, lessons) -> {
                    Set<DayOfWeek> distinctDays = lessons.stream()
                            .filter(l -> l.getTimeslot() != null)
                            .map(l -> l.getTimeslot().getDayOfWeek())
                            .collect(Collectors.toSet());
                    return distinctDays.size() > 2;
                })
                .penalize(HardSoftScore.ONE_SOFT)
                .asConstraint("Practical subject two days per week");
    }

    Constraint lowDifficultySubjectWeeklyLimit(ConstraintFactory factory) {
        return factory.forEach(TimeFoldLesson.class)
                .filter(l -> l.getSubject() != null && l.getSubject().getDifficultyLevel() == DifficultyLevel.LOW)
                .groupBy(l -> l.getSection().getId(),
                        l -> l.getSubject().getId(),
                        ConstraintCollectors.count())
                .filter((sectionId, subjectId, count) -> count > 2)
                .penalize(HardSoftScore.ONE_SOFT,
                        (sectionId, subjectId, count) -> Math.toIntExact(count - 2))
                .asConstraint("Low difficulty subject weekly limit");
    }

    Constraint lowDifficultyOncePerDay(ConstraintFactory factory) {
        return factory.forEach(TimeFoldLesson.class)
                .filter(l -> l.getSubject() != null && l.getSubject().getDifficultyLevel() == DifficultyLevel.LOW)
                .groupBy(l -> l.getSection().getId(),
                        l -> l.getSubject().getId(),
                        l -> l.getTimeslot().getDayOfWeek(),
                        ConstraintCollectors.count())
                .filter((sectionId, subjectId, day, count) -> count > 1)
                .penalize(HardSoftScore.ONE_SOFT,
                        (sectionId, subjectId, day, count) -> Math.toIntExact(count - 1))
                .asConstraint("Low difficulty once per day");
    }

    Constraint preferHighPriorityTheory(ConstraintFactory factory) {
        return factory.forEach(TimeFoldLesson.class)
                .filter(l -> l.getSubject() != null &&
                        Boolean.TRUE.equals(l.getSubject().getIsTheory()) &&
                        l.getSubject().getDifficultyLevel() == DifficultyLevel.HIGH)
                .reward(HardSoftScore.ONE_SOFT)
                .asConstraint("Prefer high priority theory");
    }
}