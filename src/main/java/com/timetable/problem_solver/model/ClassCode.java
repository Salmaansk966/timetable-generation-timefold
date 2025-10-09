package com.timetable.problem_solver.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Table(name = "class_code")
@Entity
@Setter
@Getter
@Data
public class ClassCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_code_name")
    private String classCodeName;

    @Column(name = "is_active", nullable = false)
    @ColumnDefault("1")
    private boolean isActive;
}
