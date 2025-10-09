package com.timetable.problem_solver.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

@Getter
public enum DifficultyLevel {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High");
    private final String value;

    DifficultyLevel(String value){
        this.value = value;
    }

    public static DifficultyLevel convert(String value){
        return Objects.isNull(value) ? null : Arrays.stream(DifficultyLevel.values())
                .filter(difficultyLevel -> difficultyLevel.getValue().equalsIgnoreCase(value))
                .findFirst().orElseThrow(()->new RuntimeException("Subject priority should be: Low,Medium or High"));
    }

    public static String value(DifficultyLevel difficultyLevel){
        if(Objects.isNull(difficultyLevel)){
            return null;
        }
        return difficultyLevel.value;
    }
}
