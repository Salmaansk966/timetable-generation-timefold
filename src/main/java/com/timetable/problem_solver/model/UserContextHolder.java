package com.timetable.problem_solver.model;



public class UserContextHolder {

    private UserContextHolder() {
    }

    private static final ThreadLocal<UserDTO> USER_CONTEXT = new ThreadLocal<>();

    public static void setUserDto(UserDTO userContextDTO) {
        USER_CONTEXT.set(userContextDTO);
    }

    public static void clear() {
        USER_CONTEXT.remove();
    }

    public static UserDTO getUserDto() {
        return USER_CONTEXT.get();
    }
}