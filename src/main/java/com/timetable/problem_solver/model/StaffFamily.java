package com.timetable.problem_solver.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "staff_family")
@Getter
@Setter
public class StaffFamily {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "staff_id_fk")
    private Staff staff;
    @Enumerated(EnumType.STRING)
    private MemberType memberType;
    @Column(length = 60, nullable = false)
    private String firstName;
    @Column(length = 60)
    private String middleName;
    @Column(length = 60, nullable = false)
    private String lastName;
    @Column(length = 80)
    private String emailId;
    @Column(length = 15, nullable = false)
    private String mobile;
    @Column(length = 120, nullable = false)
    private String workplace;
    private String occupation;
    @ColumnDefault("0.0")
    @Column(name = "ann_income")
    private Double annualIncome;
    @Column(name = "is_emergency_ctn")
    @ColumnDefault("0")
    private boolean isEmergencyContact;
    @Embedded
    private Address address;
}
