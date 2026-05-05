package com.familyfood.infrastructure.adapter.persistence.repository;

import com.familyfood.domain.enums.EtiquetaReceta;
import com.familyfood.infrastructure.adapter.persistence.entities.RecipeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataRecipeRepository extends JpaRepository<RecipeEntity, UUID> {

    List<RecipeEntity> findByUserIdOrderByNombreAsc(UUID userId);

    List<RecipeEntity> findByFavoritaTrueAndUserIdOrderByNombreAsc(UUID userId);

    List<RecipeEntity> findByEtiquetasContainsAndUserIdOrderByNombreAsc(EtiquetaReceta etiqueta, UUID userId);

    List<RecipeEntity> findByNombreContainingIgnoreCaseAndUserIdOrderByNombreAsc(String busqueda, UUID userId);
}
