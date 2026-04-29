package com.familyfood.infrastructure.adapter.persistence.adapters;

import com.familyfood.application.port.repository.JoinRequestRepository;
import com.familyfood.domain.enums.JoinRequestStatus;
import com.familyfood.domain.model.JoinRequest;
import com.familyfood.infrastructure.adapter.persistence.entities.JoinRequestEntity;
import com.familyfood.infrastructure.adapter.persistence.repository.SpringDataJoinRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JoinRequestRepositoryAdapter implements JoinRequestRepository {

    private final SpringDataJoinRequestRepository repository;

    @Override
    public JoinRequest save(JoinRequest joinRequest) {
        JoinRequestEntity entity;
        if (joinRequest.getId() != null) {
            entity = toEntityForUpdate(joinRequest);
        } else {
            entity = toEntityForCreate(joinRequest);
        }
        return toDomain(repository.save(entity));
    }

    @Override
    public Optional<JoinRequest> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<JoinRequest> findByFamilyGroupIdAndStatus(UUID familyGroupId, JoinRequestStatus status) {
        return repository.findByFamilyGroupIdAndStatus(familyGroupId, status).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<JoinRequest> findByUserId(UUID userId) {
        return repository.findByUserId(userId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<JoinRequest> findByUserIdAndFamilyGroupId(UUID userId, UUID familyGroupId) {
        return repository.findByUserIdAndFamilyGroupId(userId, familyGroupId).map(this::toDomain);
    }

    @Override
    public boolean existsByUserIdAndFamilyGroupIdAndStatus(UUID userId, UUID familyGroupId, JoinRequestStatus status) {
        return repository.existsByUserIdAndFamilyGroupIdAndStatus(userId, familyGroupId, status);
    }

    private JoinRequest toDomain(JoinRequestEntity entity) {
        if (entity == null) return null;
        return JoinRequest.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .familyGroupId(entity.getFamilyGroupId())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private JoinRequestEntity toEntityForCreate(JoinRequest domain) {
        if (domain == null) return null;
        return JoinRequestEntity.builder()
                .id(null)
                .userId(domain.getUserId())
                .familyGroupId(domain.getFamilyGroupId())
                .status(domain.getStatus())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    private JoinRequestEntity toEntityForUpdate(JoinRequest domain) {
        if (domain == null) return null;
        return JoinRequestEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .familyGroupId(domain.getFamilyGroupId())
                .status(domain.getStatus())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
