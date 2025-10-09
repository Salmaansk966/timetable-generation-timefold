package com.timetable.problem_solver.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "entity_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
public class Attachment extends AuditorEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,length = 120)
    private String fileName;

    @Column(name = "gen_name", nullable = false, unique = true, length = 80)
    private String generatedName;

    @Column(nullable = false, length = 60)
    private String fileType;
}
