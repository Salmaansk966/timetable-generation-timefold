package com.timetable.problem_solver.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Table(name="accounting_year")
@Entity
@Getter
@Setter
public class AccountingYear extends AuditorEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    @Column(name = "year_string", nullable = false)
    private String yearString;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "accounting_year_order", nullable = false, unique = true)
    private Long accountingYearOrder;

}