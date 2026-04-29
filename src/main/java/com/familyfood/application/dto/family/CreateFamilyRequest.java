package com.familyfood.application.dto.family;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para solicitud de creación de grupo familiar.
 */
public record CreateFamilyRequest(
        @NotBlank(message = "El nombre de la familia es obligatorio")
        @Size(min = 2, max = 255, message = "El nombre debe tener entre 2 y 255 caracteres")
        String name
) { }
