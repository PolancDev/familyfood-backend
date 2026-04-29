package com.familyfood.application.dto.family;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para un grupo familiar.
 */
public record FamilyResponse(
        UUID id,
        String name,
        UUID createdBy,
        LocalDateTime createdAt,
        long memberCount
) { }
