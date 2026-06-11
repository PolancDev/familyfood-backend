package com.familyfood.application.port.repository;

import com.familyfood.domain.model.Recipe;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecipeRepository {
    Recipe save(Recipe recipe);
    Optional<Recipe> findById(UUID id);
    List<Recipe> findAll();
    List<Recipe> findByUserId(UUID userId);
    List<Recipe> findByFavoritaTrueAndUserId(UUID userId);
    List<Recipe> findByEtiquetasContainsAndUserId(String etiqueta, UUID userId);
    List<Recipe> findByNombreContainingIgnoreCaseAndUserId(String busqueda, UUID userId);

    // Queries por grupo familiar (recetas compartidas)
    List<Recipe> findByFamilyGroupId(UUID familyGroupId);
    List<Recipe> findByFavoritaTrueAndFamilyGroupId(UUID familyGroupId);
    List<Recipe> findByEtiquetasContainsAndFamilyGroupId(String etiqueta, UUID familyGroupId);
    List<Recipe> findByNombreContainingIgnoreCaseAndFamilyGroupId(String busqueda, UUID familyGroupId);

    // Fallback: usuario sin familia
    List<Recipe> findByUserIdAndFamilyGroupIdIsNull(UUID userId);

    void deleteById(UUID id);
    boolean existsById(UUID id);
}
