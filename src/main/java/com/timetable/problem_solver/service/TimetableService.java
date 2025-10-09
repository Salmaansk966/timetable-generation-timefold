//package com.timetable.problem_solver.service;
//
//import ai.timefold.solver.core.api.solver.SolverJob;
//import ai.timefold.solver.core.api.solver.SolverManager;
//import com.timetable.problem_solver.model.Lesson;
//import com.timetable.problem_solver.model.Timetable;
//import com.timetable.problem_solver.repository.LessonRepository;
//import com.timetable.problem_solver.repository.RoomRepository;
//import com.timetable.problem_solver.repository.TimeslotRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.UUID;
//import java.util.concurrent.ExecutionException;
//
//@Service
//public class TimetableService {
//
//    @Autowired
//    private SolverManager<Timetable, UUID> solverManager;
//
//    private final RoomRepository roomRepository;
//    private final LessonRepository lessonRepository;
//    private final TimeslotRepository timeSlotRepository;
//
//    public TimetableService(RoomRepository roomRepository, LessonRepository lessonRepository, TimeslotRepository timeSlotRepository) {
//        this.roomRepository = roomRepository;
//        this.lessonRepository = lessonRepository;
//        this.timeSlotRepository = timeSlotRepository;
//    }
//
//    public void solve() {
//        UUID problemId = UUID.randomUUID();
//        // Submit the problem to start solving
//        SolverJob<Timetable, UUID> solverJob = solverManager.solve(problemId, prepareProblem());
//        Timetable solution;
//        try {
//            // Wait until the solving ends
//            solution = solverJob.getFinalBestSolution();
//        } catch (InterruptedException | ExecutionException e) {
//            throw new IllegalStateException("Solving failed.", e);
//        }
//        this.saveSolution(solution);
//    }
//
//    private Timetable prepareProblem() {
//        return new Timetable(
//                timeSlotRepository.findAll(),
//                roomRepository.findAll(),
//                lessonRepository.findAll(),
//                null
//        );
//    }
//
//    public void saveSolution(Timetable solution) {
//        List<Lesson> withoutFree = solution.getLessons().stream().
//                filter(l -> !l.getSubject().equals("FREE")).toList();
//
//        lessonRepository.saveAll(withoutFree);
//        System.out.println("Solved score: " + solution.getScore());
//    }
//
//}
