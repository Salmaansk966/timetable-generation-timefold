package com.timetable.problem_solver.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public enum QualificationType {

    UG("Under Graduate");
    private final String value;

    public String getValue() {
        return value;
    }

    QualificationType(String value){
        this.value = value;
    }

    public static QualificationType convert(String value){
        return Objects.isNull(value) ? null : Arrays.stream(QualificationType.values())
                .filter(qualificationType -> qualificationType.getValue().equals(value))
                .findFirst().orElseThrow(()->new RuntimeException("Unknown Qualification Type"));
    }

    public static String value(QualificationType qualificationType){
        if(Objects.isNull(qualificationType)){
            return null;
        }
        return qualificationType.value;
    }

    public static List<String> toList(){
        return Arrays.stream(QualificationType.values()).map(QualificationType::getValue).toList();
    }
}
