package com.familyfood.application.port.repository;

import com.familyfood.domain.enums.JoinRequestStatus;
import com.familyfood.domain.model.JoinRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de repositorio para solicitudes de unión.
 */
public interface JoinRequestRepository {

    /**
     * Guarda una solicitud de unión.
     *
     * @param joinRequest solicitud a guardar
     * @return solicitud guardada
     */
    JoinRequest save(JoinRequest joinRequest);

    /**
     * Busca una solicitud por ID.
     *
     * @param id ID de la solicitud
     * @return Optional con la solicitud encontrada
     */
    Optional<JoinRequest> findById(UUID id);

    /**
     * Busca solicitudes por grupo y estado.
     *
     * @param familyGroupId ID del grupo
     * @param status        estado de la solicitud
     * @return lista de solicitudes
     */
    List<JoinRequest> findByFamilyGroupIdAndStatus(
            UUID familyGroupId, JoinRequestStatus status);

    /**
     * Busca solicitudes por usuario.
     *
     * @param userId ID del usuario
     * @return lista de solicitudes
     */
    List<JoinRequest> findByUserId(UUID userId);

    /**
     * Busca una solicitud por usuario y grupo.
     *
     * @param userId        ID del usuario
     * @param familyGroupId ID del grupo
     * @return Optional con la solicitud encontrada
     */
    Optional<JoinRequest> findByUserIdAndFamilyGroupId(
            UUID userId, UUID familyGroupId);

    /**
     * Verifica si existe una solicitud con estado específico.
     *
     * @param userId        ID del usuario
     * @param familyGroupId ID del grupo
     * @param status        estado a verificar
     * @return true si existe
     */
    boolean existsByUserIdAndFamilyGroupIdAndStatus(
            UUID userId, UUID familyGroupId, JoinRequestStatus status);
}
