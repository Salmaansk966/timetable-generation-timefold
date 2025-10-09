package com.timetable.problem_solver.repository;


import com.timetable.problem_solver.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SectionRepository extends JpaRepository<Section,Long> {
    List<Section> findBySchoolTimingsIdAndIsActiveTrue(Long id);
}
