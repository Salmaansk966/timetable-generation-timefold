package com.timetable.problem_solver.constraints;

import java.time.DayOfWeek;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import ai.timefold.solver.core.api.score.stream.*;
import org.jspecify.annotations.NonNull;
import com.timetable.problem_solver.model.TimeFoldLesson;
import com.timetable.problem_solver.model.DifficultyLevel;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;

public class TimeTableConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint @NonNull [] defineConstraints(@NonNull ConstraintFactory factory) {
        return new Constraint[]{
                // Hard constraints (must never be violated)
                teacherConflict(factory),
                studentGroupConflict(factory),
                noFreeTimeslots(factory),
                teacherDailyWorkloadLimit(factory),
                noBackToBackSameSubject(factory),

                // Slightly relaxed constraints (soft for flexibility)
                teacherNoThreeConsecutive(factory),
                practicalSubjectConsecutivePeriods(factory),

                // Soft constraints (optimization goals)
                teacherTimeEfficiency(factory),
                practicalSubjectsWeekdaysOnly(factory),
                onePracticalPerDayPerSection(factory),
                practicalSubjectTwoDaysPerWeek(factory),
                lowDifficultySubjectWeeklyLimit(factory),
                lowDifficultyOncePerDay(factory),
                preferHighPriorityTheory(factory)
        };
    }

    // --- HARD CONSTRAINTS ---

    Constraint teacherConflict(ConstraintFactory factory) {
        return factory.forEachUniquePair(TimeFoldLesson.class,
                        Joiners.equal(TimeFoldLesson::getTimeslot),
                        Joiners.equal(l -> l.getTeacher() != null ? l.getTeacher().getId() : null))
                .penalize(HardSoftScore.ONE_HARD)
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
        // Make this soft to allow solver flexibility
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
                .penalize(HardSoftScore.ONE_SOFT) // changed to soft
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
                .penalize(HardSoftScore.ONE_SOFT) // softened for better solvability
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
                        (sectionId, subjectId,day, count) -> Math.toIntExact(count - 1))
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