//package com.timetable.problem_solver.model;
//
//import com.fasterxml.jackson.annotation.JsonFormat;
//import jakarta.persistence.*;
//import org.hibernate.annotations.CreationTimestamp;
//
//import java.sql.Timestamp;
//
//
//@Entity
//@Table(name = "group_or_class")
//public class ClassOrGroup {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "id")
//    private Integer id;
//
//    @Column(name = "name")
//    private String className;
//
//    @CreationTimestamp
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    @Column(name = "created_at")
//    private Timestamp createdAt;
//
//    public ClassOrGroup() {
//    }
//
//    public ClassOrGroup(Integer id, String className) {
//        this.id = id;
//        this.className = className;
//    }
//
//    public String getClassName() {
//        return className;
//    }
//
//    public void setClassName(String className) {
//        this.className = className;
//    }
//
//    public Integer getId() {
//        return id;
//    }
//
//    public void setId(Integer id) {
//        this.id = id;
//    }
//}
