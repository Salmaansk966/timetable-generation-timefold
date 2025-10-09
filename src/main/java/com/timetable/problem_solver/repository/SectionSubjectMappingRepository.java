package com.timetable.problem_solver.repository;

import com.timetable.problem_solver.model.SectionSubjectMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectionSubjectMappingRepository extends JpaRepository<SectionSubjectMapping, Integer> {
    List<SectionSubjectMapping> findBySectionId(Long id);
}
