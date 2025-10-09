package com.timetable.problem_solver.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "staff_edu")
@Getter
@Setter
//@AuditAnno(excludeList = {"id","staff"}, embeddableList = {"address"})
public class StaffEducation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "staff_id_fk")
    private Staff staff;
    @Column(name = "quali_type")
    @Enumerated(EnumType.STRING)
    private QualificationType qualificationType;
    @Column(name = "qualification", length = 20, nullable = false)
    private String qualification;
    @Column(length = 60, nullable = false)
    private String institute;
    @Column(length = 60, nullable = false)
    private String courseName;
    @Column(length = 60, nullable = false)
    private String specialization;
    @Column(length = 20)
    private String medium;
    @Column(name = "pass_percent", nullable = false)
    @ColumnDefault("0.0")
    private Double passPercentage;
    @Column(nullable = false)
    @ColumnDefault("0.0")
    private Double classSecured;
    @Column(name = "pass_year", nullable = false)
    private Integer passOutYear;
    @Embedded
    private Address address;
}
