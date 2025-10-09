package com.timetable.problem_solver.model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Table(name = "section")
@Entity
@Setter
@Getter
public class Section extends AuditorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "section_name")
    private String sectionName;

    @Column(name = "is_active", nullable = false)
    @ColumnDefault("1")
    private boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_master_id_fk", referencedColumnName = "id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ClassMaster classMaster;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_timing_id", referencedColumnName = "id", nullable = true)
    private SchoolTiming schoolTimings;

    @Override
    public String toString(){
        return this.sectionName;
    }
}
