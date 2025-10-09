package com.timetable.problem_solver.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "period_timings")
@Getter
@Setter
@NoArgsConstructor
public class PeriodTimings extends AuditorEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "period_name", nullable = false, length = 40)
    private String periodName;

    @Column(name = "from_time", nullable = false)
    private LocalTime fromTime;

    @Column(name = "to_time", nullable = false)
    private LocalTime toTime;

    @Column(name = "is_period", nullable = false)
    @ColumnDefault("0")
    private boolean isPeriod;

    @Column(name = "period_order", nullable = false)
    private Integer periodOrder;

    @Column(name = "is_active", nullable = false)
    @ColumnDefault("1")
    private boolean isActive;

    @Column(name = "period_time_string")
    private String periodTimeString;

    @ManyToOne
    @JoinColumn(name = "school_timing_id_fk")
    private SchoolTiming schoolTimings;

    public PeriodTimings(Integer id, LocalTime startTime, LocalTime endTime) {
        this.id = id;
        this.fromTime = startTime;
        this.toTime = endTime;
    }
}
