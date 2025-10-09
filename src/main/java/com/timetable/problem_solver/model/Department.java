package com.timetable.problem_solver.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Table(name = "department")
@Entity
@Setter
@Getter
public class Department extends AuditorEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "department_name")
    private String departmentName;

    @Column(name = "is_active", nullable = false)
    @ColumnDefault("1")
    private boolean isActive;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id_fk", referencedColumnName = "id")
    private Staff staff;
}
