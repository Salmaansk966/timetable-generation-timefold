package com.timetable.problem_solver.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "staff")
@Getter
@Setter
//@AuditAnno(excludeList = {"id","staffAttaches","staffWork","staffEducations","staffExperiences"
//,"staffFamilies", "staffServiceRecords","profileBlobName","user"}, embeddableList = {"address"})
public class Staff extends AuditorEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 60)
    private String firstName;

    @Column(name = "middle_name", length = 60)
    private String middleName;

    @Column(name = "last_name", nullable = false, length = 60)
    private String lastName;

    @Column(name = "mobile", length = 15)
    private String mobile;

    @Column(name = "email_id", length = 80)
    private String email;

    @Column(name = "is_active", nullable = false)
    @ColumnDefault("1")
    private boolean isActive;

    @Column(name = "profile_blob", length = 80)
    private String profileBlobName;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_grp", nullable = false)
    private BloodGroup bloodGroup;

    @Column(name = "dob", nullable = false)
    private LocalDate dateOfBirth;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Title title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "religion_id_fk")
    private Religion religion;

    @Column(length = 5)
    private String community;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MartialStatus martialStatus;

    @Column(length = 40)
    private String oasisId;

    @Column(name = "pan_no", length = 10, nullable = false)
    private String pan;

    @Column(name = "passport_no", length = 8)
    private String passport;

    @Column(name = "passport_exp")
    private LocalDate passportExpiry;

    @Column(name = "aadhar_no", length = 12)
    private String aadhar;

    @Column(name = "pf_no", length = 22)
    private String pf;

    @Column(name = "uan_no", length = 12)
    private String uan;

    private LocalDate pfDate;

    private boolean pfStatus;

    private String trustNo;

    private String esi;

    private LocalDate esiDate;

    private boolean esiStatus;

    @Enumerated(EnumType.STRING)
    private PayMode payMode;

    private boolean payrollStatus;

    @Column(name = "bank_acc_no", length = 16)
    private String bankAccountNo;

    @Column(name = "bank_name", length = 60)
    private String bankName;

    @Column(name = "bank_branch", length = 60)
    private String bankBranch;

    @Column(name = "bank_ifsc", length = 11)
    private String bankIfscCode;

    @OneToMany(cascade = {CascadeType.PERSIST,CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(name = "staff_attach", joinColumns = @JoinColumn(name = "staff_id_fk"),
            inverseJoinColumns = @JoinColumn(name = "attach_id_fk"))
    private Set<StaffAttach> staffAttaches = new HashSet<>();

    @OneToOne(mappedBy = "staff", fetch = FetchType.LAZY)
    private StaffWork staffWork;

    @OneToMany(mappedBy = "staff")
    private List<StaffEducation> staffEducations = new ArrayList<>();

    @OneToMany(mappedBy = "staff")
    private List<StaffExperience> staffExperiences = new ArrayList<>();

    @OneToMany(mappedBy = "staff")
    private List<StaffFamily> staffFamilies = new ArrayList<>();

    @OneToMany(mappedBy = "staff")
    private List<StaffServiceRecord> staffServiceRecords = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id_fk")
    private User user;

    public void addAttach(StaffAttach staffAttach){
        this.staffAttaches.add(staffAttach);
    }

    public void removeAttach(String generatedName){
        this.staffAttaches.removeIf(staffAttach -> staffAttach.getGeneratedName().equals(generatedName));
    }
}
