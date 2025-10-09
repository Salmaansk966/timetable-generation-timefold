//package com.timetable.problem_solver.model;
//
//import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
//import ai.timefold.solver.core.api.domain.lookup.PlanningId;
//import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
//import com.fasterxml.jackson.annotation.JsonFormat;
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//import org.hibernate.annotations.CreationTimestamp;
//
//import java.sql.Timestamp;
//
//
//@PlanningEntity
//@Entity
//@Table(name = "lesson")
//@AllArgsConstructor
//public class Lesson {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @PlanningId
//    private Integer id;
//
//    @OneToOne
//    @JoinColumn(name = "subject_id")
//    private Subject subject;
//
//    @OneToOne
//    @JoinColumn(name = "teacher_id")
//    private Teacher teacher;
//
//    @OneToOne
//    @JoinColumn(name = "group_id")
//    private ClassOrGroup studentGroup;
//
//    @ManyToOne
//    @PlanningVariable(valueRangeProviderRefs = "timeslotRange")
//    private Timeslot timeslot;
//
//    @ManyToOne
////    @PlanningVariable(valueRangeProviderRefs = "roomRange")
//    private Room room;
//
//    @CreationTimestamp
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    @Column(name = "created_at")
//    private Timestamp createdAt;
//
//    public Timestamp getCreatedAt() {
//        return createdAt;
//    }
//
//    public void setCreatedAt(Timestamp createdAt) {
//        this.createdAt = createdAt;
//    }
//
//    public Lesson() {
//    }
//
//    public Lesson(Integer id, Subject subject, Teacher teacher, ClassOrGroup studentGroup) {
//        this.id = id;
//        this.subject = subject;
//        this.teacher = teacher;
//        this.studentGroup = studentGroup;
//    }
//
//    public Integer getId() {
//        return id;
//    }
//
//    public void setId(Integer id) {
//        this.id = id;
//    }
//
//    public ClassOrGroup getStudentGroup() {
//        return studentGroup;
//    }
//
//    public void setStudentGroup(ClassOrGroup studentGroup) {
//        this.studentGroup = studentGroup;
//    }
//
//    public Timeslot getTimeslot() {
//        return timeslot;
//    }
//
//    public void setTimeslot(Timeslot timeslot) {
//        this.timeslot = timeslot;
//    }
//
//    public Room getRoom() {
//        return room;
//    }
//
//    public void setRoom(Room room) {
//        this.room = room;
//    }
//
//    public Subject getSubject() {
//        return subject;
//    }
//
//    public void setSubject(Subject subject) {
//        this.subject = subject;
//    }
//
//    public Teacher getTeacher() {
//        return teacher;
//    }
//
//    public void setTeacher(Teacher teacher) {
//        this.teacher = teacher;
//    }
//}
