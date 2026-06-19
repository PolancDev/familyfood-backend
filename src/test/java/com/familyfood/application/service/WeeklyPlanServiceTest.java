package com.familyfood.application.service;

import com.familyfood.application.dto.plan.*;
import com.familyfood.application.mapper.WeeklyPlanMapper;
import com.familyfood.application.port.repository.FamilyMemberRepository;
import com.familyfood.application.port.repository.RecipeRepository;
import com.familyfood.application.port.repository.UserRepository;
import com.familyfood.application.port.repository.WeeklyPlanRepository;
import com.familyfood.domain.enums.*;
import com.familyfood.domain.exception.*;
import com.familyfood.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WeeklyPlanService Tests")
class WeeklyPlanServiceTest {

    @Mock
    private WeeklyPlanRepository weeklyPlanRepository;
    @Mock
    private RecipeRepository recipeRepository;
    @Mock
    private FamilyMemberRepository familyMemberRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private WeeklyPlanMapper weeklyPlanMapper;

    private WeeklyPlanService weeklyPlanService;

    private UUID userId;
    private UUID adminUserId;
    private UUID familyGroupId;
    private UUID recipeId;
    private UUID planId;
    private WeeklyPlan testPlan;
    private Recipe testRecipe;

    @BeforeEach
    void setUp() {
        weeklyPlanService = new WeeklyPlanService(
                weeklyPlanRepository, recipeRepository,
                familyMemberRepository, userRepository, weeklyPlanMapper);

        userId = UUID.randomUUID();
        adminUserId = UUID.randomUUID();
        familyGroupId = UUID.randomUUID();
        recipeId = UUID.randomUUID();
        planId = UUID.randomUUID();

        testRecipe = Recipe.builder()
                .id(recipeId)
                .nombre("Paella")
                .descripcion("Paella valenciana")
                .tiempoMinutos(45)
                .raciones(4)
                .ingredientes(List.of(
                        RecipeIngredient.builder().nombre("Arroz").cantidad(300.0).unidad("g").build(),
                        RecipeIngredient.builder().nombre("Marisco").cantidad(500.0).unidad("g").build()
                ))
                .pasos(List.of("Cocinar"))
                .etiquetas(List.of(EtiquetaReceta.RAPIDA))
                .favorita(true)
                .userId(adminUserId)
                .familyGroupId(familyGroupId)
                .version(0L)
                .build();

        LocalDate startDate = LocalDate.of(2026, 6, 15);
        LocalDate endDate = LocalDate.of(2026, 6, 21);

        List<PlanDay> dias = new ArrayList<>();
        for (DiaSemana dia : DiaSemana.values()) {
            for (TipoComida tipo : TipoComida.values()) {
                dias.add(PlanDay.builder()
                        .id(UUID.randomUUID())
                        .weeklyPlanId(planId)
                        .dia(dia)
                        .tipo(tipo)
                        .recetaId(recipeId)
                        .recetaNombre("Paella")
                        .recetaTiempoMinutos(45)
                        .estado(EstadoDia.NORMAL)
                        .alergenosAdvertencia(false)
                        .version(0L)
                        .build());
            }
        }

        testPlan = WeeklyPlan.builder()
                .id(planId)
                .familyGroupId(familyGroupId)
                .year(2026)
                .weekNumber(25)
                .startDate(startDate)
                .endDate(endDate)
                .dias(dias)
                .version(0L)
                .build();
    }

    // ========== OBTENER PLAN ACTUAL ==========

    @Nested
    @DisplayName("obtenerPlanActual")
    class ObtenerPlanActualTests {

        @Test
        @DisplayName("debe devolver el plan semanal si existe")
        void debeDevolverPlanExistente() {
            when(familyMemberRepository.existsByUserIdAndFamilyGroupId(userId, familyGroupId)).thenReturn(true);
            when(weeklyPlanRepository.findByFamilyGroupAndWeek(eq(familyGroupId), anyInt(), anyInt()))
                    .thenReturn(Optional.of(testPlan));
            when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(testRecipe));

            WeeklyPlanResponse response = weeklyPlanService.obtenerPlan(familyGroupId, null, null, userId);

            assertThat(response).isNotNull();
            assertThat(response.dias()).hasSize(14);
            verify(weeklyPlanRepository).findByFamilyGroupAndWeek(eq(familyGroupId), anyInt(), anyInt());
        }

        @Test
        @DisplayName("debe auto-crear plan vacío si no existe")
        void debeAutoCrearPlanVacioSiNoExiste() {
            when(familyMemberRepository.existsByUserIdAndFamilyGroupId(userId, familyGroupId)).thenReturn(true);
            when(weeklyPlanRepository.findByFamilyGroupAndWeek(eq(familyGroupId), anyInt(), anyInt()))
                    .thenReturn(Optional.empty());
            when(weeklyPlanRepository.save(any(WeeklyPlan.class))).thenAnswer(inv -> inv.getArgument(0));

            WeeklyPlanResponse response = weeklyPlanService.obtenerPlan(familyGroupId, null, null, userId);

            assertThat(response).isNotNull();
            assertThat(response.dias()).hasSize(14);
            assertThat(response.dias()).allMatch(d -> d.estado() == EstadoDia.IMPROVISADO);
            verify(weeklyPlanRepository).save(any(WeeklyPlan.class));
        }

        @Test
        @DisplayName("debe lanzar excepción si el usuario no pertenece a la familia")
        void debeLanzarExcepcionSiNoEsMiembro() {
            when(familyMemberRepository.existsByUserIdAndFamilyGroupId(userId, familyGroupId)).thenReturn(false);

            assertThatThrownBy(() -> weeklyPlanService.obtenerPlan(familyGroupId, null, null, userId))
                    .isInstanceOf(UnauthorizedException.class);
        }
    }

    // ========== GUARDAR PLAN ==========

    @Nested
    @DisplayName("guardarPlan")
    class GuardarPlanTests {

        @Test
        @DisplayName("debe guardar un plan nuevo correctamente")
        void debeGuardarPlanNuevo() {
            FamilyMember adminMember = FamilyMember.builder()
                    .userId(adminUserId).familyGroupId(familyGroupId)
                    .role(FamilyRole.ADMIN).build();

            when(familyMemberRepository.findByUserIdAndFamilyGroupId(adminUserId, familyGroupId))
                    .thenReturn(Optional.of(adminMember));
            when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(testRecipe));

            SaveWeeklyPlanRequest.PlanDayRequest dayRequest =
                    new SaveWeeklyPlanRequest.PlanDayRequest(
                            DiaSemana.LUNES, TipoComida.COMIDA, recipeId, EstadoDia.NORMAL, null, null);

            SaveWeeklyPlanRequest request = new SaveWeeklyPlanRequest(2026, 25, List.of(dayRequest));

            PlanDay mappedDay = PlanDay.builder()
                    .dia(DiaSemana.LUNES).tipo(TipoComida.COMIDA)
                    .recetaId(recipeId).estado(EstadoDia.NORMAL).build();

            WeeklyPlan mappedPlan = WeeklyPlan.builder()
                    .id(planId).familyGroupId(familyGroupId)
                    .year(2026).weekNumber(25)
                    .startDate(LocalDate.of(2026, 6, 15))
                    .endDate(LocalDate.of(2026, 6, 21))
                    .dias(List.of(mappedDay))
                    .build();

            when(weeklyPlanMapper.toDomain(any(SaveWeeklyPlanRequest.class))).thenReturn(mappedPlan);
            when(weeklyPlanMapper.toPlanDayDomain(any(SaveWeeklyPlanRequest.PlanDayRequest.class))).thenReturn(mappedDay);
            when(weeklyPlanRepository.save(any())).thenReturn(testPlan);

            WeeklyPlanResponse response = weeklyPlanService.guardarPlan(familyGroupId, request, adminUserId);

            assertThat(response).isNotNull();
            verify(weeklyPlanRepository).deleteByFamilyGroupAndWeek(familyGroupId, 2026, 25);
            verify(weeklyPlanRepository).save(any());
        }

        @Test
        @DisplayName("debe lanzar excepción si no es ADMIN")
        void debeLanzarExcepcionSiNoEsAdmin() {
            FamilyMember consumerMember = FamilyMember.builder()
                    .userId(userId).familyGroupId(familyGroupId)
                    .role(FamilyRole.CONSUMER).build();

            when(familyMemberRepository.findByUserIdAndFamilyGroupId(userId, familyGroupId))
                    .thenReturn(Optional.of(consumerMember));

            SaveWeeklyPlanRequest request = new SaveWeeklyPlanRequest(2026, 25, List.of());

            assertThatThrownBy(() -> weeklyPlanService.guardarPlan(familyGroupId, request, userId))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("administrador");
        }

        @Test
        @DisplayName("debe lanzar excepción si la receta no pertenece a la familia")
        void debeLanzarExcepcionSiRecetaNoEsDeLaFamilia() {
            FamilyMember adminMember = FamilyMember.builder()
                    .userId(adminUserId).familyGroupId(familyGroupId)
                    .role(FamilyRole.ADMIN).build();

            UUID otroGroupId = UUID.randomUUID();
            Recipe otraReceta = Recipe.builder()
                    .id(recipeId).nombre("Otra").familyGroupId(otroGroupId).build();

            when(familyMemberRepository.findByUserIdAndFamilyGroupId(adminUserId, familyGroupId))
                    .thenReturn(Optional.of(adminMember));
            when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(otraReceta));

            SaveWeeklyPlanRequest.PlanDayRequest dayRequest =
                    new SaveWeeklyPlanRequest.PlanDayRequest(
                            DiaSemana.LUNES, TipoComida.COMIDA, recipeId, EstadoDia.NORMAL, null, null);

            SaveWeeklyPlanRequest request = new SaveWeeklyPlanRequest(2026, 25, List.of(dayRequest));

            PlanDay mappedDay = PlanDay.builder()
                    .dia(DiaSemana.LUNES).tipo(TipoComida.COMIDA)
                    .recetaId(recipeId).estado(EstadoDia.NORMAL).build();

            WeeklyPlan mappedPlan = WeeklyPlan.builder()
                    .id(planId).familyGroupId(familyGroupId)
                    .year(2026).weekNumber(25)
                    .startDate(LocalDate.of(2026, 6, 15))
                    .endDate(LocalDate.of(2026, 6, 21))
                    .dias(new ArrayList<>())
                    .build();

            when(weeklyPlanMapper.toDomain(any(SaveWeeklyPlanRequest.class))).thenReturn(mappedPlan);
            when(weeklyPlanMapper.toPlanDayDomain(any(SaveWeeklyPlanRequest.PlanDayRequest.class))).thenReturn(mappedDay);

            assertThatThrownBy(() -> weeklyPlanService.guardarPlan(familyGroupId, request, adminUserId))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("receta");
        }
    }

    // ========== ACTUALIZAR DÍA ==========

    @Nested
    @DisplayName("actualizarDia")
    class ActualizarDiaTests {

        @Test
        @DisplayName("debe actualizar un slot correctamente")
        void debeActualizarSlot() {
            FamilyMember adminMember = FamilyMember.builder()
                    .userId(adminUserId).familyGroupId(familyGroupId)
                    .role(FamilyRole.ADMIN).build();

            when(familyMemberRepository.findByUserIdAndFamilyGroupId(adminUserId, familyGroupId))
                    .thenReturn(Optional.of(adminMember));
            when(weeklyPlanRepository.findByFamilyGroupAndWeek(familyGroupId, 2026, 25))
                    .thenReturn(Optional.of(testPlan));
            when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(testRecipe));

            PlanDay updatedDay = testPlan.getDias().get(0);
            updatedDay.setEstado(EstadoDia.COMER_FUERA);
            when(weeklyPlanRepository.updateDay(any())).thenReturn(updatedDay);

            UpdateDayRequest request = new UpdateDayRequest(
                    DiaSemana.LUNES, TipoComida.COMIDA, recipeId, EstadoDia.COMER_FUERA, null, null);

            PlanDayResponse response = weeklyPlanService.actualizarDia(
                    familyGroupId, 2026, 25, request, adminUserId);

            assertThat(response).isNotNull();
            assertThat(response.estado()).isEqualTo(EstadoDia.COMER_FUERA);
            verify(weeklyPlanRepository).updateDay(any());
        }

        @Test
        @DisplayName("debe lanzar excepción si el plan no existe")
        void debeLanzarExcepcionSiPlanNoExiste() {
            FamilyMember adminMember = FamilyMember.builder()
                    .userId(adminUserId).familyGroupId(familyGroupId)
                    .role(FamilyRole.ADMIN).build();

            when(familyMemberRepository.findByUserIdAndFamilyGroupId(adminUserId, familyGroupId))
                    .thenReturn(Optional.of(adminMember));
            when(weeklyPlanRepository.findByFamilyGroupAndWeek(familyGroupId, 2026, 25))
                    .thenReturn(Optional.empty());

            UpdateDayRequest request = new UpdateDayRequest(
                    DiaSemana.LUNES, TipoComida.COMIDA, null, EstadoDia.IMPROVISADO, null, null);

            assertThatThrownBy(() -> weeklyPlanService.actualizarDia(
                    familyGroupId, 2026, 25, request, adminUserId))
                    .isInstanceOf(WeeklyPlanNotFoundException.class);
        }
    }

    // ========== GENERAR MENÚ ==========

    @Nested
    @DisplayName("generarMenu")
    class GenerarMenuTests {

        @Test
        @DisplayName("debe generar un menú con 14 slots")
        void debeGenerarMenuCon14Slots() {
            FamilyMember adminMember = FamilyMember.builder()
                    .userId(adminUserId).familyGroupId(familyGroupId)
                    .role(FamilyRole.ADMIN).build();

            // Crear 14 recetas suficientes
            List<Recipe> recipes = new ArrayList<>();
            for (int i = 0; i < 14; i++) {
                recipes.add(Recipe.builder()
                        .id(UUID.randomUUID())
                        .nombre("Receta " + i)
                        .tiempoMinutos(30)
                        .raciones(4)
                        .ingredientes(List.of())
                        .favorita(i < 4)
                        .familyGroupId(familyGroupId)
                        .build());
            }

            when(familyMemberRepository.findByUserIdAndFamilyGroupId(adminUserId, familyGroupId))
                    .thenReturn(Optional.of(adminMember));
            when(recipeRepository.findByFamilyGroupId(familyGroupId)).thenReturn(recipes);
            when(familyMemberRepository.findByFamilyGroupId(familyGroupId)).thenReturn(List.of());
            when(userRepository.findById(adminUserId)).thenReturn(Optional.of(User.builder()
                    .id(adminUserId)
                    .preferencias(Preferences.builder().nivelCocina("MEDIO").build())
                    .build()));
            when(weeklyPlanRepository.findHistoryByFamilyGroup(familyGroupId)).thenReturn(List.of());
            when(weeklyPlanRepository.save(any(WeeklyPlan.class))).thenAnswer(inv -> inv.getArgument(0));

            GenerateMenuRequest request = new GenerateMenuRequest(4, List.of(), true, false, 2026, 25);

            WeeklyPlanResponse response = weeklyPlanService.generarMenu(familyGroupId, request, adminUserId);

            assertThat(response).isNotNull();
            assertThat(response.dias()).hasSize(14);
            assertThat(response.familyGroupId()).isEqualTo(familyGroupId);
        }

        @Test
        @DisplayName("debe lanzar excepción si no hay recetas disponibles")
        void debeLanzarExcepcionSinRecetas() {
            FamilyMember adminMember = FamilyMember.builder()
                    .userId(adminUserId).familyGroupId(familyGroupId)
                    .role(FamilyRole.ADMIN).build();

            when(familyMemberRepository.findByUserIdAndFamilyGroupId(adminUserId, familyGroupId))
                    .thenReturn(Optional.of(adminMember));
            when(recipeRepository.findByFamilyGroupId(familyGroupId)).thenReturn(List.of());

            GenerateMenuRequest request = new GenerateMenuRequest(4, List.of(), false, false, 2026, 25);

            assertThatThrownBy(() -> weeklyPlanService.generarMenu(familyGroupId, request, adminUserId))
                    .isInstanceOf(WeeklyPlanNotFoundException.class)
                    .hasMessageContaining("No hay recetas disponibles");
        }

        @Test
        @DisplayName("debe excluir recetas indicadas en el request")
        void debeExcluirRecetasIndicadas() {
            FamilyMember adminMember = FamilyMember.builder()
                    .userId(adminUserId).familyGroupId(familyGroupId)
                    .role(FamilyRole.ADMIN).build();

            UUID excludedId = UUID.randomUUID();
            List<Recipe> recipes = new ArrayList<>();
            recipes.add(Recipe.builder().id(excludedId).nombre("Excluida")
                    .tiempoMinutos(30).raciones(2).ingredientes(List.of())
                    .familyGroupId(familyGroupId).build());
            for (int i = 0; i < 14; i++) {
                recipes.add(Recipe.builder().id(UUID.randomUUID()).nombre("Receta " + i)
                        .tiempoMinutos(30).raciones(2).ingredientes(List.of())
                        .familyGroupId(familyGroupId).build());
            }

            when(familyMemberRepository.findByUserIdAndFamilyGroupId(adminUserId, familyGroupId))
                    .thenReturn(Optional.of(adminMember));
            when(recipeRepository.findByFamilyGroupId(familyGroupId)).thenReturn(recipes);
            when(familyMemberRepository.findByFamilyGroupId(familyGroupId)).thenReturn(List.of());
            when(userRepository.findById(adminUserId)).thenReturn(Optional.of(User.builder()
                    .id(adminUserId)
                    .preferencias(Preferences.builder().nivelCocina("MEDIO").build())
                    .build()));
            when(weeklyPlanRepository.findHistoryByFamilyGroup(familyGroupId)).thenReturn(List.of());
            when(weeklyPlanRepository.save(any(WeeklyPlan.class))).thenAnswer(inv -> inv.getArgument(0));

            GenerateMenuRequest request = new GenerateMenuRequest(2, List.of(excludedId), false, false, 2026, 25);

            WeeklyPlanResponse response = weeklyPlanService.generarMenu(familyGroupId, request, adminUserId);

            assertThat(response).isNotNull();
            assertThat(response.dias()).hasSize(14);
            // Verificar que ninguna receta excluida aparece
            boolean hasExcluded = response.dias().stream()
                    .filter(d -> d.receta() != null)
                    .anyMatch(d -> d.receta().id().equals(excludedId));
            assertThat(hasExcluded).isFalse();
        }

        @Test
        @DisplayName("debe lanzar excepción si no es ADMIN")
        void debeLanzarExcepcionSiNoEsAdmin() {
            FamilyMember consumerMember = FamilyMember.builder()
                    .userId(userId).familyGroupId(familyGroupId)
                    .role(FamilyRole.CONSUMER).build();

            when(familyMemberRepository.findByUserIdAndFamilyGroupId(userId, familyGroupId))
                    .thenReturn(Optional.of(consumerMember));

            GenerateMenuRequest request = new GenerateMenuRequest(4, List.of(), false, false, 2026, 25);

            assertThatThrownBy(() -> weeklyPlanService.generarMenu(familyGroupId, request, userId))
                    .isInstanceOf(UnauthorizedException.class);
        }
    }
}
