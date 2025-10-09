package com.timetable.problem_solver.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Table(name = "user")
@Entity
@Setter
@Getter
public class User extends AuditorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fullName", nullable = false)
    private String fullName;

    @Column(name = "mobile", length = 15, nullable = false, unique = true)
    private String mobile;

    @Column(name = "email_id", length = 80, nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @ManyToOne
    @JoinColumn(name = "role_id_fk", referencedColumnName = "id")
    private Role role;

    @Column(name = "is_active", nullable = false)
    @ColumnDefault("1")
    private boolean isActive;

    @Column(name = "web_login", nullable = false)
    @ColumnDefault("0")
    private boolean webLogin;

    @Column(name = "mobile_login", nullable = false)
    @ColumnDefault("0")
    private boolean mobileLogin;

    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;

    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Column(name = "otp", length = 8)
    private String otp;

    @Column(name = "otp_req_time")
    private LocalDateTime otpRequestTime;

    @Column(name = "is_otp_request", nullable = false)
    @ColumnDefault("0")
    private boolean isOtpRequested;


}
