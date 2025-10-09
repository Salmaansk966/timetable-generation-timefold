package com.timetable.problem_solver.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.time.Period;

@Entity
@Table(name = "staff_exp")
@Getter
@Setter
//@AuditAnno(excludeList = {"id","staff"}, embeddableList = {"address"})
public class StaffExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "staff_id_fk")
    private Staff staff;
    @Column(length = 120, nullable = false)
    private String workplace;
    @Column(length = 60, nullable = false)
    private String designation;
    @Column(nullable = false)
    private LocalDate fromDate;
    @Column(nullable = false)
    private LocalDate toDate;
    @Column(name = "wrk_dec")
    private String workDescription;
    @Column(name = "exp_years")
    @ColumnDefault("0.0")
    private Double experienceYears;
    @Embedded
    private Address address;

    @PrePersist
    @PreUpdate
    public void setExpYears(){
        Period diff = Period.between(fromDate,toDate);
        this.experienceYears = (double) diff.getYears();
        this.experienceYears += diff.getMonths()/10.0;
    }
}
