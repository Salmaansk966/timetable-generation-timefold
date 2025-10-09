//package com.timetable.problem_solver.model;
//
//import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
//import ai.timefold.solver.core.api.domain.solution.PlanningScore;
//import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
//import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
//import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
//import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
//import ai.timefold.solver.core.api.solver.SolverStatus;
//
//import java.util.List;
//@PlanningSolution
//public class Timetable {
//
//    @ValueRangeProvider(id = "timeslotRange")
//    @ProblemFactCollectionProperty
//    private List<Timeslot> timeslots;
//
////    @ValueRangeProvider(id = "roomRange")
////    @ProblemFactCollectionProperty
//    private List<Room> rooms;
//
//    @PlanningEntityCollectionProperty
//    private List<Lesson> lessons;
//
//    // Ignored by Timefold, used by the UI to display solve or stop solving button
//    private SolverStatus solverStatus;
//
//    @PlanningScore
//    private HardSoftScore score;
//
//    public List<Timeslot> getTimeslots() {
//        return timeslots;
//    }
//
//    public void setTimeslots(List<Timeslot> timeslotList) {
//        this.timeslots = timeslotList;
//    }
//
//    public List<Room> getRooms() {
//        return rooms;
//    }
//
//    public void setRooms(List<Room> roomList) {
//        this.rooms = roomList;
//    }
//
//    public List<Lesson> getLessons() {
//        return lessons;
//    }
//
//    public void setLesson(List<Lesson> lessonList) {
//        this.lessons = lessonList;
//    }
//
//    public HardSoftScore getScore() {
//        return score;
//    }
//
//    public void setScore(HardSoftScore score) {
//        this.score = score;
//    }
//
//    public SolverStatus getSolverStatus() {
//        return solverStatus;
//    }
//
//    public void setSolverStatus(SolverStatus solverStatus) {
//        this.solverStatus = solverStatus;
//    }
//
//    public Timetable(List<Timeslot> timeslotList, List<Room> roomList, List<Lesson> lessonList, HardSoftScore score) {
//        this.timeslots = timeslotList;
//        this.rooms = roomList;
//        this.lessons = lessonList;
//        this.score = score;
//    }
//
//    public Timetable(HardSoftScore score, SolverStatus solverStatus) {
//        this.score = score;
//        this.solverStatus = solverStatus;
//    }
//
//    public Timetable() {
//    }
//
//    // Ensure free lessons are added
////    public void fillFreePeriods() {
////        int totalSlots = timeslots.size() * rooms.size();
////        int currentLessons = lessons.size();
////        int freeNeeded = totalSlots - currentLessons;
////
////        int nextId = currentLessons + 1;
////        for (int i = 0; i < freeNeeded; i++) {
////            Lesson free = new Lesson();
////            free.setId(nextId++);
////            free.setSubject("FREE");
////            free.setTeacher("NONE");
////            free.setStudentGroup("NONE");
////            lessons.add(free);
////        }
////    }
//}
