package com.familyfood.domain.exception;

public class JoinRequestNotFoundException extends RuntimeException {
    public JoinRequestNotFoundException(String message) {
        super(message);
    }
}
