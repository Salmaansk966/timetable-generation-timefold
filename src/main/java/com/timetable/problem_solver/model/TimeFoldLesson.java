package com.timetable.problem_solver.model;


import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import lombok.*;

import java.time.DayOfWeek;

@Setter
@Getter
@PlanningEntity
public class TimeFoldLesson {

    @PlanningId
    private Integer id;

    private Subject subject;

    private Staff teacher;

    private Section section;

    @PlanningVariable(valueRangeProviderRefs = "timeslotRange")
    private TimeFoldTimeslot timeslot;

    public TimeFoldLesson(Integer id, Subject subject, Staff teacher, Section studentGroup) {
        this.id = id;
        this.subject = subject;
        this.teacher = teacher;
        this.section = studentGroup;
    }

    public TimeFoldLesson(Integer id, Subject subject, Staff teacher, Section section, TimeFoldTimeslot timeFoldTimeslots) {
        this.id = id;
        this.subject = subject;
        this.teacher = teacher;
        this.section = section;
        this.timeslot = timeFoldTimeslots;
    }

    public TimeFoldLesson() {
    }
}
