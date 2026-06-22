package com.familyfood.application.service;

import com.familyfood.application.dto.shopping.*;
import com.familyfood.application.mapper.ShoppingListMapper;
import com.familyfood.application.port.repository.FamilyMemberRepository;
import com.familyfood.application.port.repository.RecipeRepository;
import com.familyfood.application.port.repository.ShoppingListRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShoppingListService Tests")
class ShoppingListServiceTest {

    @Mock
    private ShoppingListRepository shoppingListRepository;
    @Mock
    private WeeklyPlanRepository weeklyPlanRepository;
    @Mock
    private RecipeRepository recipeRepository;
    @Mock
    private FamilyMemberRepository familyMemberRepository;
    @Mock
    private ShoppingListMapper shoppingListMapper;

    private ShoppingListService shoppingListService;

    private UUID userId;
    private UUID adminUserId;
    private UUID familyGroupId;
    private UUID recipeId1;
    private UUID recipeId2;
    private UUID planId;

    private WeeklyPlan testWeeklyPlan;
    private Recipe testRecipe1;
    private Recipe testRecipe2;
    private ShoppingList testShoppingList;

    @BeforeEach
    void setUp() {
        shoppingListService = new ShoppingListService(
                shoppingListRepository, weeklyPlanRepository,
                recipeRepository, familyMemberRepository, shoppingListMapper);

        userId = UUID.randomUUID();
        adminUserId = UUID.randomUUID();
        familyGroupId = UUID.randomUUID();
        recipeId1 = UUID.randomUUID();
        recipeId2 = UUID.randomUUID();
        planId = UUID.randomUUID();

        // Recetas de prueba
        testRecipe1 = Recipe.builder()
                .id(recipeId1)
                .nombre("Paella")
                .raciones(4)
                .tiempoMinutos(45)
                .ingredientes(List.of(
                        RecipeIngredient.builder().nombre("Arroz").cantidad(300.0).unidad("g").build(),
                        RecipeIngredient.builder().nombre("Pollo").cantidad(500.0).unidad("g").build(),
                        RecipeIngredient.builder().nombre("Pimiento").cantidad(2.0).unidad("unidad").build()
                ))
                .familyGroupId(familyGroupId)
                .build();

        testRecipe2 = Recipe.builder()
                .id(recipeId2)
                .nombre("Ensalada")
                .raciones(2)
                .tiempoMinutos(15)
                .ingredientes(List.of(
                        RecipeIngredient.builder().nombre("Lechuga").cantidad(1.0).unidad("unidad").build(),
                        RecipeIngredient.builder().nombre("Tomate").cantidad(2.0).unidad("unidad").build(),
                        RecipeIngredient.builder().nombre("Arroz").cantidad(100.0).unidad("g").build()
                ))
                .familyGroupId(familyGroupId)
                .build();

        // Plan semanal de prueba con 2 días NORMAL
        PlanDay day1 = PlanDay.builder()
                .id(UUID.randomUUID())
                .weeklyPlanId(planId)
                .dia(DiaSemana.LUNES)
                .tipo(TipoComida.COMIDA)
                .recetaId(recipeId1)
                .estado(EstadoDia.NORMAL)
                .build();

        PlanDay day2 = PlanDay.builder()
                .id(UUID.randomUUID())
                .weeklyPlanId(planId)
                .dia(DiaSemana.MARTES)
                .tipo(TipoComida.CENA)
                .recetaId(recipeId2)
                .estado(EstadoDia.NORMAL)
                .build();

        // Día IMPROVISADO (debe ignorarse)
        PlanDay day3 = PlanDay.builder()
                .id(UUID.randomUUID())
                .weeklyPlanId(planId)
                .dia(DiaSemana.MIERCOLES)
                .tipo(TipoComida.COMIDA)
                .estado(EstadoDia.IMPROVISADO)
                .build();

        testWeeklyPlan = WeeklyPlan.builder()
                .id(planId)
                .familyGroupId(familyGroupId)
                .year(2026)
                .weekNumber(25)
                .startDate(LocalDate.of(2026, 6, 15))
                .endDate(LocalDate.of(2026, 6, 21))
                .dias(List.of(day1, day2, day3))
                .build();

        testShoppingList = ShoppingList.builder()
                .id(UUID.randomUUID())
                .familyGroupId(familyGroupId)
                .year(2026)
                .weekNumber(25)
                .items(new ArrayList<>())
                .build();
    }

    // ========== GENERAR LISTA DE COMPRA ==========

    @Nested
    @DisplayName("generateShoppingList")
    class GenerateShoppingListTests {

        @Test
        @DisplayName("debe generar lista de compra desde el plan semanal correctamente")
        void debeGenerarListaDesdePlan() {
            FamilyMember adminMember = FamilyMember.builder()
                    .userId(adminUserId).familyGroupId(familyGroupId)
                    .role(FamilyRole.ADMIN).build();

            when(familyMemberRepository.findByUserIdAndFamilyGroupId(adminUserId, familyGroupId))
                    .thenReturn(Optional.of(adminMember));
            when(weeklyPlanRepository.findByFamilyGroupAndWeek(familyGroupId, 2026, 25))
                    .thenReturn(Optional.of(testWeeklyPlan));
            when(recipeRepository.findById(recipeId1)).thenReturn(Optional.of(testRecipe1));
            when(recipeRepository.findById(recipeId2)).thenReturn(Optional.of(testRecipe2));
            when(shoppingListRepository.findByFamilyGroupAndWeek(familyGroupId, 2026, 25))
                    .thenReturn(Optional.empty());
            when(shoppingListRepository.save(any(ShoppingList.class))).thenAnswer(inv -> inv.getArgument(0));

            GenerateShoppingListRequest request = new GenerateShoppingListRequest(2026, 25, 2, false);

            ShoppingListResponse response = shoppingListService.generateShoppingList(familyGroupId, request, adminUserId);

            assertThat(response).isNotNull();
            assertThat(response.items()).isNotEmpty();
            assertThat(response.familyGroupId()).isEqualTo(familyGroupId);
            assertThat(response.year()).isEqualTo(2026);
            assertThat(response.weekNumber()).isEqualTo(25);

            // Verificar agrupación: Arroz aparece en ambas recetas, debe estar consolidado
            Optional<ShoppingItemResponse> arrozItem = response.items().stream()
                    .filter(i -> "Arroz".equals(i.ingrediente()))
                    .findFirst();
            assertThat(arrozItem).isPresent();
            // Paella: 300g * (2/4) = 150g; Ensalada: 100g * (2/2) = 100g; Total = 250g
            assertThat(arrozItem.get().cantidad()).isEqualTo(250.0);
        }

        @Test
        @DisplayName("debe aplicar correctamente el factor de raciones")
        void debeAplicarFactorRaciones() {
            FamilyMember adminMember = FamilyMember.builder()
                    .userId(adminUserId).familyGroupId(familyGroupId)
                    .role(FamilyRole.ADMIN).build();

            // Plan con solo 1 receta
            PlanDay singleDay = PlanDay.builder()
                    .id(UUID.randomUUID())
                    .weeklyPlanId(planId)
                    .dia(DiaSemana.LUNES)
                    .tipo(TipoComida.COMIDA)
                    .recetaId(recipeId1)
                    .estado(EstadoDia.NORMAL)
                    .build();

            WeeklyPlan plan = WeeklyPlan.builder()
                    .id(planId).familyGroupId(familyGroupId)
                    .year(2026).weekNumber(25)
                    .startDate(LocalDate.of(2026, 6, 15))
                    .endDate(LocalDate.of(2026, 6, 21))
                    .dias(List.of(singleDay))
                    .build();

            when(familyMemberRepository.findByUserIdAndFamilyGroupId(adminUserId, familyGroupId))
                    .thenReturn(Optional.of(adminMember));
            when(weeklyPlanRepository.findByFamilyGroupAndWeek(familyGroupId, 2026, 25))
                    .thenReturn(Optional.of(plan));
            when(recipeRepository.findById(recipeId1)).thenReturn(Optional.of(testRecipe1));
            when(shoppingListRepository.findByFamilyGroupAndWeek(familyGroupId, 2026, 25))
                    .thenReturn(Optional.empty());
            when(shoppingListRepository.save(any(ShoppingList.class))).thenAnswer(inv -> inv.getArgument(0));

            // raciones=8 (doble de la receta que es 4)
            GenerateShoppingListRequest request = new GenerateShoppingListRequest(2026, 25, 8, false);

            ShoppingListResponse response = shoppingListService.generateShoppingList(familyGroupId, request, adminUserId);

            // Pollo: 500g * (8/4) = 1000g
            Optional<ShoppingItemResponse> polloItem = response.items().stream()
                    .filter(i -> "Pollo".equals(i.ingrediente()))
                    .findFirst();
            assertThat(polloItem).isPresent();
            assertThat(polloItem.get().cantidad()).isEqualTo(1000.0);
        }

        @Test
        @DisplayName("debe incluir items básicos cuando incluirBasicos=true")
        void debeIncluirBasicos() {
            FamilyMember adminMember = FamilyMember.builder()
                    .userId(adminUserId).familyGroupId(familyGroupId)
                    .role(FamilyRole.ADMIN).build();

            PlanDay singleDay = PlanDay.builder()
                    .id(UUID.randomUUID())
                    .weeklyPlanId(planId)
                    .dia(DiaSemana.LUNES)
                    .tipo(TipoComida.COMIDA)
                    .recetaId(recipeId1)
                    .estado(EstadoDia.NORMAL)
                    .build();

            WeeklyPlan plan = WeeklyPlan.builder()
                    .id(planId).familyGroupId(familyGroupId)
                    .year(2026).weekNumber(25)
                    .startDate(LocalDate.of(2026, 6, 15))
                    .endDate(LocalDate.of(2026, 6, 21))
                    .dias(List.of(singleDay))
                    .build();

            when(familyMemberRepository.findByUserIdAndFamilyGroupId(adminUserId, familyGroupId))
                    .thenReturn(Optional.of(adminMember));
            when(weeklyPlanRepository.findByFamilyGroupAndWeek(familyGroupId, 2026, 25))
                    .thenReturn(Optional.of(plan));
            when(recipeRepository.findById(recipeId1)).thenReturn(Optional.of(testRecipe1));
            when(shoppingListRepository.findByFamilyGroupAndWeek(familyGroupId, 2026, 25))
                    .thenReturn(Optional.empty());
            when(shoppingListRepository.save(any(ShoppingList.class))).thenAnswer(inv -> inv.getArgument(0));

            GenerateShoppingListRequest request = new GenerateShoppingListRequest(2026, 25, 4, true);

            ShoppingListResponse response = shoppingListService.generateShoppingList(familyGroupId, request, adminUserId);

            // Debe incluir "Pan", "Huevos", etc.
            List<String> allIngredients = response.items().stream()
                    .map(ShoppingItemResponse::ingrediente)
                    .toList();
            assertThat(allIngredients).contains("Pan", "Huevos", "Sal", "Aceite de oliva");
        }

        @Test
        @DisplayName("debe lanzar excepción si no existe el plan semanal")
        void debeLanzarExcepcionSinPlan() {
            FamilyMember adminMember = FamilyMember.builder()
                    .userId(adminUserId).familyGroupId(familyGroupId)
                    .role(FamilyRole.ADMIN).build();

            when(familyMemberRepository.findByUserIdAndFamilyGroupId(adminUserId, familyGroupId))
                    .thenReturn(Optional.of(adminMember));
            when(weeklyPlanRepository.findByFamilyGroupAndWeek(familyGroupId, 2026, 25))
                    .thenReturn(Optional.empty());

            GenerateShoppingListRequest request = new GenerateShoppingListRequest(2026, 25, 4, false);

            assertThatThrownBy(() -> shoppingListService.generateShoppingList(familyGroupId, request, adminUserId))
                    .isInstanceOf(WeeklyPlanNotFoundException.class)
                    .hasMessageContaining("plan semanal");
        }

        @Test
        @DisplayName("debe lanzar excepción si no es ADMIN")
        void debeLanzarExcepcionSiNoEsAdmin() {
            FamilyMember consumerMember = FamilyMember.builder()
                    .userId(userId).familyGroupId(familyGroupId)
                    .role(FamilyRole.CONSUMER).build();

            when(familyMemberRepository.findByUserIdAndFamilyGroupId(userId, familyGroupId))
                    .thenReturn(Optional.of(consumerMember));

            GenerateShoppingListRequest request = new GenerateShoppingListRequest(2026, 25, 4, false);

            assertThatThrownBy(() -> shoppingListService.generateShoppingList(familyGroupId, request, userId))
                    .isInstanceOf(UnauthorizedException.class);
        }

        @Test
        @DisplayName("debe reemplazar lista existente al regenerar")
        void debeReemplazarListaExistente() {
            FamilyMember adminMember = FamilyMember.builder()
                    .userId(adminUserId).familyGroupId(familyGroupId)
                    .role(FamilyRole.ADMIN).build();

            when(familyMemberRepository.findByUserIdAndFamilyGroupId(adminUserId, familyGroupId))
                    .thenReturn(Optional.of(adminMember));
            when(weeklyPlanRepository.findByFamilyGroupAndWeek(familyGroupId, 2026, 25))
                    .thenReturn(Optional.of(testWeeklyPlan));
            when(recipeRepository.findById(recipeId1)).thenReturn(Optional.of(testRecipe1));
            when(recipeRepository.findById(recipeId2)).thenReturn(Optional.of(testRecipe2));
            when(shoppingListRepository.findByFamilyGroupAndWeek(familyGroupId, 2026, 25))
                    .thenReturn(Optional.of(testShoppingList));
            when(shoppingListRepository.save(any(ShoppingList.class))).thenAnswer(inv -> inv.getArgument(0));

            GenerateShoppingListRequest request = new GenerateShoppingListRequest(2026, 25, 2, false);

            ShoppingListResponse response = shoppingListService.generateShoppingList(familyGroupId, request, adminUserId);

            assertThat(response).isNotNull();
            // Debió eliminar la lista existente antes de crear la nueva
            verify(shoppingListRepository).delete(any(ShoppingList.class));
            verify(shoppingListRepository).save(any(ShoppingList.class));
        }
    }

    // ========== OBTENER LISTA DE COMPRA ==========

    @Nested
    @DisplayName("getShoppingList")
    class GetShoppingListTests {

        @Test
        @DisplayName("debe devolver la lista de compra si existe")
        void debeDevolverListaExistente() {
            when(familyMemberRepository.existsByUserIdAndFamilyGroupId(userId, familyGroupId)).thenReturn(true);
            when(shoppingListRepository.findByFamilyGroupAndWeek(familyGroupId, 2026, 25))
                    .thenReturn(Optional.of(testShoppingList));
            when(weeklyPlanRepository.findByFamilyGroupAndWeek(familyGroupId, 2026, 25))
                    .thenReturn(Optional.of(testWeeklyPlan));
            when(recipeRepository.findById(recipeId1)).thenReturn(Optional.of(testRecipe1));
            when(recipeRepository.findById(recipeId2)).thenReturn(Optional.of(testRecipe2));

            ShoppingListResponse response = shoppingListService.getShoppingList(familyGroupId, 2026, 25, userId);

            assertThat(response).isNotNull();
            assertThat(response.familyGroupId()).isEqualTo(familyGroupId);
            assertThat(response.semana()).isEqualTo("2026-W25");
        }

        @Test
        @DisplayName("debe lanzar 404 si la lista no existe")
        void debeLanzar404SiNoExiste() {
            when(familyMemberRepository.existsByUserIdAndFamilyGroupId(userId, familyGroupId)).thenReturn(true);
            when(shoppingListRepository.findByFamilyGroupAndWeek(familyGroupId, 2026, 25))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> shoppingListService.getShoppingList(familyGroupId, 2026, 25, userId))
                    .isInstanceOf(ShoppingListNotFoundException.class)
                    .hasMessageContaining("No se encontró la lista de compra");
        }

        @Test
        @DisplayName("debe lanzar excepción si el usuario no pertenece a la familia")
        void debeLanzarExcepcionSiNoEsMiembro() {
            when(familyMemberRepository.existsByUserIdAndFamilyGroupId(userId, familyGroupId)).thenReturn(false);

            assertThatThrownBy(() -> shoppingListService.getShoppingList(familyGroupId, 2026, 25, userId))
                    .isInstanceOf(UnauthorizedException.class);
        }
    }

    // ========== CATEGORIZACIÓN ==========

    @Nested
    @DisplayName("categorizar")
    class CategorizarTests {

        @Test
        @DisplayName("debe categorizar 'Leche' como LACTEOS")
        void debeCategorizarLacteos() {
            assertThat(shoppingListService.categorizar("Leche")).isEqualTo(CategoriaCompra.LACTEOS);
        }

        @Test
        @DisplayName("debe categorizar 'Queso parmesano' como LACTEOS")
        void debeCategorizarQuesoComoLacteos() {
            assertThat(shoppingListService.categorizar("Queso parmesano")).isEqualTo(CategoriaCompra.LACTEOS);
        }

        @Test
        @DisplayName("debe categorizar 'Pan' como PANADERIA")
        void debeCategorizarPanComoPanaderia() {
            assertThat(shoppingListService.categorizar("Pan")).isEqualTo(CategoriaCompra.PANADERIA);
        }

        @Test
        @DisplayName("debe categorizar 'Garbanzos' como LEGUMBRES")
        void debeCategorizarGarbanzosComoLegumbres() {
            assertThat(shoppingListService.categorizar("Garbanzos")).isEqualTo(CategoriaCompra.LEGUMBRES);
        }

        @Test
        @DisplayName("debe categorizar 'Atún en lata' como CONSERVAS")
        void debeCategorizarAtunEnLataComoConservas() {
            assertThat(shoppingListService.categorizar("Atún en lata")).isEqualTo(CategoriaCompra.CONSERVAS);
        }

        @Test
        @DisplayName("debe categorizar 'Aceitunas' como CONSERVAS")
        void debeCategorizarAceitunasComoConservas() {
            assertThat(shoppingListService.categorizar("Aceitunas")).isEqualTo(CategoriaCompra.CONSERVAS);
        }

        @Test
        @DisplayName("debe categorizar 'Pollo' como CARNE")
        void debeCategorizarPolloComoCarne() {
            assertThat(shoppingListService.categorizar("Pollo")).isEqualTo(CategoriaCompra.CARNE);
        }

        @Test
        @DisplayName("debe categorizar 'Arroz' como ARROCES")
        void debeCategorizarArrozComoArroces() {
            assertThat(shoppingListService.categorizar("Arroz")).isEqualTo(CategoriaCompra.ARROCES);
        }

        @Test
        @DisplayName("debe categorizar 'Espaguetis' como PASTA")
        void debeCategorizarEspaguetisComoPasta() {
            assertThat(shoppingListService.categorizar("Espaguetis")).isEqualTo(CategoriaCompra.PASTA);
        }

        @Test
        @DisplayName("debe categorizar 'Merluza' como PESCADERIA")
        void debeCategorizarMerluzaComoPescaderia() {
            assertThat(shoppingListService.categorizar("Merluza")).isEqualTo(CategoriaCompra.PESCADERIA);
        }

        @Test
        @DisplayName("debe categorizar 'Manzana' como FRUTERIA")
        void debeCategorizarManzanaComoFruteria() {
            assertThat(shoppingListService.categorizar("Manzana")).isEqualTo(CategoriaCompra.FRUTERIA);
        }

        @Test
        @DisplayName("debe categorizar 'Guacamole' como OTROS (desconocido)")
        void debeCategorizarDesconocidoComoOtros() {
            assertThat(shoppingListService.categorizar("Guacamole")).isEqualTo(CategoriaCompra.OTROS);
        }

        @Test
        @DisplayName("debe categorizar 'Verduras congeladas' como CONGELADOS")
        void debeCategorizarCongelados() {
            assertThat(shoppingListService.categorizar("Verduras congeladas")).isEqualTo(CategoriaCompra.CONGELADOS);
        }
    }

    // ========== AÑADIR ITEM MANUAL ==========

    @Nested
    @DisplayName("addItem")
    class AddItemTests {

        @Test
        @DisplayName("debe añadir item manual a la lista")
        void debeAnadirItemManual() {
            when(familyMemberRepository.existsByUserIdAndFamilyGroupId(userId, familyGroupId)).thenReturn(true);
            when(shoppingListRepository.findByFamilyGroupAndWeek(familyGroupId, 2026, 25))
                    .thenReturn(Optional.of(testShoppingList));
            when(shoppingListRepository.save(any(ShoppingList.class))).thenAnswer(inv -> inv.getArgument(0));
            when(shoppingListMapper.toItemResponse(any(ShoppingItem.class))).thenAnswer(inv -> {
                ShoppingItem item = inv.getArgument(0);
                return new ShoppingItemResponse(item.getId(), item.getIngrediente(), item.getCantidad(),
                        item.getUnidad(), item.getCategoria(), item.isComprado(), item.isEsManual(), List.of());
            });

            AddItemRequest request = new AddItemRequest("Galletas", 2.0, "paquete", CategoriaCompra.OTROS);

            ShoppingItemResponse response = shoppingListService.addItem(familyGroupId, 2026, 25, request, userId);

            assertThat(response).isNotNull();
            assertThat(response.ingrediente()).isEqualTo("Galletas");
            assertThat(response.cantidad()).isEqualTo(2.0);
            assertThat(response.esManual()).isTrue();
        }
    }

    // ========== MARCAR COMO COMPRADO ==========

    @Nested
    @DisplayName("updateItem - marcar comprado")
    class MarcarCompradoTests {

        @Test
        @DisplayName("debe marcar item como comprado")
        void debeMarcarComprado() {
            UUID itemId = UUID.randomUUID();
            ShoppingItem item = ShoppingItem.builder()
                    .id(itemId)
                    .shoppingListId(testShoppingList.getId())
                    .ingrediente("Leche")
                    .cantidad(1.0)
                    .unidad("litro")
                    .categoria(CategoriaCompra.LACTEOS)
                    .comprado(false)
                    .esManual(false)
                    .build();

            when(shoppingListRepository.findItemById(itemId)).thenReturn(Optional.of(item));
            when(shoppingListRepository.findListByItemId(itemId)).thenReturn(Optional.of(testShoppingList));
            when(familyMemberRepository.existsByUserIdAndFamilyGroupId(userId, familyGroupId)).thenReturn(true);
            when(shoppingListRepository.updateItem(any(ShoppingItem.class))).thenAnswer(inv -> inv.getArgument(0));
            when(shoppingListMapper.toItemResponse(any(ShoppingItem.class))).thenAnswer(inv -> {
                ShoppingItem it = inv.getArgument(0);
                return new ShoppingItemResponse(it.getId(), it.getIngrediente(), it.getCantidad(),
                        it.getUnidad(), it.getCategoria(), it.isComprado(), it.isEsManual(), List.of());
            });

            UpdateItemRequest request = new UpdateItemRequest(true, null, null);

            ShoppingItemResponse response = shoppingListService.updateItem(itemId, request, userId);

            assertThat(response).isNotNull();
            assertThat(response.comprado()).isTrue();
        }

        @Test
        @DisplayName("debe desmarcar item como comprado")
        void debeDesmarcarComprado() {
            UUID itemId = UUID.randomUUID();
            ShoppingItem item = ShoppingItem.builder()
                    .id(itemId)
                    .shoppingListId(testShoppingList.getId())
                    .ingrediente("Leche")
                    .cantidad(1.0)
                    .unidad("litro")
                    .categoria(CategoriaCompra.LACTEOS)
                    .comprado(true)
                    .esManual(false)
                    .build();

            when(shoppingListRepository.findItemById(itemId)).thenReturn(Optional.of(item));
            when(shoppingListRepository.findListByItemId(itemId)).thenReturn(Optional.of(testShoppingList));
            when(familyMemberRepository.existsByUserIdAndFamilyGroupId(userId, familyGroupId)).thenReturn(true);
            when(shoppingListRepository.updateItem(any(ShoppingItem.class))).thenAnswer(inv -> inv.getArgument(0));
            when(shoppingListMapper.toItemResponse(any(ShoppingItem.class))).thenAnswer(inv -> {
                ShoppingItem it = inv.getArgument(0);
                return new ShoppingItemResponse(it.getId(), it.getIngrediente(), it.getCantidad(),
                        it.getUnidad(), it.getCategoria(), it.isComprado(), it.isEsManual(), List.of());
            });

            UpdateItemRequest request = new UpdateItemRequest(false, null, null);

            ShoppingItemResponse response = shoppingListService.updateItem(itemId, request, userId);

            assertThat(response).isNotNull();
            assertThat(response.comprado()).isFalse();
        }
    }

    // ========== EXPORTAR ==========

    @Nested
    @DisplayName("exportShoppingList")
    class ExportShoppingListTests {

        @Test
        @DisplayName("debe exportar lista en texto plano")
        void debeExportarEnTextoPlano() {
            ShoppingItem item1 = ShoppingItem.builder()
                    .id(UUID.randomUUID())
                    .shoppingListId(testShoppingList.getId())
                    .ingrediente("Leche")
                    .cantidad(2.0)
                    .unidad("litros")
                    .categoria(CategoriaCompra.LACTEOS)
                    .comprado(false)
                    .esManual(false)
                    .build();

            ShoppingItem item2 = ShoppingItem.builder()
                    .id(UUID.randomUUID())
                    .shoppingListId(testShoppingList.getId())
                    .ingrediente("Pan")
                    .cantidad(1.0)
                    .unidad("barra")
                    .categoria(CategoriaCompra.PANADERIA)
                    .comprado(true)
                    .esManual(false)
                    .build();

            testShoppingList.getItems().add(item1);
            testShoppingList.getItems().add(item2);

            when(familyMemberRepository.existsByUserIdAndFamilyGroupId(userId, familyGroupId)).thenReturn(true);
            when(shoppingListRepository.findByFamilyGroupAndWeek(familyGroupId, 2026, 25))
                    .thenReturn(Optional.of(testShoppingList));

            String export = shoppingListService.exportShoppingList(familyGroupId, 2026, 25, userId);

            assertThat(export).isNotNull();
            assertThat(export).contains("LISTA DE COMPRA");
            assertThat(export).contains("LACTEOS");
            assertThat(export).contains("Leche");
            assertThat(export).contains("[ ]");
            assertThat(export).contains("[x]");
            assertThat(export).contains("Pan");
        }

        @Test
        @DisplayName("debe lanzar 404 si la lista no existe")
        void debeLanzar404AlExportarSinLista() {
            when(familyMemberRepository.existsByUserIdAndFamilyGroupId(userId, familyGroupId)).thenReturn(true);
            when(shoppingListRepository.findByFamilyGroupAndWeek(familyGroupId, 2026, 25))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> shoppingListService.exportShoppingList(familyGroupId, 2026, 25, userId))
                    .isInstanceOf(ShoppingListNotFoundException.class);
        }
    }
}
