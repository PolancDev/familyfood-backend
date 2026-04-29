package com.familyfood.application.dto.family;

import com.familyfood.domain.enums.FamilyRole;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para un miembro de grupo familiar.
 */
public record FamilyMemberResponse(
        UUID id,
        UUID userId,
        String userName,
        String userEmail,
        FamilyRole role,
        LocalDateTime joinedAt
) { }
