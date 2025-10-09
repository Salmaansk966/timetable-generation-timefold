package com.timetable.problem_solver.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
public enum PayMode {
    CASH("Cash"),
    CHEQUE("Cheque"),
    DEMAND_DRAFT("Demand Draft"),
    BANK("Bank"),
    HOLD("Hold"),
    RTGS("RTGS");
    private final String value;

    PayMode(String value){
        this.value = value;
    }

    public static PayMode convert(String value){
        return Objects.isNull(value) ? null : Arrays.stream(PayMode.values())
                .filter(payMode -> payMode.getValue().equals(value))
                .findFirst().orElseThrow(()->new RuntimeException("Unknown Pay Mode"));
    }

    public static String value(PayMode payMode){
        if(Objects.isNull(payMode)){
            return null;
        }
        return payMode.value;
    }

    public static List<String> toList(){
        return Arrays.stream(PayMode.values()).map(PayMode::getValue).toList();
    }
}
