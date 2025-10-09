package com.timetable.problem_solver.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
public enum JobType {
    PERMANENT("Permanent"),
    PART_TIME("Part-Time");
    private final String value;

    JobType(String value){
        this.value = value;
    }

    public static JobType convert(String value){
        return Objects.isNull(value) ? null : Arrays.stream(JobType.values())
                .filter(jobType -> jobType.getValue().equals(value))
                .findFirst().orElseThrow(()->new RuntimeException("Unknown Job Type"));
    }

    public static String value(JobType jobType){
        if(Objects.isNull(jobType)){
            return null;
        }
        return jobType.value;
    }

    public static List<String> toList(){
        return Arrays.stream(JobType.values()).map(JobType::getValue).toList();
    }
}
