package com.familyfood.application.dto.recipe;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateRecipeRequest(
    @NotBlank(message = "El nombre de la receta es obligatorio")
    @Size(min = 2, max = 200, message = "El nombre debe tener entre 2 y 200 caracteres")
    String nombre,

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 2000, message = "La descripción no puede exceder 2000 caracteres")
    String descripcion,

    @Positive(message = "El tiempo debe ser positivo")
    Integer tiempoMinutos,

    @Positive(message = "Las raciones deben ser positivas")
    Integer raciones,

    @NotEmpty(message = "Debe haber al menos un ingrediente")
    List<RecipeIngredientDTO> ingredientes,

    @NotEmpty(message = "Debe haber al menos un paso")
    List<String> pasos,

    List<String> etiquetas,

    boolean favorita
) {}
