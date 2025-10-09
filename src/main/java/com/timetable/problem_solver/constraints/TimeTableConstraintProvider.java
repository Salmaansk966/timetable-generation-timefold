package com.timetable.problem_solver.constraints;

import java.time.Duration;

import org.jspecify.annotations.NonNull;

import com.timetable.problem_solver.model.TimeFoldLesson;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;

public class TimeTableConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint @NonNull [] defineConstraints(@NonNull ConstraintFactory factory) {
        return new Constraint[]{
                teacherConflict(factory),
                studentGroupConflict(factory),
                noFreeTimeslots(factory),
                teacherTimeEfficiency(factory),
                studentGroupSubjectVariety(factory)
        };
    }

    // A teacher can teach at most one lesson at the same timeslot.
    Constraint teacherConflict(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEachUniquePair(TimeFoldLesson.class,
                        Joiners.equal(TimeFoldLesson::getTimeslot),
                        Joiners.equal(l -> l.getTeacher() == null ? null : l.getTeacher().getId()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Teacher conflict");
    }

    // A student group can attend at most one lesson at the same timeslot.
    Constraint studentGroupConflict(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEachUniquePair(TimeFoldLesson.class,
                        Joiners.equal(TimeFoldLesson::getTimeslot),
                        Joiners.equal(l -> l.getSection() == null ? null : l.getSection().getId()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Student group conflict");
    }

    // Penalize unassigned lessons (not scheduled in any timeslot).
    Constraint noFreeTimeslots(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(TimeFoldLesson.class)
                .filter(lesson -> lesson.getTimeslot() == null)
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Unassigned lesson");
    }

    // Teacher prefers sequential lessons (minimize gaps).
    Constraint teacherTimeEfficiency(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(TimeFoldLesson.class)
                .join(TimeFoldLesson.class,
                        Joiners.equal(l -> l.getTeacher() == null ? null : l.getTeacher().getId()),
                        Joiners.equal(l -> l.getTimeslot() == null ? null : l.getTimeslot().getDayOfWeek()))
                .filter((lesson1, lesson2) -> {
                    if (lesson1.getTimeslot() == null || lesson2.getTimeslot() == null) return false;
                    if (lesson1.getTimeslot().equals(lesson2.getTimeslot())) return false;
                    Duration between = Duration.between(
                            lesson1.getTimeslot().getEndTime(),
                            lesson2.getTimeslot().getStartTime());
                    return !between.isNegative() && between.compareTo(Duration.ofMinutes(30)) <= 0;
                })
                .reward(HardSoftScore.ONE_SOFT)
                .asConstraint("Teacher time efficiency");
    }

    // Penalize if a student group has the same subject back-to-back.
    Constraint studentGroupSubjectVariety(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(TimeFoldLesson.class)
                .join(TimeFoldLesson.class,
                        Joiners.equal(l -> l.getSubject() == null ? null : l.getSubject().getId()),
                        Joiners.equal(l -> l.getSection() == null ? null : l.getSection().getId()),
                        Joiners.equal(l -> l.getTimeslot() == null ? null : l.getTimeslot().getDayOfWeek()))
                .filter((lesson1, lesson2) -> {
                    if (lesson1.getTimeslot() == null || lesson2.getTimeslot() == null) return false;
                    if (lesson1.getTimeslot().equals(lesson2.getTimeslot())) return false;
                    Duration between = Duration.between(
                            lesson1.getTimeslot().getEndTime(),
                            lesson2.getTimeslot().getStartTime());
                    return !between.isNegative() && between.compareTo(Duration.ofMinutes(30)) <= 0;
                })
                .penalize(HardSoftScore.ONE_SOFT)
                .asConstraint("Student group subject variety");
    }
}