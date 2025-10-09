package com.timetable.problem_solver.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Table(name = "class_master")
@Entity
@Setter
@Getter
public class ClassMaster extends AuditorEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_name")
    private String className;

    @Column(name = "class_weight")
    private int classWeight;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "class_code_id_fk", referencedColumnName = "id")
    private ClassCode classCode;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "class_group_id_fk", referencedColumnName = "id")
    private ClassGroup classGroup;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "standard_classification_id_fk", referencedColumnName = "id")
    private StandardClassification standardClassification;

    @Column(name = "is_active", nullable = false)
    @ColumnDefault("1")
    private boolean isActive;

    @Column(name = "class_order", nullable = false, unique = true)
    private Long classOrder;
}
