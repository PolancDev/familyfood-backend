package com.familyfood.domain.model;

import com.familyfood.domain.enums.DiaSemana;
import com.familyfood.domain.enums.EstadoDia;
import com.familyfood.domain.enums.TipoComida;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanDay {
    private UUID id;
    private UUID weeklyPlanId;
    private DiaSemana dia;
    private TipoComida tipo;
    private UUID recetaId;
    private String recetaNombre;
    private Integer recetaTiempoMinutos;
    private EstadoDia estado;
    private DiaSemana sobrasOrigenDia;
    private TipoComida sobrasOrigenTipo;
    private boolean alergenosAdvertencia;

    @Builder.Default
    private Long version = 0L;

    public static PlanDay create(UUID weeklyPlanId, DiaSemana dia, TipoComida tipo) {
        return PlanDay.builder()
                .id(UUID.randomUUID())
                .weeklyPlanId(weeklyPlanId)
                .dia(dia)
                .tipo(tipo)
                .estado(EstadoDia.IMPROVISADO)
                .build();
    }
}
