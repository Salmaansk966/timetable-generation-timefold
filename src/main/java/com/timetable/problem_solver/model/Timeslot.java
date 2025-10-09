//package com.timetable.problem_solver.model;
//
//
//import com.fasterxml.jackson.annotation.JsonFormat;
//import jakarta.persistence.*;
//import org.hibernate.annotations.CreationTimestamp;
//
//import java.sql.Timestamp;
//import java.time.DayOfWeek;
//import java.time.LocalTime;
//
//@Entity
//@Table(name = "timeslot")
//public class Timeslot {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Integer id;
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "day_of_week")
//    private DayOfWeek dayOfWeek;
//
//    @Column(name = "start_time")
//    private LocalTime startTime;
//
//    @Column(name = "end_time")
//    private LocalTime endTime;
//
//    @CreationTimestamp
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    @Column(name = "created_at")
//    private Timestamp createdAt;
//
//    public Timeslot(Integer id, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
//        this.id = id;
//        this.dayOfWeek = dayOfWeek;
//        this.startTime = startTime;
//        this.endTime = endTime;
//    }
//
//    public Timeslot(){}
//
//    public Integer getId() {
//        return id;
//    }
//
//    public void setId(Integer id) {
//        this.id = id;
//    }
//
//    public DayOfWeek getDayOfWeek() {
//        return dayOfWeek;
//    }
//
//    public void setDayOfWeek(DayOfWeek dayOfWeek) {
//        this.dayOfWeek = dayOfWeek;
//    }
//
//    public LocalTime getStartTime() {
//        return startTime;
//    }
//
//    public void setStartTime(LocalTime startTime) {
//        this.startTime = startTime;
//    }
//
//    public LocalTime getEndTime() {
//        return endTime;
//    }
//
//    public void setEndTime(LocalTime endTime) {
//        this.endTime = endTime;
//    }
//
//    public Timestamp getCreatedAt() {
//        return createdAt;
//    }
//
//    public void setCreatedAt(Timestamp createdAt) {
//        this.createdAt = createdAt;
//    }
//
//    public boolean isFirstSlotOfDay() {
//        return startTime.equals(LocalTime.of(8, 30)); // or your actual first slot time
//    }
//}
