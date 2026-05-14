package com.familyfood.domain.exception;

import java.util.UUID;

public class FamilyGroupNotFoundException extends RuntimeException {
    public FamilyGroupNotFoundException(String message) {
        super(message);
    }

    public FamilyGroupNotFoundException(UUID familyId) {
        super("Grupo familiar no encontrado: " + familyId);
    }
}
