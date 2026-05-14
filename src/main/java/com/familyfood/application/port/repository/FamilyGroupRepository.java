package com.familyfood.application.port.repository;

import com.familyfood.domain.model.FamilyGroup;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de repositorio para grupos familiares.
 */
public interface FamilyGroupRepository {

    /**
     * Guarda un grupo familiar.
     *
     * @param familyGroup grupo a guardar
     * @return grupo guardado
     */
    FamilyGroup save(FamilyGroup familyGroup);

    /**
     * Busca un grupo por ID.
     *
     * @param id ID del grupo
     * @return Optional con el grupo encontrado
     */
    Optional<FamilyGroup> findById(UUID id);

    /**
     * Busca grupos creados por un usuario.
     *
     * @param userId ID del usuario creador
     * @return lista de grupos
     */
    List<FamilyGroup> findByCreatedBy(UUID userId);

    /**
     * Busca grupos por ID de miembro.
     *
     * @param userId ID del usuario miembro
     * @return lista de grupos
     */
    List<FamilyGroup> findByMemberUserId(UUID userId);

    /**
     * Verifica si existe un grupo por ID.
     *
     * @param id ID del grupo
     * @return true si existe
     */
    boolean existsById(UUID id);

    /**
     * Busca grupos por nombre (para autocomplete).
     *
     * @param query término de búsqueda
     * @return lista de grupos que coinciden
     */
    List<FamilyGroup> searchByName(String query);

    /**
     * Elimina (soft delete) un grupo familiar.
     *
     * @param familyGroup grupo a eliminar
     */
    void delete(FamilyGroup familyGroup);
}
