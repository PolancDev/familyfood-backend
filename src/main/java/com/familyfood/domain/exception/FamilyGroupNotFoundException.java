package com.familyfood.domain.exception;

public class FamilyGroupNotFoundException extends RuntimeException {
    public FamilyGroupNotFoundException(String message) {
        super(message);
    }
}
