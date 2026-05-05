package com.familyfood.application.port.repository;

import com.familyfood.domain.enums.EtiquetaReceta;
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
    List<Recipe> findByEtiquetasContainsAndUserId(EtiquetaReceta etiqueta, UUID userId);
    List<Recipe> findByNombreContainingIgnoreCaseAndUserId(String busqueda, UUID userId);
    void deleteById(UUID id);
    boolean existsById(UUID id);
}
