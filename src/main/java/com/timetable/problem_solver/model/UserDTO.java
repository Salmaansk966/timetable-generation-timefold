package com.timetable.problem_solver.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private Long userId;
    private Long roleId;
    private String email;
    private String mobile;
    private String fullName;
    private AccountType accountType;
    private PlatformType platform;
}
