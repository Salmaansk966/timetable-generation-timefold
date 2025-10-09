package com.timetable.problem_solver.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "staff_work")
@Getter
@Setter
//@AuditAnno(excludeList = {"id"}, embeddableList = {"address"})
public class StaffWork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    @JoinColumn(name = "staff_id_fk")
    private Staff staff;
    @ManyToOne
    @JoinColumn(name = "designation_id_fk")
    private Designation designation;
    @Column(name = "emp_code", unique = true, length = 20, nullable = false)
    private String employeeCode;
    @Column(name = "doj", nullable = false)
    private LocalDate dateOfJoining;
    @Column(name = "contract_from")
    private LocalDate contractFrom;
    @Column(name = "contract_to")
    private LocalDate contractTo;
    @Column(name = "contract_exp")
    private LocalDate contractExpiry;
    @Column(name = "prob_from")
    private LocalDate probationFrom;
    @Column(name = "prob_to")
    private LocalDate probationTo;
    @Enumerated(EnumType.STRING)
    private JobType jobType;
    @Column(name = "signature_blob", length = 80, nullable = false)
    private String signatureBlobName;

    @ManyToOne
    @JoinColumn(name = "report_mgr_id_fk")
    private Staff reportingManager;

    @Column(name = "is_teach_staff", nullable = false)
    @ColumnDefault("0")
    private boolean isTeachingStaff;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "teacher_section", joinColumns = @JoinColumn(name = "teacher_id_fk"))
    private Set<TeacherSection> teacherSections = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "teacher_subject", joinColumns = @JoinColumn(name = "teacher_id_fk"))
    private Set<TeacherSubject> teacherSubjects = new HashSet<>();

    @Column(name = "is_resign", nullable = false)
    @ColumnDefault("0")
    private boolean isResigned;
    @Column(name = "resign_req_date")
    private LocalDate resignRequestDate;
    @Column(name = "last_wrk_date")
    private LocalDate lastWorkingDate;
    @Column(name = "last_wrk_time")
    private LocalTime lastWorkingTime;
    @Column(name = "leave_reason")
    private String leavingReason;

    public void updateSectionSubject(Collection<TeacherSubject> teacherSubjects){
        this.teacherSubjects.addAll(teacherSubjects);
        this.teacherSubjects.removeIf(teacherSubject -> !teacherSubjects.contains(teacherSubject));
    }

    public void updateSection(Collection<TeacherSection> teacherSections){
        this.teacherSections.addAll(teacherSections);
        this.teacherSections.removeIf(teacherSection -> !teacherSections.contains(teacherSection));
    }

    @Override
    public String toString(){
        return this.employeeCode;
    }
}
