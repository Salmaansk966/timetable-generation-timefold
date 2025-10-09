package com.timetable.problem_solver.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

/**
 * @author Sekhar M
 */
@Table(name = "subject")
@Entity
@Setter
@Getter
public class Subject extends AuditorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subject_name", length = 100, nullable = false)
    private String subjectName;

    @Column(name = "subject_code", length = 50, nullable = false)
    private String subjectCode;

    @Column(name = "subject_alias", length = 50, nullable = false)
    private String subjectAlias;

    @Column(name = "subject_color", length = 50, nullable = false)
    private String subjectColor;

    @Column(name = "regional_subject", length = 100)
    private String regionalSubject;

    @Column(name = "is_active")
    @ColumnDefault("1")
    private Boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 50, nullable = false)
    private DifficultyLevel difficultyLevel;

    @Column(name = "is_practical")
    private Boolean isPractical;

    @Column(name = "is_theory")
    private Boolean isTheory;
}