package com.timetable.problem_solver.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
public enum Gender {
    MALE("Male"),
    FEMALE("Female"),
    OTHERS("Others");
    private final String value;

    Gender(String value) {
        this.value = value;
    }

    public static Gender convert(String value) {
        return Objects.isNull(value) ? null : Arrays.stream(Gender.values())
                .filter(gender -> gender.getValue().equals(value))
                .findFirst().orElseThrow(() -> new RuntimeException("Unknown Gender"));
    }

    public static String value(Gender gender) {
        if (Objects.isNull(gender)) {
            return null;
        }
        return gender.value;
    }

    public static List<String> toList() {
        return Arrays.stream(Gender.values()).map(Gender::getValue).toList();
    }
}
