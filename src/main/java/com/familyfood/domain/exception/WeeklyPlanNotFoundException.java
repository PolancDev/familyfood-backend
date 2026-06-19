package com.familyfood.domain.exception;

public class WeeklyPlanNotFoundException extends RuntimeException {
    public WeeklyPlanNotFoundException(String message) {
        super(message);
    }
}
