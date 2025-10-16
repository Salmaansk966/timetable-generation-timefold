package com.timetable.problem_solver.controller;

import ai.timefold.solver.core.api.score.analysis.ScoreAnalysis;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.solver.ScoreAnalysisFetchPolicy;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;
import com.timetable.problem_solver.controller.exception.TimetableSolverException;
import com.timetable.problem_solver.model.*;
import com.timetable.problem_solver.repository.*;
import com.timetable.problem_solver.service.SolverService;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/timetable")
public class TimetableController {

    private static final Logger logger = LoggerFactory.getLogger(TimetableController.class);

    public TimetableController(SolverService solverService, SolutionManager<TimeFoldTimetable, HardSoftScore> solutionManager, PeriodTimingsRepository timeSlotRepository, SubjectRepository subjectRepository, StaffRepository teacherRepository, StaffWorkRepository staffWorkRepository, SectionRepository groupRepository, SchoolTimingRepository schoolTimingRepository, SectionSubjectMappingRepository sectionSubjectMappingRepository) {
        this.solverService = solverService;
        this.solutionManager = solutionManager;
        this.timeSlotRepository = timeSlotRepository;
        this.subjectRepository = subjectRepository;
        this.teacherRepository = teacherRepository;
        this.staffWorkRepository = staffWorkRepository;
        this.groupRepository = groupRepository;
        this.schoolTimingRepository = schoolTimingRepository;
        this.sectionSubjectMappingRepository = sectionSubjectMappingRepository;
    }

//    private final TimetableService timetableService;
    private final SolverService solverService;
    private final SolutionManager<TimeFoldTimetable, HardSoftScore> solutionManager;

    private final PeriodTimingsRepository timeSlotRepository;
    private final SubjectRepository subjectRepository;
    private final StaffRepository teacherRepository;
    private final StaffWorkRepository staffWorkRepository;
    private final SectionRepository groupRepository;
    private final SchoolTimingRepository schoolTimingRepository;
    private final SectionSubjectMappingRepository sectionSubjectMappingRepository;

    // TODO: Without any "time to live", the map may eventually grow out of memory.
    @Getter
    private final ConcurrentMap<String, Job> jobIdToJob = new ConcurrentHashMap<>();

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String solve(@RequestBody TimeFoldTimetable problem) {
        try {
//        problem.fillFreePeriods();
            String jobId = UUID.randomUUID().toString();
            jobIdToJob.put(jobId, Job.ofTimetable(problem));
            
            // Get the current solver manager (with latest constraint settings)
            SolverManager<TimeFoldTimetable, String> solverManager = solverService.getSolverManager();
            if (solverManager == null) {
                throw new RuntimeException("Solver manager is not initialized. Please check the application logs.");
            }
            
            solverManager.solveAndListen(jobId, problem, solution -> {
                try {
                    jobIdToJob.put(jobId, Job.ofTimetable(solution));
                } catch (Exception e) {
                    logger.error("Error updating solution for job {}: {}", jobId, e.getMessage(), e);
                    jobIdToJob.put(jobId, Job.ofException(e));
                }
            });
            
            return jobId;
        } catch (Exception e) {
            logger.error("Failed to start solving: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to start solving: " + e.getMessage(), e);
        }
    }

    @GetMapping
    public Collection<String> list() {
        return jobIdToJob.keySet();
    }


    @GetMapping(value = "/{jobId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TimeFoldTimetable getTimeTable(@PathVariable("jobId") String jobId) {
        TimeFoldTimetable timetable = getTimetableAndCheckForExceptions(jobId);
        SolverManager<TimeFoldTimetable, String> solverManager = solverService.getSolverManager();
        SolverStatus solverStatus = solverManager.getSolverStatus(jobId);
        timetable.setSolverStatus(solverStatus);
        return timetable;
    }

    @GetMapping(value = "/{jobId}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public TimeFoldTimetable getStatus(@PathVariable("jobId") String jobId) {
        TimeFoldTimetable timetable = getTimetableAndCheckForExceptions(jobId);
        SolverManager<TimeFoldTimetable, String> solverManager = solverService.getSolverManager();
        SolverStatus solverStatus = solverManager.getSolverStatus(jobId);
        return new TimeFoldTimetable(timetable.getScore());
    }

    @GetMapping("/prepare/problem")
    private TimeFoldTimetable prepareProblem() {
        List<SchoolTiming> schoolTimingList = schoolTimingRepository.findByIsActiveTrue();

        List<TimeFoldLesson> lessons = new ArrayList<>();
        AtomicInteger lessonIdCounter = new AtomicInteger(1);
        List<TimeFoldTimeslot> allTimeSlots = new ArrayList<>();
        AtomicInteger timeslotCounter = new AtomicInteger(1);

        List<DayOfWeek> workingDays = List.of(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY
        );

        for (SchoolTiming timing : schoolTimingList) {
            // 1️⃣ Fetch base periods (no day info)
            List<PeriodTimings> periodList = timeSlotRepository
                    .findBySchoolTimingsIdAndIsPeriodTrueAndIsActiveTrue(timing.getId());

            // 2️⃣ Expand periods for each day
            List<TimeFoldTimeslot> currentPeriodsTimeSlots = new ArrayList<>();
            for (DayOfWeek day : workingDays) {
                for (PeriodTimings period : periodList) {
                    TimeFoldTimeslot timeslot = new TimeFoldTimeslot(
                            timeslotCounter.getAndIncrement(),
                            day,
                            period.getFromTime(),
                            period.getToTime(),
                            timing
                    );
                    currentPeriodsTimeSlots.add(timeslot);
                }
            }

            // 3️⃣ Fetch all sections, subjects, teachers
//            List<Subject> subjects = subjectRepository.findByIsActiveTrue();
            List<Section> sections = groupRepository.findBySchoolTimingsIdAndIsActiveTrue(timing.getId());
            List<TeacherSubjectProjection> teacherSubjectList = teacherRepository.findTeacherSubjectList();

            Map<String, List<TeacherSubjectProjection>> subjectTeacherMap =
                    teacherSubjectList.stream().collect(Collectors.groupingBy(TeacherSubjectProjection::getSubjectName));

            Random random = new Random();

            // 4️⃣ Generate random lessons
            for (Section section : sections) {
                List<SectionSubjectMapping> subjectMappings = sectionSubjectMappingRepository.findBySectionId(section.getId());
                for (TimeFoldTimeslot ignored : currentPeriodsTimeSlots) {

                    Subject subject = subjectMappings.get(random.nextInt(subjectMappings.size())).getSubject();
                    List<TeacherSubjectProjection> teachers = subjectTeacherMap.get(subject.getSubjectName());

                    if (teachers == null || teachers.isEmpty()) continue;

                    TeacherSubjectProjection teacher = teachers.get(random.nextInt(teachers.size()));

                    Staff staff = new Staff();
                    staff.setId(teacher.getTeacherId());
                    staff.setFirstName(teacher.getFirstName());
                    staff.setLastName(teacher.getLastName());

                    lessons.add(new TimeFoldLesson(lessonIdCounter.getAndIncrement(), subject, staff, section));
                }
            }
            allTimeSlots.addAll(currentPeriodsTimeSlots);
        }

        return new TimeFoldTimetable(
                allTimeSlots,
                lessons,
                null
        );
    }


    @PutMapping(value = "/analyze", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @RegisterReflectionForBinding()
    public ScoreAnalysis<HardSoftScore> analyze(@RequestBody TimeFoldTimetable problem,
                                                @RequestParam(name = "fetchPolicy", required = false) ScoreAnalysisFetchPolicy fetchPolicy) {
        return fetchPolicy == null ? solutionManager.analyze(problem) : solutionManager.analyze(problem, fetchPolicy);
    }

//    @PostMapping("/migrate")
//    public String migrateData() {
//        Random random = new Random();
//        // 1. Create timeslots
//        List<Timeslot> timeslots = new ArrayList<>();
//        for (DayOfWeek day : DayOfWeek.values()) {
//            if (day.getValue() > 5) continue; // Monday to Friday
//            for (int h = 9; h <= 16; h++) { // 9-16
//                if (h == 13) continue; // break 1 hour
//                timeslots.add(new Timeslot(null, day, LocalTime.of(h,0), LocalTime.of(h+1,0)));
//            }
//        }
//
//        // 2. Create rooms (Class 1-5)
//        List<Room> rooms = new ArrayList<>();
//        for (int i=1;i<=5;i++){
//            rooms.add(new Room(null, "Class " + i));
//        }
//
//        // 3. Create lessons for each class, 5 subjects each
//        List<com.timetable.problem_solver.sbioa.model.Subject> subjects = subjectRepository.findAll();
//        List<Staff> teachers = teacherRepository.findAll();
//        List<Section> groups = groupRepository.findAll();
//        System.out.println("subject count"+ subjects.size());
//        System.out.println("teacher count"+ teachers.size());
//        System.out.println("group count"+ groups.size());
//
//        List<TimeFoldLesson> lessons = new ArrayList<>();
//        for (Section group : groups) {
//            for (int t = 0; t < timeslots.size(); t++) {
//                // pick subject/teacher dynamically
//                int idx = random.nextInt(subjects.size());
//                Subject subject = subjects.get(idx);
//                Staff teacher = teachers.get(idx);
//                lessons.add(new TimeFoldLesson(null, subject, teacher, group));
//            }
//        }
//        try {
////            timeSlotRepository.saveAll(timeslots);
////            lessonRepository.saveAll(lessons);
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        }
//        return "Success";
//    }

    @DeleteMapping(value = "/{jobId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TimeFoldTimetable terminateSolving(
            @PathVariable("jobId") String jobId) {
        // TODO: Replace with .terminateEarlyAndWait(... [, timeout]); see https://github.com/TimefoldAI/timefold-solver/issues/77
        SolverManager<TimeFoldTimetable, String> solverManager = solverService.getSolverManager();
        solverManager.terminateEarly(jobId);
        return getTimeTable(jobId);
    }

//    @PostMapping("/solve")
//    private String solve() {
//        timetableService.solve();
//        return "Solver started. Check logs for updates.";
//    }

    private record Job(TimeFoldTimetable timetable, Throwable exception) {

        static Job ofTimetable(TimeFoldTimetable timetable) {
            return new Job(timetable, null);
        }

        static Job ofException(Throwable error) {
            return new Job(null, error);
        }
    }

    private TimeFoldTimetable getTimetableAndCheckForExceptions(String jobId) {
        Job job = jobIdToJob.get(jobId);
        if (job == null) {
            throw new TimetableSolverException(jobId, HttpStatus.NOT_FOUND, "No timetable found.");
        }
        if (job.exception != null) {
            throw new TimetableSolverException(jobId, job.exception);
        }
        return job.timetable;
    }

}
