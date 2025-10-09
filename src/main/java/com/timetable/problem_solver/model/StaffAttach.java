package com.timetable.problem_solver.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@DiscriminatorValue("STAFF_BASIC")
public class StaffAttach extends Attachment{
}
