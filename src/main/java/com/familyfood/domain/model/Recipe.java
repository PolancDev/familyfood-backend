package com.familyfood.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recipe {
    private UUID id;
    private String nombre;
    private String descripcion;
    private Integer tiempoMinutos;
    private Integer raciones;
    private List<RecipeIngredient> ingredientes;
    private List<String> pasos;
    private List<String> etiquetas;
    private String imagen;
    private boolean favorita;
    private UUID userId;

    @Builder.Default
    private Long version = 0L;

    public void marcarFavorita() {
        this.favorita = !this.favorita;
    }
}
