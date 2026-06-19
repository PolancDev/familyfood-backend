package com.familyfood.application.dto.plan;

import com.familyfood.domain.enums.DiaSemana;
import com.familyfood.domain.enums.EstadoDia;
import com.familyfood.domain.enums.TipoComida;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record SaveWeeklyPlanRequest(
    @NotNull int year,
    @NotNull int weekNumber,
    @NotNull List<PlanDayRequest> dias
) {
    public record PlanDayRequest(
        @NotNull DiaSemana dia,
        @NotNull TipoComida tipo,
        UUID recetaId,
        @NotNull EstadoDia estado,
        DiaSemana sobrasOrigenDia,
        TipoComida sobrasOrigenTipo
    ) {}
}
