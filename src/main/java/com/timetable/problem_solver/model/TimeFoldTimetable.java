package com.timetable.problem_solver.model;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.solver.SolverStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@PlanningSolution
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TimeFoldTimetable {

    @ValueRangeProvider(id = "timeslotRange")
    @ProblemFactCollectionProperty
    private List<TimeFoldTimeslot> timeslots;

    @PlanningEntityCollectionProperty
    private List<TimeFoldLesson> lessons;

    // Ignored by Timefold, used by the UI to display solve or stop solving button
    private SolverStatus solverStatus;

    @PlanningScore
    private HardSoftScore score;

    public TimeFoldTimetable(HardSoftScore score) {
        this.score = score;
    }

    public TimeFoldTimetable(List<TimeFoldTimeslot> timeslots, List<TimeFoldLesson> lessons, HardSoftScore score) {
        this.timeslots = timeslots;
        this.lessons = lessons;
        this.score = score;
    }
}
