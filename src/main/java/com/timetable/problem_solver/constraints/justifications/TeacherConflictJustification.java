//package com.timetable.problem_solver.constraints.justifications;
//
//import ai.timefold.solver.core.api.score.stream.ConstraintJustification;
//import com.timetable.problem_solver.model.TimeFoldLesson;
//
//public record TeacherConflictJustification(String teacher, TimeFoldLesson lesson1, TimeFoldLesson lesson2, String description)
//        implements
//            ConstraintJustification {
//
//    public TeacherConflictJustification(String teacher, TimeFoldLesson lesson1, TimeFoldLesson lesson2) {
//        this(teacher, lesson1, lesson2,
//                "Teacher '%s' needs to teach lesson '%s' for student group '%s' and lesson '%s' for student group '%s' at '%s %s'"
//                        .formatted(teacher, lesson1.getSubject(), lesson1.getSection().getClassMaster()+ "-" + lesson1.getSection().getSectionName(), lesson2.getSubject(),
//                                lesson2.getSection().getClassMaster()+ "-" + lesson2.getSection().getSectionName(), lesson1.getTimeslot().getDayOfWeek(),
//                                lesson1.getTimeslot().getStartTime()));
//    }
//}
