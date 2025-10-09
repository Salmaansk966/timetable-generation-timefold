package com.timetable.problem_solver.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeFoldTimeslot {

    private Integer id;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private SchoolTiming timing;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeFoldTimeslot that = (TimeFoldTimeslot) o;
        return Objects.equals(id, that.id) && 
               dayOfWeek == that.dayOfWeek && 
               Objects.equals(startTime, that.startTime) && 
               Objects.equals(endTime, that.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dayOfWeek, startTime, endTime);
    }
}
