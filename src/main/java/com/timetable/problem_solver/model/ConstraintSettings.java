package com.timetable.problem_solver.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing constraint settings stored in the database.
 * Each constraint can be enabled/disabled dynamically without application restart.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "constraint_settings")
public class ConstraintSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "constraint_name", nullable = false, unique = true)
    private String constraintName;

    @Column(name = "constraint_weight")
    private int constraintWeight;

    @Column(name = "constraint_type")
    private String constraintType;

    @Column(name = "enable_flag")
    private boolean enableFlag;

    @Column(name = "description")
    private String description;
}
