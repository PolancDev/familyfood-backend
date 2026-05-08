package com.familyfood.application.dto.recipe;

import java.util.List;
import java.util.UUID;

public record RecipeResponse(
    UUID id,
    String nombre,
    String descripcion,
    Integer tiempoMinutos,
    Integer raciones,
    List<RecipeIngredientDTO> ingredientes,
    List<String> pasos,
    List<String> etiquetas,
    String imagen,
    boolean favorita
) {}
