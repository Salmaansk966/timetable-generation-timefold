package com.timetable.problem_solver.repository;


import com.timetable.problem_solver.model.PeriodTimings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PeriodTimingsRepository extends JpaRepository<PeriodTimings, Integer> {
    List<PeriodTimings> findBySchoolTimingsId(Long id);

    List<PeriodTimings> findBySchoolTimingsIdAndIsPeriodTrueAndIsActiveTrue(Long id);
}
