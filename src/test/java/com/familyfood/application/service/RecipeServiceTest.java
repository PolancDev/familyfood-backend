package com.familyfood.application.service;

import com.familyfood.application.dto.recipe.CreateRecipeRequest;
import com.familyfood.application.dto.recipe.RecipeIngredientDTO;
import com.familyfood.application.dto.recipe.RecipeListResponse;
import com.familyfood.application.dto.recipe.RecipeResponse;
import com.familyfood.application.dto.recipe.UpdateRecipeRequest;
import com.familyfood.application.mapper.RecipeMapper;
import com.familyfood.application.port.repository.FamilyMemberRepository;
import com.familyfood.application.port.repository.RecipeRepository;
import com.familyfood.application.port.repository.UserRepository;
import com.familyfood.domain.enums.EtiquetaReceta;
import com.familyfood.domain.exception.RecipeNotFoundException;
import com.familyfood.domain.exception.UnauthorizedException;
import com.familyfood.domain.model.FamilyMember;
import com.familyfood.domain.model.Recipe;
import com.familyfood.domain.model.RecipeIngredient;
import com.familyfood.domain.model.User;
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

    @Mock
    private FamilyMemberRepository familyMemberRepository;

    @Mock
    private UserRepository userRepository;

    private RecipeService recipeService;

    private UUID userId;
    private UUID recipeId;
    private UUID familyGroupId;
    private Recipe testRecipe;
    private RecipeResponse testRecipeResponse;
    private CreateRecipeRequest createRequest;
    private UpdateRecipeRequest updateRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        recipeService = new RecipeService(recipeRepository, recipeMapper, familyMemberRepository, userRepository);

        userId = UUID.randomUUID();
        recipeId = UUID.randomUUID();
        familyGroupId = UUID.randomUUID();

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
                .familyGroupId(null)
                .version(0L)
                .build();

        List<RecipeIngredientDTO> ingredientDTOs = List.of(
                new RecipeIngredientDTO("Tomate", 2.0, "unidades")
        );

        testRecipeResponse = new RecipeResponse(
                recipeId, "Ensalada", "Ensalada fresca", 15, 2,
                ingredientDTOs, List.of("Cortar", "Mezclar"),
                List.of(EtiquetaReceta.RAPIDA), null, false, "Usuario Test"
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

        testUser = User.builder().id(userId).nombre("Usuario Test").email("test@test.com").build();
    }

    /**
     * Helper: configura los mocks comunes para toResponseWithCreator.
     * RecipeService.toResponseWithCreator() llama a recipeMapper.toResponse() y userRepository.findById().
     */
    private void setupCreatorMocks() {
        when(recipeMapper.toResponse(any(Recipe.class))).thenReturn(testRecipeResponse);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    }

    @Nested
    @DisplayName("Listar recetas")
    class ListarRecetasTests {

        @Test
        @DisplayName("Should list all recipes for user without family")
        void shouldListAllRecipesForUserWithoutFamily() {
            when(familyMemberRepository.findByUserId(userId)).thenReturn(List.of());
            when(recipeRepository.findByUserId(userId)).thenReturn(List.of(testRecipe));
            setupCreatorMocks();

            RecipeListResponse response = recipeService.listarRecetas(userId, null, null, null);

            assertThat(response).isNotNull();
            assertThat(response.recetas()).hasSize(1);
            assertThat(response.recetas().get(0).nombre()).isEqualTo("Ensalada");
            verify(recipeRepository).findByUserId(userId);
        }

        @Test
        @DisplayName("Should list all recipes for user with family")
        void shouldListAllRecipesForUserWithFamily() {
            FamilyMember membership = FamilyMember.builder()
                    .id(UUID.randomUUID()).userId(userId).familyGroupId(familyGroupId)
                    .role(com.familyfood.domain.enums.FamilyRole.ADMIN).build();
            when(familyMemberRepository.findByUserId(userId)).thenReturn(List.of(membership));
            when(recipeRepository.findByFamilyGroupId(familyGroupId)).thenReturn(List.of(testRecipe));
            setupCreatorMocks();

            RecipeListResponse response = recipeService.listarRecetas(userId, null, null, null);

            assertThat(response).isNotNull();
            assertThat(response.recetas()).hasSize(1);
            verify(recipeRepository).findByFamilyGroupId(familyGroupId);
        }

        @Test
        @DisplayName("Should list favorite recipes when favoritas=true and no family")
        void shouldListFavoriteRecipesNoFamily() {
            when(familyMemberRepository.findByUserId(userId)).thenReturn(List.of());
            when(recipeRepository.findByFavoritaTrueAndUserId(userId)).thenReturn(List.of(testRecipe));
            setupCreatorMocks();

            RecipeListResponse response = recipeService.listarRecetas(userId, true, null, null);

            assertThat(response).isNotNull();
            assertThat(response.recetas()).hasSize(1);
            verify(recipeRepository).findByFavoritaTrueAndUserId(userId);
        }

        @Test
        @DisplayName("Should list favorite recipes when favoritas=true with family")
        void shouldListFavoriteRecipesWithFamily() {
            FamilyMember membership = FamilyMember.builder()
                    .id(UUID.randomUUID()).userId(userId).familyGroupId(familyGroupId)
                    .role(com.familyfood.domain.enums.FamilyRole.ADMIN).build();
            when(familyMemberRepository.findByUserId(userId)).thenReturn(List.of(membership));
            when(recipeRepository.findByFavoritaTrueAndFamilyGroupId(familyGroupId)).thenReturn(List.of(testRecipe));
            setupCreatorMocks();

            RecipeListResponse response = recipeService.listarRecetas(userId, true, null, null);

            assertThat(response).isNotNull();
            assertThat(response.recetas()).hasSize(1);
            verify(recipeRepository).findByFavoritaTrueAndFamilyGroupId(familyGroupId);
        }

        @Test
        @DisplayName("Should search recipes by name for user without family")
        void shouldSearchRecipesByNameNoFamily() {
            when(familyMemberRepository.findByUserId(userId)).thenReturn(List.of());
            when(recipeRepository.findByNombreContainingIgnoreCaseAndUserId("ensalada", userId))
                    .thenReturn(List.of(testRecipe));
            setupCreatorMocks();

            RecipeListResponse response = recipeService.listarRecetas(userId, null, "ensalada", null);

            assertThat(response).isNotNull();
            assertThat(response.recetas()).hasSize(1);
            verify(recipeRepository).findByNombreContainingIgnoreCaseAndUserId("ensalada", userId);
        }

        @Test
        @DisplayName("Should search recipes by name for user with family")
        void shouldSearchRecipesByNameWithFamily() {
            FamilyMember membership = FamilyMember.builder()
                    .id(UUID.randomUUID()).userId(userId).familyGroupId(familyGroupId)
                    .role(com.familyfood.domain.enums.FamilyRole.ADMIN).build();
            when(familyMemberRepository.findByUserId(userId)).thenReturn(List.of(membership));
            when(recipeRepository.findByNombreContainingIgnoreCaseAndFamilyGroupId("ensalada", familyGroupId))
                    .thenReturn(List.of(testRecipe));
            setupCreatorMocks();

            RecipeListResponse response = recipeService.listarRecetas(userId, null, "ensalada", null);

            assertThat(response).isNotNull();
            assertThat(response.recetas()).hasSize(1);
            verify(recipeRepository).findByNombreContainingIgnoreCaseAndFamilyGroupId("ensalada", familyGroupId);
        }

        @Test
        @DisplayName("Should filter recipes by etiqueta for user without family")
        void shouldFilterByEtiquetaNoFamily() {
            when(familyMemberRepository.findByUserId(userId)).thenReturn(List.of());
            when(recipeRepository.findByEtiquetasContainsAndUserId("RAPIDA", userId))
                    .thenReturn(List.of(testRecipe));
            setupCreatorMocks();

            RecipeListResponse response = recipeService.listarRecetas(userId, null, null, "RAPIDA");

            assertThat(response).isNotNull();
            assertThat(response.recetas()).hasSize(1);
            verify(recipeRepository).findByEtiquetasContainsAndUserId("RAPIDA", userId);
        }

        @Test
        @DisplayName("Should filter recipes by etiqueta for user with family")
        void shouldFilterByEtiquetaWithFamily() {
            FamilyMember membership = FamilyMember.builder()
                    .id(UUID.randomUUID()).userId(userId).familyGroupId(familyGroupId)
                    .role(com.familyfood.domain.enums.FamilyRole.ADMIN).build();
            when(familyMemberRepository.findByUserId(userId)).thenReturn(List.of(membership));
            when(recipeRepository.findByEtiquetasContainsAndFamilyGroupId("RAPIDA", familyGroupId))
                    .thenReturn(List.of(testRecipe));
            setupCreatorMocks();

            RecipeListResponse response = recipeService.listarRecetas(userId, null, null, "RAPIDA");

            assertThat(response).isNotNull();
            assertThat(response.recetas()).hasSize(1);
            verify(recipeRepository).findByEtiquetasContainsAndFamilyGroupId("RAPIDA", familyGroupId);
        }
    }

    @Nested
    @DisplayName("Obtener receta")
    class ObtenerRecetaTests {

        @Test
        @DisplayName("Should return recipe when found with creator name")
        void shouldReturnRecipeWhenFound() {
            when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(testRecipe));
            setupCreatorMocks();

            RecipeResponse response = recipeService.obtenerReceta(recipeId);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(recipeId);
            assertThat(response.nombre()).isEqualTo("Ensalada");
            assertThat(response.nombreCreador()).isEqualTo("Usuario Test");
        }

        @Test
        @DisplayName("Should return recipe with 'Desconocido' when creator not found")
        void shouldReturnDesconocidoWhenCreatorNotFound() {
            when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(testRecipe));
            when(recipeMapper.toResponse(testRecipe)).thenReturn(testRecipeResponse);
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            RecipeResponse response = recipeService.obtenerReceta(recipeId);

            assertThat(response).isNotNull();
            assertThat(response.nombreCreador()).isEqualTo("Desconocido");
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
        @DisplayName("Should create recipe without family")
        void shouldCreateRecipeWithoutFamily() {
            when(familyMemberRepository.findByUserId(userId)).thenReturn(List.of());
            when(recipeMapper.toDomainFromCreate(createRequest)).thenReturn(testRecipe);
            when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);
            setupCreatorMocks();

            RecipeResponse response = recipeService.crearReceta(createRequest, userId);

            assertThat(response).isNotNull();
            assertThat(response.nombre()).isEqualTo("Ensalada");
            verify(recipeRepository).save(any(Recipe.class));
        }

        @Test
        @DisplayName("Should create recipe with family")
        void shouldCreateRecipeWithFamily() {
            FamilyMember membership = FamilyMember.builder()
                    .id(UUID.randomUUID()).userId(userId).familyGroupId(familyGroupId)
                    .role(com.familyfood.domain.enums.FamilyRole.ADMIN).build();
            when(familyMemberRepository.findByUserId(userId)).thenReturn(List.of(membership));
            when(recipeMapper.toDomainFromCreate(createRequest)).thenReturn(testRecipe);
            when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);
            setupCreatorMocks();

            RecipeResponse response = recipeService.crearReceta(createRequest, userId);

            assertThat(response).isNotNull();
            verify(recipeRepository).save(any(Recipe.class));
        }
    }

    @Nested
    @DisplayName("Actualizar receta")
    class ActualizarRecetaTests {

        @Test
        @DisplayName("Should update recipe successfully when user is author")
        void shouldUpdateRecipeSuccessfully() {
            when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(testRecipe));
            when(recipeMapper.toDomainFromUpdate(updateRequest)).thenReturn(testRecipe);
            when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);
            setupCreatorMocks();

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

        @Test
        @DisplayName("Should throw UnauthorizedException when user is not author or admin")
        void shouldThrowUnauthorizedWhenNotOwner() {
            UUID otherUserId = UUID.randomUUID();
            when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(testRecipe));

            assertThatThrownBy(() -> recipeService.actualizarReceta(recipeId, updateRequest, otherUserId))
                    .isInstanceOf(UnauthorizedException.class);
        }
    }

    @Nested
    @DisplayName("Eliminar receta")
    class EliminarRecetaTests {

        @Test
        @DisplayName("Should delete recipe successfully when user is author")
        void shouldDeleteRecipeSuccessfully() {
            when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(testRecipe));

            recipeService.eliminarReceta(recipeId, userId);

            verify(recipeRepository).deleteById(recipeId);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent recipe")
        void shouldThrowExceptionWhenNotFound() {
            when(recipeRepository.findById(recipeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> recipeService.eliminarReceta(recipeId, userId))
                    .isInstanceOf(RecipeNotFoundException.class)
                    .hasMessageContaining("No se ha encontrado la receta solicitada");
        }

        @Test
        @DisplayName("Should throw UnauthorizedException when user is not author or admin")
        void shouldThrowUnauthorizedWhenNotOwner() {
            UUID otherUserId = UUID.randomUUID();
            when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(testRecipe));

            assertThatThrownBy(() -> recipeService.eliminarReceta(recipeId, otherUserId))
                    .isInstanceOf(UnauthorizedException.class);
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
            setupCreatorMocks();

            RecipeResponse response = recipeService.toggleFavorita(recipeId, userId);

            assertThat(response).isNotNull();
            verify(recipeRepository).findById(recipeId);
            verify(recipeRepository).save(any(Recipe.class));
        }

        @Test
        @DisplayName("Should throw exception when toggling non-existent recipe")
        void shouldThrowExceptionWhenNotFound() {
            when(recipeRepository.findById(recipeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> recipeService.toggleFavorita(recipeId, userId))
                    .isInstanceOf(RecipeNotFoundException.class)
                    .hasMessageContaining("No se ha encontrado la receta solicitada");
        }
    }
}