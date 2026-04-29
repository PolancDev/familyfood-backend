package com.familyfood.infrastructure.adapter.persistence.repository;

import com.familyfood.infrastructure.adapter.persistence.entities.FamilyMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data para entidades FamilyMemberEntity.
 */
public interface SpringDataFamilyMemberRepository
        extends JpaRepository<FamilyMemberEntity, UUID> {

    /**
     * Busca un miembro por usuario y grupo familiar.
     *
     * @param userId        ID del usuario
     * @param familyGroupId ID del grupo familiar
     * @return Optional con el miembro encontrado
     */
    Optional<FamilyMemberEntity> findByUserIdAndFamilyGroupId(
            UUID userId, UUID familyGroupId);

    /**
     * Busca todos los miembros de un grupo familiar.
     *
     * @param familyGroupId ID del grupo familiar
     * @return lista de miembros
     */
    List<FamilyMemberEntity> findByFamilyGroupId(UUID familyGroupId);

    /**
     * Busca todos los grupos a los que pertenece un usuario.
     *
     * @param userId ID del usuario
     * @return lista de miembros
     */
    List<FamilyMemberEntity> findByUserId(UUID userId);

    /**
     * Cuenta los miembros de un grupo familiar.
     *
     * @param familyGroupId ID del grupo familiar
     * @return número de miembros
     */
    long countByFamilyGroupId(UUID familyGroupId);

    /**
     * Verifica si un usuario ya es miembro de un grupo.
     *
     * @param userId        ID del usuario
     * @param familyGroupId ID del grupo familiar
     * @return true si ya es miembro
     */
    boolean existsByUserIdAndFamilyGroupId(UUID userId, UUID familyGroupId);
}
