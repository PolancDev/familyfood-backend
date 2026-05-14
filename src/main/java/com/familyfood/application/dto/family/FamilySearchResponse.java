package com.familyfood.application.dto.family;

import java.util.UUID;

/**
 * DTO de respuesta para la búsqueda de grupos familiares (autocomplete).
 */
public record FamilySearchResponse(
        UUID id,
        String name,
        long memberCount
) { }
