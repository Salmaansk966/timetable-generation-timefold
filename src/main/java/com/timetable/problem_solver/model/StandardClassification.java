package com.timetable.problem_solver.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Table(name = "standard_classification")
@Entity
@Setter
@Getter
public class StandardClassification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "standard_classification_name")
    private String standardClassificationName;

    @Column(name = "is_active", nullable = false)
    @ColumnDefault("1")
    private boolean isActive;
}
