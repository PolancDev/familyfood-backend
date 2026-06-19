package com.familyfood.application.dto.plan;

import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.UUID;

public record GenerateMenuRequest(
    @Positive int numeroPersonas,
    List<UUID> recetasExcluidas,
    boolean preferenciasUsar,
    boolean completar,
    Integer year,
    Integer weekNumber
) {}
