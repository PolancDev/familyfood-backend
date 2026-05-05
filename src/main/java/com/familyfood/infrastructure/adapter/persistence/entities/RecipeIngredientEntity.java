package com.familyfood.infrastructure.adapter.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RecipeIngredientEntity {

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private Double cantidad;

    @Column(nullable = false)
    private String unidad;
}
