package com.familyfood.application.mapper;

import com.familyfood.application.dto.recipe.CreateRecipeRequest;
import com.familyfood.application.dto.recipe.RecipeIngredientDTO;
import com.familyfood.application.dto.recipe.RecipeResponse;
import com.familyfood.application.dto.recipe.UpdateRecipeRequest;
import com.familyfood.domain.enums.EtiquetaReceta;
import com.familyfood.domain.model.Recipe;
import com.familyfood.domain.model.RecipeIngredient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@DisplayName("RecipeMapper Tests")
class RecipeMapperTest {

    private RecipeMapper recipeMapper;

    private UUID recipeId;
    private UUID userId;
    private Recipe fullRecipe;
    private RecipeIngredient ingredient1;
    private RecipeIngredient ingredient2;
    private RecipeIngredientDTO ingredientDTO1;
    private RecipeIngredientDTO ingredientDTO2;

    @BeforeEach
    void setUp() {
        recipeMapper = new RecipeMapperImpl();

        recipeId = UUID.randomUUID();
        userId = UUID.randomUUID();

        ingredient1 = RecipeIngredient.builder()
                .nombre("Tomate")
                .cantidad(2.0)
                .unidad("unidades")
                .build();

        ingredient2 = RecipeIngredient.builder()
                .nombre("Cebolla")
                .cantidad(1.0)
                .unidad("unidad")
                .build();

        ingredientDTO1 = new RecipeIngredientDTO("Tomate", 2.0, "unidades");
        ingredientDTO2 = new RecipeIngredientDTO("Cebolla", 1.0, "unidad");

        fullRecipe = Recipe.builder()
                .id(recipeId)
                .nombre("Ensalada")
                .descripcion("Ensalada fresca de verano")
                .tiempoMinutos(15)
                .raciones(2)
                .ingredientes(List.of(ingredient1, ingredient2))
                .pasos(List.of("Cortar verduras", "Mezclar", "Aliñar"))
                .etiquetas(List.of(EtiquetaReceta.RAPIDA, EtiquetaReceta.ECONOMICA))
                .imagen("ensalada.jpg")
                .favorita(true)
                .userId(userId)
                .version(1L)
                .build();
    }

    @Nested
    @DisplayName("toResponse(Recipe)")
    class ToResponse {

        @Test
        @DisplayName("should map all fields from Recipe to RecipeResponse")
        void shouldMapAllFields() {
            // Given
            Recipe recipe = fullRecipe;

            // When
            RecipeResponse response = recipeMapper.toResponse(recipe);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(recipeId);
            assertThat(response.nombre()).isEqualTo("Ensalada");
            assertThat(response.descripcion()).isEqualTo("Ensalada fresca de verano");
            assertThat(response.tiempoMinutos()).isEqualTo(15);
            assertThat(response.raciones()).isEqualTo(2);
            assertThat(response.imagen()).isEqualTo("ensalada.jpg");
            assertThat(response.favorita()).isTrue();
            assertThat(response.version()).isEqualTo(1L);

            assertThat(response.ingredientes())
                    .hasSize(2)
                    .extracting(RecipeIngredientDTO::nombre, RecipeIngredientDTO::cantidad, RecipeIngredientDTO::unidad)
                    .containsExactly(
                            tuple("Tomate", 2.0, "unidades"),
                            tuple("Cebolla", 1.0, "unidad")
                    );

            assertThat(response.pasos())
                    .containsExactly("Cortar verduras", "Mezclar", "Aliñar");

            assertThat(response.etiquetas())
                    .containsExactly(EtiquetaReceta.RAPIDA, EtiquetaReceta.ECONOMICA);
        }

        @Test
        @DisplayName("should map recipe with empty ingredients list")
        void shouldMapWithEmptyIngredients() {
            // Given
            Recipe recipe = Recipe.builder()
                    .id(recipeId)
                    .nombre("Solo nombre")
                    .descripcion("Sin ingredientes")
                    .tiempoMinutos(5)
                    .raciones(1)
                    .ingredientes(Collections.emptyList())
                    .pasos(List.of("Hacer"))
                    .etiquetas(Collections.emptyList())
                    .favorita(false)
                    .userId(userId)
                    .version(0L)
                    .build();

            // When
            RecipeResponse response = recipeMapper.toResponse(recipe);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.ingredientes()).isEmpty();
            assertThat(response.pasos()).containsExactly("Hacer");
            assertThat(response.etiquetas()).isEmpty();
            assertThat(response.favorita()).isFalse();
        }

        @Test
        @DisplayName("should map recipe with null optional fields")
        void shouldMapWithNullOptionalFields() {
            // Given
            Recipe recipe = Recipe.builder()
                    .id(recipeId)
                    .nombre("Minimal")
                    .descripcion("Receta mínima")
                    .tiempoMinutos(null)
                    .raciones(null)
                    .ingredientes(List.of(ingredient1))
                    .pasos(List.of("Paso 1"))
                    .etiquetas(null)
                    .imagen(null)
                    .favorita(false)
                    .userId(userId)
                    .version(0L)
                    .build();

            // When
            RecipeResponse response = recipeMapper.toResponse(recipe);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.tiempoMinutos()).isNull();
            assertThat(response.raciones()).isNull();
            assertThat(response.imagen()).isNull();
            assertThat(response.etiquetas()).isNull();
            assertThat(response.favorita()).isFalse();
        }
    }

    @Nested
    @DisplayName("toResponseList(List<Recipe>)")
    class ToResponseList {

        @Test
        @DisplayName("should map list of Recipes to list of RecipeResponses")
        void shouldMapList() {
            // Given
            Recipe recipe2 = Recipe.builder()
                    .id(UUID.randomUUID())
                    .nombre("Sopa")
                    .descripcion("Sopa de verduras")
                    .tiempoMinutos(30)
                    .raciones(4)
                    .ingredientes(List.of(ingredient1))
                    .pasos(List.of("Hervir", "Servir"))
                    .etiquetas(List.of(EtiquetaReceta.ECONOMICA))
                    .favorita(false)
                    .userId(userId)
                    .version(0L)
                    .build();

            List<Recipe> recipes = List.of(fullRecipe, recipe2);

            // When
            List<RecipeResponse> responses = recipeMapper.toResponseList(recipes);

            // Then
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).nombre()).isEqualTo("Ensalada");
            assertThat(responses.get(1).nombre()).isEqualTo("Sopa");
        }

        @Test
        @DisplayName("should return empty list when input list is empty")
        void shouldReturnEmptyList() {
            // Given
            List<Recipe> emptyList = Collections.emptyList();

            // When
            List<RecipeResponse> responses = recipeMapper.toResponseList(emptyList);

            // Then
            assertThat(responses).isEmpty();
        }
    }

    @Nested
    @DisplayName("toDomainFromCreate(CreateRecipeRequest)")
    class ToDomainFromCreate {

        @Test
        @DisplayName("should map all fields from CreateRecipeRequest to Recipe")
        void shouldMapAllFields() {
            // Given
            CreateRecipeRequest request = new CreateRecipeRequest(
                    "Ensalada",
                    "Ensalada fresca de verano",
                    15,
                    2,
                    List.of(ingredientDTO1, ingredientDTO2),
                    List.of("Cortar verduras", "Mezclar", "Aliñar"),
                    List.of(EtiquetaReceta.RAPIDA),
                    true
            );

            // When
            Recipe recipe = recipeMapper.toDomainFromCreate(request);

            // Then
            assertThat(recipe).isNotNull();
            assertThat(recipe.getId()).isNull(); // ignored
            assertThat(recipe.getNombre()).isEqualTo("Ensalada");
            assertThat(recipe.getDescripcion()).isEqualTo("Ensalada fresca de verano");
            assertThat(recipe.getTiempoMinutos()).isEqualTo(15);
            assertThat(recipe.getRaciones()).isEqualTo(2);
            assertThat(recipe.getImagen()).isNull(); // ignored
            assertThat(recipe.getUserId()).isNull(); // ignored
            assertThat(recipe.getVersion()).isEqualTo(0L); // @Builder.Default value
            assertThat(recipe.isFavorita()).isTrue();

            assertThat(recipe.getIngredientes())
                    .hasSize(2)
                    .extracting(RecipeIngredient::getNombre, RecipeIngredient::getCantidad, RecipeIngredient::getUnidad)
                    .containsExactly(
                            tuple("Tomate", 2.0, "unidades"),
                            tuple("Cebolla", 1.0, "unidad")
                    );

            assertThat(recipe.getPasos())
                    .containsExactly("Cortar verduras", "Mezclar", "Aliñar");

            assertThat(recipe.getEtiquetas())
                    .containsExactly(EtiquetaReceta.RAPIDA);
        }

        @Test
        @DisplayName("should map request with favorita=false")
        void shouldMapWithFavoritaFalse() {
            // Given
            CreateRecipeRequest request = new CreateRecipeRequest(
                    "Sopa",
                    "Sopa simple",
                    20,
                    4,
                    List.of(ingredientDTO1),
                    List.of("Hervir"),
                    null,
                    false
            );

            // When
            Recipe recipe = recipeMapper.toDomainFromCreate(request);

            // Then
            assertThat(recipe).isNotNull();
            assertThat(recipe.getNombre()).isEqualTo("Sopa");
            assertThat(recipe.isFavorita()).isFalse();
            assertThat(recipe.getEtiquetas()).isNull();
        }
    }

    @Nested
    @DisplayName("toDomainFromUpdate(UpdateRecipeRequest)")
    class ToDomainFromUpdate {

        @Test
        @DisplayName("should map all fields from UpdateRecipeRequest to Recipe")
        void shouldMapAllFields() {
            // Given
            UpdateRecipeRequest request = new UpdateRecipeRequest(
                    "Ensalada actualizada",
                    "Descripción actualizada",
                    20,
                    3,
                    List.of(ingredientDTO1),
                    List.of("Paso nuevo"),
                    List.of(EtiquetaReceta.NINOS),
                    2L
            );

            // When
            Recipe recipe = recipeMapper.toDomainFromUpdate(request);

            // Then
            assertThat(recipe).isNotNull();
            assertThat(recipe.getId()).isNull(); // ignored
            assertThat(recipe.getNombre()).isEqualTo("Ensalada actualizada");
            assertThat(recipe.getDescripcion()).isEqualTo("Descripción actualizada");
            assertThat(recipe.getTiempoMinutos()).isEqualTo(20);
            assertThat(recipe.getRaciones()).isEqualTo(3);
            assertThat(recipe.getImagen()).isNull(); // ignored
            assertThat(recipe.getUserId()).isNull(); // ignored
            assertThat(recipe.isFavorita()).isFalse(); // ignored (default)
            assertThat(recipe.getVersion()).isEqualTo(2L);

            assertThat(recipe.getIngredientes())
                    .hasSize(1)
                    .extracting(RecipeIngredient::getNombre)
                    .containsExactly("Tomate");

            assertThat(recipe.getPasos())
                    .containsExactly("Paso nuevo");

            assertThat(recipe.getEtiquetas())
                    .containsExactly(EtiquetaReceta.NINOS);
        }
    }

    @Nested
    @DisplayName("toIngredientDTO(RecipeIngredient)")
    class ToIngredientDTO {

        @Test
        @DisplayName("should map RecipeIngredient to RecipeIngredientDTO")
        void shouldMapIngredient() {
            // Given
            RecipeIngredient ingredient = ingredient1;

            // When
            RecipeIngredientDTO dto = recipeMapper.toIngredientDTO(ingredient);

            // Then
            assertThat(dto).isNotNull();
            assertThat(dto.nombre()).isEqualTo("Tomate");
            assertThat(dto.cantidad()).isEqualTo(2.0);
            assertThat(dto.unidad()).isEqualTo("unidades");
        }
    }

    @Nested
    @DisplayName("toIngredientDomain(RecipeIngredientDTO)")
    class ToIngredientDomain {

        @Test
        @DisplayName("should map RecipeIngredientDTO to RecipeIngredient")
        void shouldMapDTOToDomain() {
            // Given
            RecipeIngredientDTO dto = ingredientDTO1;

            // When
            RecipeIngredient ingredient = recipeMapper.toIngredientDomain(dto);

            // Then
            assertThat(ingredient).isNotNull();
            assertThat(ingredient.getNombre()).isEqualTo("Tomate");
            assertThat(ingredient.getCantidad()).isEqualTo(2.0);
            assertThat(ingredient.getUnidad()).isEqualTo("unidades");
        }
    }
}
