package com.familyfood.application.dto.recipe;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record RecipeIngredientDTO(
    @NotBlank(message = "El nombre del ingrediente es obligatorio")
    String nombre,

    @Positive(message = "La cantidad debe ser positiva")
    Double cantidad,

    @NotBlank(message = "La unidad es obligatoria")
    String unidad
) {}
