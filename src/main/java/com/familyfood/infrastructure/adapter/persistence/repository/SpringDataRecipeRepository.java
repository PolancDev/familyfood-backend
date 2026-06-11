package com.familyfood.infrastructure.adapter.persistence.repository;

import com.familyfood.infrastructure.adapter.persistence.entities.RecipeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataRecipeRepository extends JpaRepository<RecipeEntity, UUID> {

    List<RecipeEntity> findByUserIdOrderByNombreAsc(UUID userId);

    List<RecipeEntity> findByFavoritaTrueAndUserIdOrderByNombreAsc(UUID userId);

    List<RecipeEntity> findByEtiquetasContainsAndUserIdOrderByNombreAsc(String etiqueta, UUID userId);

    List<RecipeEntity> findByNombreContainingIgnoreCaseAndUserIdOrderByNombreAsc(String busqueda, UUID userId);

    // Queries por grupo familiar (recetas compartidas)
    List<RecipeEntity> findByFamilyGroupIdOrderByNombreAsc(UUID familyGroupId);

    List<RecipeEntity> findByFavoritaTrueAndFamilyGroupIdOrderByNombreAsc(UUID familyGroupId);

    List<RecipeEntity> findByEtiquetasContainsAndFamilyGroupIdOrderByNombreAsc(String etiqueta, UUID familyGroupId);

    List<RecipeEntity> findByNombreContainingIgnoreCaseAndFamilyGroupIdOrderByNombreAsc(String busqueda, UUID familyGroupId);

    // Fallback: usuario sin familia
    List<RecipeEntity> findByUserIdAndFamilyGroupIdIsNullOrderByNombreAsc(UUID userId);
}
