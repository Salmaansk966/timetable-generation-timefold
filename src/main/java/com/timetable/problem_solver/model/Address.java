package com.timetable.problem_solver.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class Address {

    private String street;

    @Column(length = 80)
    private String area;

    @Column(length = 60)
    private String state;

    @Column(length = 60)
    private String district;

    @Column(length = 80)
    private String block;

    @Column(length = 15)
    private String pinCode;

    @Column(length = 60)
    private String country;
}
