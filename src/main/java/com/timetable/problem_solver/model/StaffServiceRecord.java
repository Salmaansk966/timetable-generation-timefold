package com.timetable.problem_solver.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;

@Entity
@Table(name = "staff_srv_rec")
@Getter
@Setter
//@AuditAnno(excludeList = {"id","staff"}, embeddableList = {"address"})
public class StaffServiceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "staff_id_fk")
    private Staff staff;
    @Column(length = 120, nullable = false)
    private String workplace;
    @Column(nullable = false)
    private LocalDate appointedOn;
    @ColumnDefault("0.0")
    private Double payScale;
    @ColumnDefault("0.0")
    private Double basicPay;
    @ColumnDefault("0.0")
    private Double grossPay;
    @Embedded
    private Address address;
}
