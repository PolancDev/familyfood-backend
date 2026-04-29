package com.familyfood.application.port.repository;

import com.familyfood.domain.model.FamilyMember;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de repositorio para miembros de grupos familiares.
 */
public interface FamilyMemberRepository {

    /**
     * Guarda un miembro del grupo.
     *
     * @param familyMember miembro a guardar
     * @return miembro guardado
     */
    FamilyMember save(FamilyMember familyMember);

    /**
     * Busca un miembro por ID.
     *
     * @param id ID del miembro
     * @return Optional con el miembro encontrado
     */
    Optional<FamilyMember> findById(UUID id);

    /**
     * Busca un miembro por usuario y grupo.
     *
     * @param userId        ID del usuario
     * @param familyGroupId ID del grupo
     * @return Optional con el miembro encontrado
     */
    Optional<FamilyMember> findByUserIdAndFamilyGroupId(
            UUID userId, UUID familyGroupId);

    /**
     * Busca todos los miembros de un grupo.
     *
     * @param familyGroupId ID del grupo
     * @return lista de miembros
     */
    List<FamilyMember> findByFamilyGroupId(UUID familyGroupId);

    /**
     * Busca todos los grupos de un usuario.
     *
     * @param userId ID del usuario
     * @return lista de miembros
     */
    List<FamilyMember> findByUserId(UUID userId);

    /**
     * Cuenta los miembros de un grupo.
     *
     * @param familyGroupId ID del grupo
     * @return número de miembros
     */
    long countByFamilyGroupId(UUID familyGroupId);

    /**
     * Verifica si un usuario es miembro de un grupo.
     *
     * @param userId        ID del usuario
     * @param familyGroupId ID del grupo
     * @return true si es miembro
     */
    boolean existsByUserIdAndFamilyGroupId(
            UUID userId, UUID familyGroupId);

    /**
     * Elimina un miembro del grupo.
     *
     * @param familyMember miembro a eliminar
     */
    void delete(FamilyMember familyMember);
}
