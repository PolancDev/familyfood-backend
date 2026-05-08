package com.familyfood.domain.model;

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
public class FamilyGroup {
    private UUID id;
    private String name;
    private UUID createdBy;
    private LocalDateTime createdAt;

    @Builder.Default
    private Long version = 0L;

    public static FamilyGroup create(String name, UUID createdBy) {
        return FamilyGroup.builder()
                .id(UUID.randomUUID())
                .name(name)
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
