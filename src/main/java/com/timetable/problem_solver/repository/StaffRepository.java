package com.timetable.problem_solver.repository;

import com.timetable.problem_solver.model.Staff;
import com.timetable.problem_solver.model.TeacherSubjectProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long>, JpaSpecificationExecutor<Staff> {

    @Query(value = """
    SELECT sw.id AS teacherId,
           s.first_name AS firstName,
           s.middle_name AS midName,
           s.last_name AS lastName,
           ts.subject_id_fk AS subjectId,
           sub.subject_name AS subjectName
    FROM staff_work sw
    JOIN staff s ON s.id = sw.staff_id_fk
    JOIN teacher_subject ts ON ts.teacher_id_fk = sw.id
    JOIN subject sub ON sub.id = ts.subject_id_fk
    WHERE sw.is_teach_staff = true AND sw.is_resign = false
""", nativeQuery = true)
    List<TeacherSubjectProjection> findTeacherSubjectList();


}
