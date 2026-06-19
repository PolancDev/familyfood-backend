package com.familyfood.infrastructure.adapter.persistence.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "plan_days",
        uniqueConstraints = @UniqueConstraint(columnNames = {"weekly_plan_id", "dia", "tipo"}))
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PlanDayEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weekly_plan_id", nullable = false)
    private WeeklyPlanEntity weeklyPlan;

    @Column(nullable = false, length = 10)
    private String dia;

    @Column(nullable = false, length = 10)
    private String tipo;

    @Column(name = "receta_id")
    private UUID recetaId;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(name = "sobras_origen_dia", length = 10)
    private String sobrasOrigenDia;

    @Column(name = "sobras_origen_tipo", length = 10)
    private String sobrasOrigenTipo;

    @Version
    @Column(nullable = false)
    private Long version;
}
