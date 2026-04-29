package com.familyfood.infrastructure.adapter.persistence.repository;

import com.familyfood.infrastructure.adapter.persistence.entities.FamilyGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio Spring Data para entidades FamilyGroupEntity.
 */
public interface SpringDataFamilyGroupRepository
        extends JpaRepository<FamilyGroupEntity, UUID> {

    /**
     * Busca grupos familiares por el ID del creador.
     *
     * @param createdBy ID del usuario creador
     * @return lista de grupos familiares
     */
    List<FamilyGroupEntity> findByCreatedBy(UUID createdBy);

    /**
     * Busca grupos familiares por ID de miembro.
     *
     * @param userId ID del usuario miembro
     * @return lista de grupos familiares
     */
    @Query("SELECT fg FROM FamilyGroupEntity fg "
            + "JOIN FamilyMemberEntity fm ON fm.familyGroupId = fg.id "
            + "WHERE fm.userId = :userId")
    List<FamilyGroupEntity> findByMemberUserId(@Param("userId") UUID userId);
}
