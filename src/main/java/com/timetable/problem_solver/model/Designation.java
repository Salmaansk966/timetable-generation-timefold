package com.timetable.problem_solver.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Table(name = "designation")
@Entity
@Setter
@Getter
public class Designation extends AuditorEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "designation_name", nullable = false)
    private String designationName;

    @ManyToOne
    @JoinColumn(name = "department_id_fk", referencedColumnName = "id")
    private Department department;

    @Column(name = "is_active", nullable = false)
    @ColumnDefault("1")
    private boolean isActive;

    @Override
    public String toString(){
        return this.designationName;
    }
}
