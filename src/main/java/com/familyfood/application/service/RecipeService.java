package com.familyfood.application.service;

import com.familyfood.application.dto.recipe.CreateRecipeRequest;
import com.familyfood.application.dto.recipe.RecipeListResponse;
import com.familyfood.application.dto.recipe.RecipeResponse;
import com.familyfood.application.dto.recipe.UpdateRecipeRequest;
import com.familyfood.application.mapper.RecipeMapper;
import com.familyfood.application.port.repository.RecipeRepository;
import com.familyfood.domain.exception.RecipeNotFoundException;
import com.familyfood.domain.model.Recipe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeMapper recipeMapper;

    public RecipeListResponse listarRecetas(UUID userId, Boolean favoritas, String busqueda, String etiqueta) {
        List<Recipe> recipes;

        if (Boolean.TRUE.equals(favoritas)) {
            recipes = recipeRepository.findByFavoritaTrueAndUserId(userId);
        } else if (etiqueta != null && !etiqueta.isBlank()) {
            recipes = recipeRepository.findByEtiquetasContainsAndUserId(etiqueta.toUpperCase(), userId);
        } else if (busqueda != null && !busqueda.isBlank()) {
            recipes = recipeRepository.findByNombreContainingIgnoreCaseAndUserId(busqueda, userId);
        } else {
            recipes = recipeRepository.findByUserId(userId);
        }

        return new RecipeListResponse(recipeMapper.toResponseList(recipes));
    }

    public RecipeResponse obtenerReceta(UUID id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException("No se ha encontrado la receta solicitada"));
        return recipeMapper.toResponse(recipe);
    }

    public RecipeResponse crearReceta(CreateRecipeRequest request, UUID userId) {
        Recipe recipe = recipeMapper.toDomainFromCreate(request);
        recipe.setUserId(userId);
        Recipe savedRecipe = recipeRepository.save(recipe);
        log.info("Receta creada: {} con id: {}", savedRecipe.getNombre(), savedRecipe.getId());
        return recipeMapper.toResponse(savedRecipe);
    }

    public RecipeResponse actualizarReceta(UUID id, UpdateRecipeRequest request, UUID userId) {
        Recipe existingRecipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException("No se ha encontrado la receta solicitada"));

        Recipe updatedRecipe = recipeMapper.toDomainFromUpdate(request);
        updatedRecipe.setId(id);
        updatedRecipe.setUserId(userId);
        updatedRecipe.setFavorita(existingRecipe.isFavorita());
        updatedRecipe.setImagen(existingRecipe.getImagen());
        updatedRecipe.setVersion(existingRecipe.getVersion());

        Recipe savedRecipe = recipeRepository.save(updatedRecipe);
        log.info("Receta actualizada: {} con id: {}", savedRecipe.getNombre(), savedRecipe.getId());
        return recipeMapper.toResponse(savedRecipe);
    }

    public void eliminarReceta(UUID id) {
        if (!recipeRepository.existsById(id)) {
            throw new RecipeNotFoundException("No se ha encontrado la receta solicitada");
        }
        recipeRepository.deleteById(id);
        log.info("Receta eliminada con id: {}", id);
    }

    public RecipeResponse toggleFavorita(UUID id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException("No se ha encontrado la receta solicitada"));
        recipe.marcarFavorita();
        Recipe savedRecipe = recipeRepository.save(recipe);
        log.info("Receta {} favorita cambiada a: {}", id, savedRecipe.isFavorita());
        return recipeMapper.toResponse(savedRecipe);
    }
}
