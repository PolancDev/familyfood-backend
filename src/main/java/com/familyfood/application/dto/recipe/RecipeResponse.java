package com.familyfood.application.dto.recipe;

import com.familyfood.domain.enums.EtiquetaReceta;

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
    List<EtiquetaReceta> etiquetas,
    String imagen,
    boolean favorita,
    Long version
) {}
