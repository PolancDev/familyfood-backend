package com.familyfood.infrastructure.adapter.persistence.adapters;

import com.familyfood.application.port.repository.FamilyMemberRepository;
import com.familyfood.domain.model.FamilyMember;
import com.familyfood.infrastructure.adapter.persistence.entities.FamilyMemberEntity;
import com.familyfood.infrastructure.adapter.persistence.repository.SpringDataFamilyMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FamilyMemberRepositoryAdapter implements FamilyMemberRepository {

    private final SpringDataFamilyMemberRepository repository;

    @Override
    public FamilyMember save(FamilyMember familyMember) {
        FamilyMemberEntity entity;
        if (familyMember.getId() != null) {
            entity = toEntityForUpdate(familyMember);
        } else {
            entity = toEntityForCreate(familyMember);
        }
        return toDomain(repository.save(entity));
    }

    @Override
    public Optional<FamilyMember> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<FamilyMember> findByUserIdAndFamilyGroupId(UUID userId, UUID familyGroupId) {
        return repository.findByUserIdAndFamilyGroupId(userId, familyGroupId).map(this::toDomain);
    }

    @Override
    public List<FamilyMember> findByFamilyGroupId(UUID familyGroupId) {
        return repository.findByFamilyGroupId(familyGroupId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<FamilyMember> findByUserId(UUID userId) {
        return repository.findByUserId(userId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countByFamilyGroupId(UUID familyGroupId) {
        return repository.countByFamilyGroupId(familyGroupId);
    }

    @Override
    public boolean existsByUserIdAndFamilyGroupId(UUID userId, UUID familyGroupId) {
        return repository.existsByUserIdAndFamilyGroupId(userId, familyGroupId);
    }

    @Override
    public void delete(FamilyMember familyMember) {
        repository.delete(toEntityForUpdate(familyMember));
    }

    private FamilyMember toDomain(FamilyMemberEntity entity) {
        if (entity == null) return null;
        return FamilyMember.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .familyGroupId(entity.getFamilyGroupId())
                .role(entity.getRole())
                .joinedAt(entity.getJoinedAt())
                .build();
    }

    private FamilyMemberEntity toEntityForCreate(FamilyMember domain) {
        if (domain == null) return null;
        return FamilyMemberEntity.builder()
                .id(null)
                .userId(domain.getUserId())
                .familyGroupId(domain.getFamilyGroupId())
                .role(domain.getRole())
                .joinedAt(domain.getJoinedAt())
                .build();
    }

    private FamilyMemberEntity toEntityForUpdate(FamilyMember domain) {
        if (domain == null) return null;
        return FamilyMemberEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .familyGroupId(domain.getFamilyGroupId())
                .role(domain.getRole())
                .joinedAt(domain.getJoinedAt())
                .build();
    }
}
