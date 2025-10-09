package com.timetable.problem_solver.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
public enum BloodGroup {
    A_POS("A+"),
    A_NEG("A-"),
    B_POS("B+"),
    B_NEG("B-"),
    O_POS("O+"),
    O_NEG("O-"),
    AB_POS("AB+"),
    AB_NEG("AB-");
    private final String value;

    BloodGroup(String value){
        this.value = value;
    }

    public static BloodGroup convert(String value){
        return Arrays.stream(BloodGroup.values()).filter(bloodGroup -> bloodGroup.getValue().equals(value))
                .findFirst().orElseThrow(()->new RuntimeException("Unknown Blood Group"));
    }

    public static String value(BloodGroup bloodGroup){
        if(Objects.isNull(bloodGroup)){
            return null;
        }
        return bloodGroup.value;
    }

    public static List<String> toList(){
        return Arrays.stream(BloodGroup.values()).map(BloodGroup::getValue).toList();
    }
}
