package com.familyfood.application.service;

import com.familyfood.application.dto.recipe.CreateRecipeRequest;
import com.familyfood.application.dto.recipe.RecipeIngredientDTO;
import com.familyfood.application.dto.recipe.RecipeListResponse;
import com.familyfood.application.dto.recipe.RecipeResponse;
import com.familyfood.application.dto.recipe.UpdateRecipeRequest;
import com.familyfood.application.mapper.RecipeMapper;
import com.familyfood.application.port.repository.RecipeRepository;
import com.familyfood.domain.enums.EtiquetaReceta;
import com.familyfood.domain.exception.RecipeNotFoundException;
import com.familyfood.domain.model.Recipe;
import com.familyfood.domain.model.RecipeIngredient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecipeService Tests")
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private RecipeMapper recipeMapper;

    private RecipeService recipeService;

    private UUID userId;
    private UUID recipeId;
    private Recipe testRecipe;
    private RecipeResponse testRecipeResponse;
    private CreateRecipeRequest createRequest;
    private UpdateRecipeRequest updateRequest;

    @BeforeEach
    void setUp() {
        recipeService = new RecipeService(recipeRepository, recipeMapper);

        userId = UUID.randomUUID();
        recipeId = UUID.randomUUID();

        List<RecipeIngredient> ingredients = List.of(
                RecipeIngredient.builder().nombre("Tomate").cantidad(2.0).unidad("unidades").build()
        );

        testRecipe = Recipe.builder()
                .id(recipeId)
                .nombre("Ensalada")
                .descripcion("Ensalada fresca")
                .tiempoMinutos(15)
                .raciones(2)
                .ingredientes(ingredients)
                .pasos(List.of("Cortar", "Mezclar"))
                .etiquetas(List.of(EtiquetaReceta.RAPIDA))
                .favorita(false)
                .userId(userId)
                .version(0L)
                .build();

        List<RecipeIngredientDTO> ingredientDTOs = List.of(
                new RecipeIngredientDTO("Tomate", 2.0, "unidades")
        );

        testRecipeResponse = new RecipeResponse(
                recipeId, "Ensalada", "Ensalada fresca", 15, 2,
                ingredientDTOs, List.of("Cortar", "Mezclar"),
                List.of(EtiquetaReceta.RAPIDA), null, false
        );

        createRequest = new CreateRecipeRequest(
                "Ensalada", "Ensalada fresca", 15, 2,
                ingredientDTOs, List.of("Cortar", "Mezclar"),
                List.of(EtiquetaReceta.RAPIDA), false
        );

        updateRequest = new UpdateRecipeRequest(
                "Ensalada Actualizada", "Descripción actualizada", 20, 4,
                ingredientDTOs, List.of("Cortar", "Mezclar", "Servir"),
                List.of(EtiquetaReceta.RAPIDA, EtiquetaReceta.ECONOMICA)
        );
    }

    @Nested
    @DisplayName("Listar recetas")
    class ListarRecetasTests {

        @Test
        @DisplayName("Should list all recipes for user")
        void shouldListAllRecipes() {
            when(recipeRepository.findByUserId(userId)).thenReturn(List.of(testRecipe));
            when(recipeMapper.toResponseList(anyList())).thenReturn(List.of(testRecipeResponse));

            RecipeListResponse response = recipeService.listarRecetas(userId, null, null, null);

            assertThat(response).isNotNull();
            assertThat(response.recetas()).hasSize(1);
            assertThat(response.recetas().get(0).nombre()).isEqualTo("Ensalada");
            verify(recipeRepository).findByUserId(userId);
        }

        @Test
        @DisplayName("Should list favorite recipes when favoritas=true")
        void shouldListFavoriteRecipes() {
            when(recipeRepository.findByFavoritaTrueAndUserId(userId)).thenReturn(List.of(testRecipe));
            when(recipeMapper.toResponseList(anyList())).thenReturn(List.of(testRecipeResponse));

            RecipeListResponse response = recipeService.listarRecetas(userId, true, null, null);

            assertThat(response).isNotNull();
            assertThat(response.recetas()).hasSize(1);
            verify(recipeRepository).findByFavoritaTrueAndUserId(userId);
        }

        @Test
        @DisplayName("Should search recipes by name")
        void shouldSearchRecipesByName() {
            when(recipeRepository.findByNombreContainingIgnoreCaseAndUserId("ensalada", userId))
                    .thenReturn(List.of(testRecipe));
            when(recipeMapper.toResponseList(anyList())).thenReturn(List.of(testRecipeResponse));

            RecipeListResponse response = recipeService.listarRecetas(userId, null, "ensalada", null);

            assertThat(response).isNotNull();
            assertThat(response.recetas()).hasSize(1);
            verify(recipeRepository).findByNombreContainingIgnoreCaseAndUserId("ensalada", userId);
        }

        @Test
        @DisplayName("Should filter recipes by etiqueta")
        void shouldFilterByEtiqueta() {
            when(recipeRepository.findByEtiquetasContainsAndUserId("RAPIDA", userId))
                    .thenReturn(List.of(testRecipe));
            when(recipeMapper.toResponseList(anyList())).thenReturn(List.of(testRecipeResponse));

            RecipeListResponse response = recipeService.listarRecetas(userId, null, null, "RAPIDA");

            assertThat(response).isNotNull();
            assertThat(response.recetas()).hasSize(1);
            verify(recipeRepository).findByEtiquetasContainsAndUserId("RAPIDA", userId);
        }

        @Test
        @DisplayName("Should filter recipes by custom etiqueta")
        void shouldFilterByCustomEtiqueta() {
            when(recipeRepository.findByEtiquetasContainsAndUserId("ETIQUETA1", userId))
                    .thenReturn(List.of(testRecipe));
            when(recipeMapper.toResponseList(anyList())).thenReturn(List.of(testRecipeResponse));

            RecipeListResponse response = recipeService.listarRecetas(userId, null, null, "etiqueta1");

            assertThat(response).isNotNull();
            assertThat(response.recetas()).hasSize(1);
            verify(recipeRepository).findByEtiquetasContainsAndUserId("ETIQUETA1", userId);
        }
    }

    @Nested
    @DisplayName("Obtener receta")
    class ObtenerRecetaTests {

        @Test
        @DisplayName("Should return recipe when found")
        void shouldReturnRecipeWhenFound() {
            when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(testRecipe));
            when(recipeMapper.toResponse(testRecipe)).thenReturn(testRecipeResponse);

            RecipeResponse response = recipeService.obtenerReceta(recipeId);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(recipeId);
            assertThat(response.nombre()).isEqualTo("Ensalada");
        }

        @Test
        @DisplayName("Should throw exception when recipe not found")
        void shouldThrowExceptionWhenNotFound() {
            when(recipeRepository.findById(recipeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> recipeService.obtenerReceta(recipeId))
                    .isInstanceOf(RecipeNotFoundException.class)
                    .hasMessageContaining("No se ha encontrado la receta solicitada");
        }
    }

    @Nested
    @DisplayName("Crear receta")
    class CrearRecetaTests {

        @Test
        @DisplayName("Should create recipe successfully")
        void shouldCreateRecipeSuccessfully() {
            when(recipeMapper.toDomainFromCreate(createRequest)).thenReturn(testRecipe);
            when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);
            when(recipeMapper.toResponse(testRecipe)).thenReturn(testRecipeResponse);

            RecipeResponse response = recipeService.crearReceta(createRequest, userId);

            assertThat(response).isNotNull();
            assertThat(response.nombre()).isEqualTo("Ensalada");
            verify(recipeRepository).save(any(Recipe.class));
        }
    }

    @Nested
    @DisplayName("Actualizar receta")
    class ActualizarRecetaTests {

        @Test
        @DisplayName("Should update recipe successfully")
        void shouldUpdateRecipeSuccessfully() {
            when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(testRecipe));
            when(recipeMapper.toDomainFromUpdate(updateRequest)).thenReturn(testRecipe);
            when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);
            when(recipeMapper.toResponse(testRecipe)).thenReturn(testRecipeResponse);

            RecipeResponse response = recipeService.actualizarReceta(recipeId, updateRequest, userId);

            assertThat(response).isNotNull();
            verify(recipeRepository).findById(recipeId);
            verify(recipeRepository).save(any(Recipe.class));
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent recipe")
        void shouldThrowExceptionWhenNotFound() {
            when(recipeRepository.findById(recipeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> recipeService.actualizarReceta(recipeId, updateRequest, userId))
                    .isInstanceOf(RecipeNotFoundException.class)
                    .hasMessageContaining("No se ha encontrado la receta solicitada");
        }
    }

    @Nested
    @DisplayName("Eliminar receta")
    class EliminarRecetaTests {

        @Test
        @DisplayName("Should delete recipe successfully")
        void shouldDeleteRecipeSuccessfully() {
            when(recipeRepository.existsById(recipeId)).thenReturn(true);

            recipeService.eliminarReceta(recipeId);

            verify(recipeRepository).deleteById(recipeId);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent recipe")
        void shouldThrowExceptionWhenNotFound() {
            when(recipeRepository.existsById(recipeId)).thenReturn(false);

            assertThatThrownBy(() -> recipeService.eliminarReceta(recipeId))
                    .isInstanceOf(RecipeNotFoundException.class)
                    .hasMessageContaining("No se ha encontrado la receta solicitada");
        }
    }

    @Nested
    @DisplayName("Toggle favorita")
    class ToggleFavoritaTests {

        @Test
        @DisplayName("Should toggle favorite status")
        void shouldToggleFavoriteStatus() {
            when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(testRecipe));
            when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);
            when(recipeMapper.toResponse(testRecipe)).thenReturn(testRecipeResponse);

            RecipeResponse response = recipeService.toggleFavorita(recipeId);

            assertThat(response).isNotNull();
            verify(recipeRepository).findById(recipeId);
            verify(recipeRepository).save(any(Recipe.class));
        }

        @Test
        @DisplayName("Should throw exception when toggling non-existent recipe")
        void shouldThrowExceptionWhenNotFound() {
            when(recipeRepository.findById(recipeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> recipeService.toggleFavorita(recipeId))
                    .isInstanceOf(RecipeNotFoundException.class)
                    .hasMessageContaining("No se ha encontrado la receta solicitada");
        }
    }
}
