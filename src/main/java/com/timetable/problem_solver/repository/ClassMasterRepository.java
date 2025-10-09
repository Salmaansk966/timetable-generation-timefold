package com.timetable.problem_solver.repository;

import com.timetable.problem_solver.model.ClassMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ClassMasterRepository extends JpaRepository<ClassMaster, Long> {

}
