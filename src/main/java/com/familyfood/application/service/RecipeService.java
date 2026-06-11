package com.familyfood.application.service;

import com.familyfood.application.dto.recipe.CreateRecipeRequest;
import com.familyfood.application.dto.recipe.RecipeListResponse;
import com.familyfood.application.dto.recipe.RecipeResponse;
import com.familyfood.application.dto.recipe.UpdateRecipeRequest;
import com.familyfood.application.mapper.RecipeMapper;
import com.familyfood.application.port.repository.FamilyMemberRepository;
import com.familyfood.application.port.repository.RecipeRepository;
import com.familyfood.application.port.repository.UserRepository;
import com.familyfood.domain.exception.RecipeNotFoundException;
import com.familyfood.domain.exception.UnauthorizedException;
import com.familyfood.domain.model.FamilyMember;
import com.familyfood.domain.model.Recipe;
import com.familyfood.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeMapper recipeMapper;
    private final FamilyMemberRepository familyMemberRepository;
    private final UserRepository userRepository;

    /**
     * Lista las recetas visibles para un usuario.
     * Si el usuario pertenece a una familia, muestra las recetas de toda la familia.
     * Si no tiene familia, muestra solo sus propias recetas.
     */
    public RecipeListResponse listarRecetas(UUID userId, Boolean favoritas, String busqueda, String etiqueta) {
        UUID familyGroupId = getUserFamilyGroupId(userId);
        List<Recipe> recipes;

        if (familyGroupId != null) {
            // Usuario con familia: mostrar recetas del grupo familiar
            if (Boolean.TRUE.equals(favoritas)) {
                recipes = recipeRepository.findByFavoritaTrueAndFamilyGroupId(familyGroupId);
            } else if (etiqueta != null && !etiqueta.isBlank()) {
                recipes = recipeRepository.findByEtiquetasContainsAndFamilyGroupId(etiqueta.toUpperCase(), familyGroupId);
            } else if (busqueda != null && !busqueda.isBlank()) {
                recipes = recipeRepository.findByNombreContainingIgnoreCaseAndFamilyGroupId(busqueda, familyGroupId);
            } else {
                recipes = recipeRepository.findByFamilyGroupId(familyGroupId);
            }
        } else {
            // Usuario sin familia: mostrar solo sus propias recetas
            if (Boolean.TRUE.equals(favoritas)) {
                recipes = recipeRepository.findByFavoritaTrueAndUserId(userId);
            } else if (etiqueta != null && !etiqueta.isBlank()) {
                recipes = recipeRepository.findByEtiquetasContainsAndUserId(etiqueta.toUpperCase(), userId);
            } else if (busqueda != null && !busqueda.isBlank()) {
                recipes = recipeRepository.findByNombreContainingIgnoreCaseAndUserId(busqueda, userId);
            } else {
                recipes = recipeRepository.findByUserId(userId);
            }
        }

        List<RecipeResponse> responses = recipes.stream()
                .map(this::toResponseWithCreator)
                .toList();
        return new RecipeListResponse(responses);
    }

    public RecipeResponse obtenerReceta(UUID id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException("No se ha encontrado la receta solicitada"));
        return toResponseWithCreator(recipe);
    }

    /**
     * Crea una receta y la asigna al grupo familiar del usuario.
     * Si el usuario no tiene familia, la receta queda sin familyGroupId.
     */
    public RecipeResponse crearReceta(CreateRecipeRequest request, UUID userId) {
        UUID familyGroupId = getUserFamilyGroupId(userId);

        Recipe recipe = recipeMapper.toDomainFromCreate(request);
        recipe.setUserId(userId);
        recipe.setFamilyGroupId(familyGroupId);

        Recipe savedRecipe = recipeRepository.save(recipe);
        log.info("Receta creada: {} con id: {} (familia: {})", savedRecipe.getNombre(), savedRecipe.getId(), familyGroupId);
        return toResponseWithCreator(savedRecipe);
    }

    public RecipeResponse actualizarReceta(UUID id, UpdateRecipeRequest request, UUID userId) {
        Recipe existingRecipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException("No se ha encontrado la receta solicitada"));

        // Verificar que el usuario es el autor o admin del grupo familiar
        validateRecipeOwnership(existingRecipe, userId);

        Recipe updatedRecipe = recipeMapper.toDomainFromUpdate(request);
        updatedRecipe.setId(id);
        updatedRecipe.setUserId(existingRecipe.getUserId()); // Mantener el autor original
        updatedRecipe.setFamilyGroupId(existingRecipe.getFamilyGroupId()); // Mantener la familia original
        updatedRecipe.setFavorita(existingRecipe.isFavorita());
        updatedRecipe.setImagen(existingRecipe.getImagen());
        updatedRecipe.setVersion(existingRecipe.getVersion());

        Recipe savedRecipe = recipeRepository.save(updatedRecipe);
        log.info("Receta actualizada: {} con id: {}", savedRecipe.getNombre(), savedRecipe.getId());
        return toResponseWithCreator(savedRecipe);
    }

    public void eliminarReceta(UUID id, UUID userId) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException("No se ha encontrado la receta solicitada"));

        // Verificar que el usuario es el autor o admin del grupo familiar
        validateRecipeOwnership(recipe, userId);

        recipeRepository.deleteById(id);
        log.info("Receta eliminada con id: {} por usuario: {}", id, userId);
    }

    public RecipeResponse toggleFavorita(UUID id, UUID userId) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException("No se ha encontrado la receta solicitada"));
        recipe.marcarFavorita();
        Recipe savedRecipe = recipeRepository.save(recipe);
        log.info("Receta {} favorita cambiada a: {} por usuario: {}", id, savedRecipe.isFavorita(), userId);
        return toResponseWithCreator(savedRecipe);
    }

    /**
     * Obtiene el familyGroupId del primer grupo familiar del usuario.
     * Retorna null si el usuario no pertenece a ninguna familia.
     */
    private UUID getUserFamilyGroupId(UUID userId) {
        List<FamilyMember> memberships = familyMemberRepository.findByUserId(userId);
        if (memberships.isEmpty()) {
            return null;
        }
        // Por ahora, usamos la primera familia del usuario
        // En el futuro se podría permitir seleccionar la familia activa
        return memberships.get(0).getFamilyGroupId();
    }

    /**
     * Valida que el usuario puede modificar/eliminar una receta.
     * Puede hacerlo si es el autor de la receta o si es ADMIN del grupo familiar.
     */
    private void validateRecipeOwnership(Recipe recipe, UUID userId) {
        // El autor siempre puede modificar sus recetas
        if (recipe.getUserId().equals(userId)) {
            return;
        }

        // Si la receta pertenece a un grupo familiar, el ADMIN del grupo también puede modificarla
        if (recipe.getFamilyGroupId() != null) {
            familyMemberRepository.findByUserIdAndFamilyGroupId(userId, recipe.getFamilyGroupId())
                    .filter(member -> member.getRole().name().equals("ADMIN"))
                    .orElseThrow(() -> new UnauthorizedException("No tienes permisos para modificar esta receta"));
        } else {
            throw new UnauthorizedException("No tienes permisos para modificar esta receta");
        }
    }

    /**
     * Convierte una Recipe a RecipeResponse incluyendo el nombre del creador.
     */
    private RecipeResponse toResponseWithCreator(Recipe recipe) {
        RecipeResponse response = recipeMapper.toResponse(recipe);
        String nombreCreador = userRepository.findById(recipe.getUserId())
                .map(User::getNombre)
                .orElse("Desconocido");

        return new RecipeResponse(
                response.id(),
                response.nombre(),
                response.descripcion(),
                response.tiempoMinutos(),
                response.raciones(),
                response.ingredientes(),
                response.pasos(),
                response.etiquetas(),
                response.imagen(),
                response.favorita(),
                nombreCreador
        );
    }
}