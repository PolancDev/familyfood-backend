package com.familyfood.infrastructure.adapter.persistence.adapters;

import com.familyfood.application.port.repository.FamilyGroupRepository;
import com.familyfood.domain.model.FamilyGroup;
import com.familyfood.infrastructure.adapter.persistence.entities.FamilyGroupEntity;
import com.familyfood.infrastructure.adapter.persistence.repository.SpringDataFamilyGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FamilyGroupRepositoryAdapter implements FamilyGroupRepository {

    private final SpringDataFamilyGroupRepository repository;

    @Override
    public FamilyGroup save(FamilyGroup familyGroup) {
        FamilyGroupEntity entity;
        if (familyGroup.getId() != null) {
            entity = toEntityForUpdate(familyGroup);
        } else {
            entity = toEntityForCreate(familyGroup);
        }
        return toDomain(repository.save(entity));
    }

    @Override
    public Optional<FamilyGroup> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<FamilyGroup> findByCreatedBy(UUID userId) {
        return repository.findByCreatedBy(userId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<FamilyGroup> findByMemberUserId(UUID userId) {
        return repository.findByMemberUserId(userId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    private FamilyGroup toDomain(FamilyGroupEntity entity) {
        if (entity == null) return null;
        return FamilyGroup.builder()
                .id(entity.getId())
                .name(entity.getName())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .version(entity.getVersion())
                .build();
    }

    private FamilyGroupEntity toEntityForCreate(FamilyGroup domain) {
        if (domain == null) return null;
        return FamilyGroupEntity.builder()
                .id(null)
                .name(domain.getName())
                .createdBy(domain.getCreatedBy())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    private FamilyGroupEntity toEntityForUpdate(FamilyGroup domain) {
        if (domain == null) return null;
        return FamilyGroupEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .createdBy(domain.getCreatedBy())
                .createdAt(domain.getCreatedAt())
                .version(domain.getVersion())
                .build();
    }
}
