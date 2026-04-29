package com.familyfood.infrastructure.adapter.persistence.repository;

import com.familyfood.domain.enums.JoinRequestStatus;
import com.familyfood.infrastructure.adapter.persistence.entities.JoinRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data para entidades JoinRequestEntity.
 */
public interface SpringDataJoinRequestRepository
        extends JpaRepository<JoinRequestEntity, UUID> {

    /**
     * Busca solicitudes por grupo y estado.
     *
     * @param familyGroupId ID del grupo familiar
     * @param status        estado de la solicitud
     * @return lista de solicitudes
     */
    List<JoinRequestEntity> findByFamilyGroupIdAndStatus(
            UUID familyGroupId, JoinRequestStatus status);

    /**
     * Busca solicitudes por usuario.
     *
     * @param userId ID del usuario
     * @return lista de solicitudes
     */
    List<JoinRequestEntity> findByUserId(UUID userId);

    /**
     * Busca una solicitud por usuario y grupo.
     *
     * @param userId        ID del usuario
     * @param familyGroupId ID del grupo familiar
     * @return Optional con la solicitud encontrada
     */
    Optional<JoinRequestEntity> findByUserIdAndFamilyGroupId(
            UUID userId, UUID familyGroupId);

    /**
     * Verifica si existe una solicitud con estado específico.
     *
     * @param userId        ID del usuario
     * @param familyGroupId ID del grupo familiar
     * @param status        estado a verificar
     * @return true si existe
     */
    boolean existsByUserIdAndFamilyGroupIdAndStatus(
            UUID userId, UUID familyGroupId, JoinRequestStatus status);
}
