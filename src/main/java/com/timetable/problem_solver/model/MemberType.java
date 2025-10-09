package com.timetable.problem_solver.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
public enum MemberType {
    FATHER("Father"),
    MOTHER("Mother"),
    HUSBAND("Husband"),
    SPOUSE("Spouse"),
    SON("Son"),
    DAUGHTER("Daughter");
    private final String value;

    MemberType(String value){
        this.value = value;
    }

    public static MemberType convert(String value){
        return Objects.isNull(value) ? null : Arrays.stream(MemberType.values())
                .filter(memberType -> memberType.getValue().equals(value))
                .findFirst().orElseThrow(()->new RuntimeException("Unknown Member Type"));
    }

    public static String value(MemberType memberType){
        if(Objects.isNull(memberType)){
            return null;
        }
        return memberType.value;
    }

    public static List<String> toList(){
        return Arrays.stream(MemberType.values()).map(MemberType::getValue).toList();
    }
}
