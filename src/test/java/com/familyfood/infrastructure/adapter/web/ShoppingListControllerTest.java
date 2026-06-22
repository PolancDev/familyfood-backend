package com.familyfood.infrastructure.adapter.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.familyfood.application.dto.shopping.*;
import com.familyfood.application.service.ShoppingListService;
import com.familyfood.domain.exception.ShoppingListNotFoundException;
import com.familyfood.domain.exception.UnauthorizedException;
import com.familyfood.domain.model.Role;
import com.familyfood.infrastructure.adapter.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShoppingListController Tests")
class ShoppingListControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ShoppingListService shoppingListService;

    @InjectMocks
    private ShoppingListController shoppingListController;

    private UUID familyGroupId;
    private UUID itemId;
    private UUID userId;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(shoppingListController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        familyGroupId = UUID.randomUUID();
        itemId = UUID.randomUUID();
        userId = UUID.randomUUID();

        userDetails = CustomUserDetails.builder()
                .id(userId)
                .email("test@familyfood.com")
                .password("password")
                .nombre("Test User")
                .enabled(true)
                .role(Role.ADMIN)
                .build();

        // Set up security context
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
    }

    @Nested
    @DisplayName("GET /api/v1/lista-compra")
    class GetShoppingListTests {

        @Test
        @DisplayName("debe devolver 200 con la lista")
        void debeDevolverOk() throws Exception {
            ShoppingItemResponse mockItem = new ShoppingItemResponse(
                    itemId, "Leche", 1.0, "litro", "LACTEOS", false, false, List.of());
            ShoppingListResponse mockResponse = new ShoppingListResponse(
                    UUID.randomUUID(), familyGroupId, 2026, 25, "2026-W25",
                    List.of("LACTEOS"), List.of(mockItem));

            when(shoppingListService.getShoppingList(eq(familyGroupId), eq(2026), eq(25), any(UUID.class)))
                    .thenReturn(mockResponse);

            mockMvc.perform(get("/api/v1/lista-compra")
                            .param("familyGroupId", familyGroupId.toString())
                            .param("year", "2026")
                            .param("weekNumber", "25"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.familyGroupId").value(familyGroupId.toString()))
                    .andExpect(jsonPath("$.semana").value("2026-W25"))
                    .andExpect(jsonPath("$.year").value(2026))
                    .andExpect(jsonPath("$.weekNumber").value(25));
        }

        @Test
        @DisplayName("debe devolver 404 si no existe")
        void debeDevolver404() throws Exception {
            when(shoppingListService.getShoppingList(eq(familyGroupId), eq(2026), eq(25), any(UUID.class)))
                    .thenThrow(new ShoppingListNotFoundException("No se encontró la lista de compra para la semana solicitada"));

            mockMvc.perform(get("/api/v1/lista-compra")
                            .param("familyGroupId", familyGroupId.toString())
                            .param("year", "2026")
                            .param("weekNumber", "25"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("No se encontró la lista de compra para la semana solicitada"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/lista-compra/generar")
    class GenerateShoppingListTests {

        @Test
        @DisplayName("debe devolver 201 al generar correctamente")
        void debeDevolverCreated() throws Exception {
            ShoppingItemResponse mockItem = new ShoppingItemResponse(
                    itemId, "Leche", 1.0, "litro", "LACTEOS", false, false, List.of());
            ShoppingListResponse mockResponse = new ShoppingListResponse(
                    UUID.randomUUID(), familyGroupId, 2026, 25, "2026-W25",
                    List.of("LACTEOS"), List.of(mockItem));

            when(shoppingListService.generateShoppingList(eq(familyGroupId), any(GenerateShoppingListRequest.class), any(UUID.class)))
                    .thenReturn(mockResponse);

            String requestJson = """
                    {
                        "year": 2026,
                        "weekNumber": 25,
                        "raciones": 4,
                        "incluirBasicos": true
                    }""";

            mockMvc.perform(post("/api/v1/lista-compra/generar")
                            .param("familyGroupId", familyGroupId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.semana").value("2026-W25"));
        }

        @Test
        @DisplayName("debe devolver 400 si faltan campos requeridos")
        void debeDevolver400SinCamposRequeridos() throws Exception {
            String invalidJson = """
                    {
                        "incluirBasicos": false
                    }""";

            mockMvc.perform(post("/api/v1/lista-compra/generar")
                            .param("familyGroupId", familyGroupId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/lista-compra/items")
    class AddItemTests {

        @Test
        @DisplayName("debe devolver 201 al añadir item")
        void debeDevolverCreated() throws Exception {
            ShoppingItemResponse mockItem = new ShoppingItemResponse(
                    itemId, "Galletas", 2.0, "paquete", "OTROS", false, true, List.of());

            when(shoppingListService.addItem(eq(familyGroupId), eq(2026), eq(25), any(AddItemRequest.class), any(UUID.class)))
                    .thenReturn(mockItem);

            String requestJson = """
                    {
                        "ingrediente": "Galletas",
                        "cantidad": 2.0,
                        "unidad": "paquete",
                        "categoria": "OTROS"
                    }""";

            mockMvc.perform(post("/api/v1/lista-compra/items")
                            .param("familyGroupId", familyGroupId.toString())
                            .param("year", "2026")
                            .param("weekNumber", "25")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.ingrediente").value("Galletas"))
                    .andExpect(jsonPath("$.esManual").value(true));
        }

        @Test
        @DisplayName("debe devolver 400 si faltan campos")
        void debeDevolver400() throws Exception {
            mockMvc.perform(post("/api/v1/lista-compra/items")
                            .param("familyGroupId", familyGroupId.toString())
                            .param("year", "2026")
                            .param("weekNumber", "25")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/lista-compra/items/{itemId}")
    class UpdateItemTests {

        @Test
        @DisplayName("debe devolver 200 al actualizar")
        void debeDevolverOk() throws Exception {
            ShoppingItemResponse mockItem = new ShoppingItemResponse(
                    itemId, "Leche", 1.0, "litro", "LACTEOS", true, false, List.of());

            when(shoppingListService.updateItem(eq(itemId), any(UpdateItemRequest.class), any(UUID.class)))
                    .thenReturn(mockItem);

            String requestJson = """
                    {
                        "comprado": true
                    }""";

            mockMvc.perform(put("/api/v1/lista-compra/items/{itemId}", itemId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.comprado").value(true));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/lista-compra/items/{itemId}")
    class DeleteItemTests {

        @Test
        @DisplayName("debe devolver 204 al eliminar")
        void debeDevolverNoContent() throws Exception {
            doNothing().when(shoppingListService).deleteItem(eq(itemId), any(UUID.class));

            mockMvc.perform(delete("/api/v1/lista-compra/items/{itemId}", itemId))
                    .andExpect(status().isNoContent());

            verify(shoppingListService).deleteItem(eq(itemId), any(UUID.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/lista-compra/exportar")
    class ExportShoppingListTests {

        @Test
        @DisplayName("debe devolver texto plano en 200")
        void debeDevolverTextoPlano() throws Exception {
            when(shoppingListService.exportShoppingList(eq(familyGroupId), eq(2026), eq(25), any(UUID.class)))
                    .thenReturn("LISTA DE COMPRA - Test");

            mockMvc.perform(get("/api/v1/lista-compra/exportar")
                            .param("familyGroupId", familyGroupId.toString())
                            .param("year", "2026")
                            .param("weekNumber", "25"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("LISTA DE COMPRA - Test"));
        }

        @Test
        @DisplayName("debe devolver 404 si la lista no existe")
        void debeDevolver404AlExportar() throws Exception {
            when(shoppingListService.exportShoppingList(eq(familyGroupId), eq(2026), eq(25), any(UUID.class)))
                    .thenThrow(new ShoppingListNotFoundException("No se encontró la lista de compra para la semana solicitada"));

            mockMvc.perform(get("/api/v1/lista-compra/exportar")
                            .param("familyGroupId", familyGroupId.toString())
                            .param("year", "2026")
                            .param("weekNumber", "25"))
                    .andExpect(status().isNotFound());
        }
    }
}
