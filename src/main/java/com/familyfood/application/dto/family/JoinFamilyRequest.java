package com.familyfood.application.dto.family;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record JoinFamilyRequest(
    @NotNull(message = "El ID del grupo familiar es obligatorio")
    UUID familyId
) {}
