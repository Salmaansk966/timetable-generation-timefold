package com.timetable.problem_solver.repository;

import com.timetable.problem_solver.model.SchoolTiming;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchoolTimingRepository extends JpaRepository<SchoolTiming, Long> {
    List<SchoolTiming> findByIsActiveTrue();
}
