package com.timetable.problem_solver.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
public enum Title {
    MR("Mr."),
    MRS("Mrs."),
    MISS("Miss."),
    MASTER("Master.");
    private final String value;

    Title(String value) {
        this.value = value;
    }

    public static Title convert(String value) {
        return Objects.isNull(value) ? null : Arrays.stream(Title.values())
                .filter(title -> title.getValue().equals(value))
                .findFirst().orElseThrow(() -> new RuntimeException("Unknown Title"));
    }

    public static String value(Title title) {
        if (Objects.isNull(title)) {
            return null;
        }
        return title.value;
    }

    public static List<String> toList() {
        return Arrays.stream(Title.values()).map(Title::getValue).toList();
    }
}
