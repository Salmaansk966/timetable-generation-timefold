package com.timetable.problem_solver.repository;

import com.timetable.problem_solver.model.StaffWork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffWorkRepository extends JpaRepository<StaffWork, Long> {
    List<StaffWork> findByIsTeachingStaffTrueAndIsResignedFalse();
}
