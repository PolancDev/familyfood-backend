package com.familyfood.application.dto.family;

import com.familyfood.domain.enums.JoinRequestStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para una solicitud de unión.
 */
public record JoinRequestResponse(
        UUID id,
        UUID userId,
        String userName,
        String userEmail,
        UUID familyGroupId,
        String familyGroupName,
        JoinRequestStatus status,
        LocalDateTime createdAt
) { }
