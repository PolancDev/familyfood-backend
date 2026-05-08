package com.familyfood.infrastructure.adapter.persistence.entities;

import com.familyfood.domain.enums.FamilyRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "family_members", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "family_group_id"})
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FamilyMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "family_group_id", nullable = false)
    private UUID familyGroupId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FamilyRole role = FamilyRole.CONSUMER;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    @PrePersist
    protected void onCreate() {
        if (joinedAt == null) {
            joinedAt = LocalDateTime.now();
        }
    }
}
