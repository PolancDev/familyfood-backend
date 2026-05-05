package com.familyfood.infrastructure.adapter.persistence.adapters;

import com.familyfood.domain.enums.EtiquetaReceta;
import com.familyfood.domain.model.Recipe;
import com.familyfood.domain.model.RecipeIngredient;
import com.familyfood.infrastructure.adapter.persistence.entities.RecipeEntity;
import com.familyfood.infrastructure.adapter.persistence.entities.RecipeIngredientEntity;
import com.familyfood.infrastructure.adapter.persistence.repository.SpringDataRecipeRepository;
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
@DisplayName("RecipeRepositoryAdapter Tests")
class RecipeRepositoryAdapterTest {

    @Mock
    private SpringDataRecipeRepository springDataRepository;

    @Mock
    private RecipeEntityMapper entityMapper;

    private RecipeRepositoryAdapter adapter;

    private UUID recipeId;
    private UUID userId;
    private Recipe testRecipe;
    private RecipeEntity testEntity;

    @BeforeEach
    void setUp() {
        adapter = new RecipeRepositoryAdapter(springDataRepository, entityMapper);

        recipeId = UUID.randomUUID();
        userId = UUID.randomUUID();

        List<RecipeIngredient> ingredients = List.of(
                RecipeIngredient.builder().nombre("Tomate").cantidad(2.0).unidad("unidades").build()
        );

        List<RecipeIngredientEntity> ingredientEntities = List.of(
                RecipeIngredientEntity.builder().nombre("Tomate").cantidad(2.0).unidad("unidades").build()
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

        testEntity = RecipeEntity.builder()
                .id(recipeId)
                .nombre("Ensalada")
                .descripcion("Ensalada fresca")
                .tiempoMinutos(15)
                .raciones(2)
                .ingredientes(ingredientEntities)
                .pasos(List.of("Cortar", "Mezclar"))
                .etiquetas(List.of(EtiquetaReceta.RAPIDA))
                .favorita(false)
                .userId(userId)
                .version(0L)
                .build();
    }

    @Nested
    @DisplayName("Save recipe")
    class SaveTests {

        @Test
        @DisplayName("Should create new recipe when id is null")
        void shouldCreateNewRecipe() {
            Recipe newRecipe = Recipe.builder()
                    .nombre("Nueva")
                    .descripcion("Desc")
                    .tiempoMinutos(10)
                    .raciones(1)
                    .ingredientes(List.of())
                    .pasos(List.of("Paso 1"))
                    .favorita(false)
                    .userId(userId)
                    .build();

            RecipeEntity newEntity = RecipeEntity.builder()
                    .nombre("Nueva")
                    .descripcion("Desc")
                    .tiempoMinutos(10)
                    .raciones(1)
                    .ingredientes(List.of())
                    .pasos(List.of("Paso 1"))
                    .favorita(false)
                    .userId(userId)
                    .build();

            when(entityMapper.toEntityForCreate(newRecipe)).thenReturn(newEntity);
            when(springDataRepository.save(newEntity)).thenReturn(testEntity);
            when(entityMapper.toDomain(testEntity)).thenReturn(testRecipe);

            Recipe result = adapter.save(newRecipe);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(recipeId);
            verify(entityMapper).toEntityForCreate(newRecipe);
            verify(entityMapper, never()).toEntityForUpdate(any());
            verify(springDataRepository).save(newEntity);
        }

        @Test
        @DisplayName("Should update existing recipe when id is not null")
        void shouldUpdateExistingRecipe() {
            when(entityMapper.toEntityForUpdate(testRecipe)).thenReturn(testEntity);
            when(springDataRepository.save(testEntity)).thenReturn(testEntity);
            when(entityMapper.toDomain(testEntity)).thenReturn(testRecipe);

            Recipe result = adapter.save(testRecipe);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(recipeId);
            verify(entityMapper).toEntityForUpdate(testRecipe);
            verify(entityMapper, never()).toEntityForCreate(any());
            verify(springDataRepository).save(testEntity);
        }
    }

    @Nested
    @DisplayName("Find by id")
    class FindByIdTests {

        @Test
        @DisplayName("Should return recipe when found")
        void shouldReturnRecipeWhenFound() {
            when(springDataRepository.findById(recipeId)).thenReturn(Optional.of(testEntity));
            when(entityMapper.toDomain(testEntity)).thenReturn(testRecipe);

            Optional<Recipe> result = adapter.findById(recipeId);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(recipeId);
            assertThat(result.get().getNombre()).isEqualTo("Ensalada");
        }

        @Test
        @DisplayName("Should return empty when not found")
        void shouldReturnEmptyWhenNotFound() {
            when(springDataRepository.findById(recipeId)).thenReturn(Optional.empty());

            Optional<Recipe> result = adapter.findById(recipeId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Find by user id")
    class FindByUserIdTests {

        @Test
        @DisplayName("Should return recipes for user")
        void shouldReturnRecipesForUser() {
            when(springDataRepository.findByUserIdOrderByNombreAsc(userId)).thenReturn(List.of(testEntity));
            when(entityMapper.toDomainList(anyList())).thenReturn(List.of(testRecipe));

            List<Recipe> results = adapter.findByUserId(userId);

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getNombre()).isEqualTo("Ensalada");
        }
    }

    @Nested
    @DisplayName("Find by favorita")
    class FindByFavoritaTests {

        @Test
        @DisplayName("Should return favorite recipes")
        void shouldReturnFavoriteRecipes() {
            when(springDataRepository.findByFavoritaTrueAndUserIdOrderByNombreAsc(userId))
                    .thenReturn(List.of(testEntity));
            when(entityMapper.toDomainList(anyList())).thenReturn(List.of(testRecipe));

            List<Recipe> results = adapter.findByFavoritaTrueAndUserId(userId);

            assertThat(results).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Find by etiqueta")
    class FindByEtiquetaTests {

        @Test
        @DisplayName("Should return recipes with given etiqueta")
        void shouldReturnRecipesWithEtiqueta() {
            when(springDataRepository.findByEtiquetasContainsAndUserIdOrderByNombreAsc(
                    EtiquetaReceta.RAPIDA, userId)).thenReturn(List.of(testEntity));
            when(entityMapper.toDomainList(anyList())).thenReturn(List.of(testRecipe));

            List<Recipe> results = adapter.findByEtiquetasContainsAndUserId(EtiquetaReceta.RAPIDA, userId);

            assertThat(results).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Find by nombre")
    class FindByNombreTests {

        @Test
        @DisplayName("Should return recipes matching search")
        void shouldReturnMatchingRecipes() {
            when(springDataRepository.findByNombreContainingIgnoreCaseAndUserIdOrderByNombreAsc(
                    "ensalada", userId)).thenReturn(List.of(testEntity));
            when(entityMapper.toDomainList(anyList())).thenReturn(List.of(testRecipe));

            List<Recipe> results = adapter.findByNombreContainingIgnoreCaseAndUserId("ensalada", userId);

            assertThat(results).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Delete and exists")
    class DeleteAndExistsTests {

        @Test
        @DisplayName("Should delete recipe by id")
        void shouldDeleteRecipe() {
            adapter.deleteById(recipeId);
            verify(springDataRepository).deleteById(recipeId);
        }

        @Test
        @DisplayName("Should return true when recipe exists")
        void shouldReturnTrueWhenExists() {
            when(springDataRepository.existsById(recipeId)).thenReturn(true);
            assertThat(adapter.existsById(recipeId)).isTrue();
        }

        @Test
        @DisplayName("Should return false when recipe does not exist")
        void shouldReturnFalseWhenNotExists() {
            when(springDataRepository.existsById(recipeId)).thenReturn(false);
            assertThat(adapter.existsById(recipeId)).isFalse();
        }
    }
}
