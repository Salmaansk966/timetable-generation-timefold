package com.timetable.problem_solver.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalTime;

@Entity
@Table(name = "school_timings")
@Getter
@Setter
public class SchoolTiming extends AuditorEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_time_type", nullable = false, unique = true, length = 40)
    private String schoolTimeType;

    @Column(name = "from_time", nullable = false)
    private LocalTime fromTime;

    @Column(name = "to_time", nullable = false)
    private LocalTime toTime;

    @Column(name = "time_string")
    private String timeString;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accounting_year_id_fk", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private AccountingYear accountingYear;

    @Column(name = "is_active", nullable = false)
    @ColumnDefault("1")
    private boolean isActive;
}
