package com.timetable.problem_solver.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
public enum MartialStatus {
    MARRIED("Married"),
    UN_MARRIED("Un-Married"),
    DIVORCED("Divorced"),
    WIDOW("Widow");

    private final String value;

    MartialStatus(String value) {
        this.value = value;
    }

    public static MartialStatus convert(String value) {
        return Objects.isNull(value) ? null : Arrays.stream(MartialStatus.values())
                .filter(martialStatus -> martialStatus.getValue().equals(value))
                .findFirst().orElseThrow(() -> new RuntimeException("Unknown Martial Status"));
    }

    public static String value(MartialStatus martialStatus) {
        if (Objects.isNull(martialStatus)) {
            return null;
        }
        return martialStatus.value;
    }

    public static List<String> toList() {
        return Arrays.stream(MartialStatus.values()).map(MartialStatus::getValue).toList();
    }
}
