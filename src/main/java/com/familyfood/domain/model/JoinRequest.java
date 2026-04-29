package com.familyfood.domain.model;

import com.familyfood.domain.enums.JoinRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinRequest {
    private UUID id;
    private UUID userId;
    private UUID familyGroupId;
    private JoinRequestStatus status;
    private LocalDateTime createdAt;

    public static JoinRequest create(UUID userId, UUID familyGroupId) {
        return JoinRequest.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .familyGroupId(familyGroupId)
                .status(JoinRequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void approve() {
        this.status = JoinRequestStatus.APPROVED;
    }

    public void reject() {
        this.status = JoinRequestStatus.REJECTED;
    }
}
