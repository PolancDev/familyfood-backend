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
     * Busca grupos familiares activos por el ID del creador.
     *
     * @param createdBy ID del usuario creador
     * @return lista de grupos familiares activos
     */
    @Query("SELECT fg FROM FamilyGroupEntity fg WHERE fg.createdBy = :createdBy AND fg.deletedAt IS NULL")
    List<FamilyGroupEntity> findByCreatedBy(@Param("createdBy") UUID createdBy);

    /**
     * Busca grupos familiares activos por ID de miembro.
     *
     * @param userId ID del usuario miembro
     * @return lista de grupos familiares activos
     */
    @Query("SELECT fg FROM FamilyGroupEntity fg "
            + "JOIN FamilyMemberEntity fm ON fm.familyGroupId = fg.id "
            + "WHERE fm.userId = :userId AND fg.deletedAt IS NULL")
    List<FamilyGroupEntity> findByMemberUserId(@Param("userId") UUID userId);

    /**
     * Busca grupos familiares activos por nombre (autocomplete).
     *
     * @param query término de búsqueda
     * @return lista de grupos familiares activos que coinciden
     */
    @Query("SELECT fg FROM FamilyGroupEntity fg WHERE "
            + "LOWER(fg.name) LIKE LOWER(CONCAT('%', :query, '%')) AND fg.deletedAt IS NULL "
            + "ORDER BY fg.name ASC")
    List<FamilyGroupEntity> searchByName(@Param("query") String query);
}
