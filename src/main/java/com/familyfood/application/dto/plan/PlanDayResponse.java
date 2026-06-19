package com.familyfood.application.dto.plan;

import com.familyfood.domain.enums.DiaSemana;
import com.familyfood.domain.enums.EstadoDia;
import com.familyfood.domain.enums.TipoComida;

import java.util.UUID;

public record PlanDayResponse(
    UUID id,
    DiaSemana dia,
    TipoComida tipo,
    RecetaResumen receta,
    EstadoDia estado,
    DiaSemana sobrasOrigenDia,
    TipoComida sobrasOrigenTipo,
    UUID sobrasOrigenRecetaId,
    String sobrasOrigenRecetaNombre,
    boolean alergenosAdvertencia
) {
    public record RecetaResumen(
        UUID id,
        String nombre,
        Integer tiempoMinutos
    ) {}
}
