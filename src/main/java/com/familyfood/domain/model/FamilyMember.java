package com.familyfood.domain.model;

import com.familyfood.domain.enums.FamilyRole;
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
public class FamilyMember {
    private UUID id;
    private UUID userId;
    private UUID familyGroupId;
    private FamilyRole role;
    private LocalDateTime joinedAt;

    @Builder.Default
    private Long version = 0L;

    public static FamilyMember create(UUID userId, UUID familyGroupId, FamilyRole role) {
        return FamilyMember.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .familyGroupId(familyGroupId)
                .role(role)
                .joinedAt(LocalDateTime.now())
                .build();
    }
}
