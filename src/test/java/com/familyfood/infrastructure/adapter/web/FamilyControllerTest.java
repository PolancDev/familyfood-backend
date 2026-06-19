package com.familyfood.infrastructure.adapter.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.familyfood.application.dto.family.CreateFamilyRequest;
import com.familyfood.application.dto.family.FamilyMemberResponse;
import com.familyfood.application.dto.family.FamilyResponse;
import com.familyfood.application.dto.family.FamilySearchResponse;
import com.familyfood.application.dto.family.JoinRequestResponse;
import com.familyfood.application.service.FamilyService;
import com.familyfood.domain.enums.FamilyRole;
import com.familyfood.domain.enums.JoinRequestStatus;
import com.familyfood.domain.exception.FamilyGroupNotFoundException;
import com.familyfood.domain.exception.JoinRequestNotFoundException;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de FamilyController")
class FamilyControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private FamilyService familyService;

    @InjectMocks
    private FamilyController familyController;

    private UUID testUserId;
    private UUID testFamilyId;
    private UUID testMemberId;
    private UUID testRequestId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        // Registrar módulo JavaTime para serializar LocalDateTime
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(familyController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        testUserId = UUID.randomUUID();
        testFamilyId = UUID.randomUUID();
        testMemberId = UUID.randomUUID();
        testRequestId = UUID.randomUUID();
    }

    /**
     * Crea un CustomUserDetails para simular un usuario autenticado con rol ADMIN.
     */
    private CustomUserDetails createAdminUser() {
        return new CustomUserDetails(
                testUserId,
                "admin@example.com",
                "encodedPassword",
                "Admin User",
                true,
                Role.ADMIN
        );
    }

    /**
     * Crea un CustomUserDetails para simular un usuario autenticado con rol CONSUMER.
     */
    private CustomUserDetails createConsumerUser() {
        return new CustomUserDetails(
                testUserId,
                "consumer@example.com",
                "encodedPassword",
                "Consumer User",
                true,
                Role.CONSUMER
        );
    }

    /**
     * RequestPostProcessor que inyecta un CustomUserDetails autenticado en el SecurityContextHolder,
     * permitiendo que {@code @AuthenticationPrincipal} funcione en standalone MockMvc.
     */
    private static RequestPostProcessor authenticateAs(CustomUserDetails user) {
        return request -> {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())
            );
            return request;
        };
    }

    /**
     * Limpia el SecurityContextHolder al finalizar la request (evita leaks entre tests).
     */
    private static MockHttpServletRequestBuilder withCleanup(MockHttpServletRequestBuilder builder) {
        return builder;
    }

    // ========================================================================
    // POST /api/v1/familias - Crear familia
    // ========================================================================

    @Nested
    @DisplayName("POST /api/v1/familias - Crear familia")
    class CreateFamilyTests {

        @Test
        @DisplayName("Debería crear una familia exitosamente y devolver 201")
        void shouldCreateFamilySuccessfully() throws Exception {
            // Given
            CustomUserDetails user = createAdminUser();
            CreateFamilyRequest request = new CreateFamilyRequest("Familia García");

            FamilyResponse response = new FamilyResponse(
                    testFamilyId, request.name(), testUserId,
                    LocalDateTime.now(), 1L
            );

            when(familyService.createFamily(eq(testUserId), eq("Familia García")))
                    .thenReturn(response);

            // When & Then
            mockMvc.perform(post("/api/v1/familias")
                            .with(authenticateAs(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(result -> SecurityContextHolder.clearContext())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(testFamilyId.toString()))
                    .andExpect(jsonPath("$.name").value("Familia García"))
                    .andExpect(jsonPath("$.memberCount").value(1));

            verify(familyService).createFamily(eq(testUserId), eq("Familia García"));
        }

        @Test
        @DisplayName("Debería devolver 400 cuando el nombre está vacío")
        void shouldReturn400WhenNameIsEmpty() throws Exception {
            // Given
            CustomUserDetails user = createAdminUser();
            CreateFamilyRequest request = new CreateFamilyRequest("");

            // When & Then
            mockMvc.perform(post("/api/v1/familias")
                            .with(authenticateAs(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(result -> SecurityContextHolder.clearContext())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"));
        }

        @Test
        @DisplayName("Debería devolver 400 cuando el nombre tiene menos de 2 caracteres")
        void shouldReturn400WhenNameIsTooShort() throws Exception {
            // Given
            CustomUserDetails user = createAdminUser();
            CreateFamilyRequest request = new CreateFamilyRequest("A");

            // When & Then
            mockMvc.perform(post("/api/v1/familias")
                            .with(authenticateAs(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(result -> SecurityContextHolder.clearContext())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"));
        }
    }

    // ========================================================================
    // POST /api/v1/familias/{id}/unirse - Unirse a familia
    // ========================================================================

    @Nested
    @DisplayName("POST /api/v1/familias/{id}/unirse - Unirse a familia")
    class JoinFamilyTests {

        @Test
        @DisplayName("Debería unirse a la familia exitosamente y devolver 200")
        void shouldJoinFamilySuccessfully() throws Exception {
            // Given
            CustomUserDetails user = createConsumerUser();

            // When & Then
            mockMvc.perform(post("/api/v1/familias/{id}/unirse", testFamilyId)
                            .with(authenticateAs(user)))
                    .andDo(result -> SecurityContextHolder.clearContext())
                    .andExpect(status().isOk());

            verify(familyService).joinFamily(eq(testUserId), eq(testFamilyId));
        }

        @Test
        @DisplayName("Debería devolver 404 cuando la familia no existe")
        void shouldReturn404WhenFamilyNotFound() throws Exception {
            // Given
            CustomUserDetails user = createConsumerUser();
            doThrow(new FamilyGroupNotFoundException("Grupo familiar no encontrado"))
                    .when(familyService).joinFamily(eq(testUserId), eq(testFamilyId));

            // When & Then
            mockMvc.perform(post("/api/v1/familias/{id}/unirse", testFamilyId)
                            .with(authenticateAs(user)))
                    .andDo(result -> SecurityContextHolder.clearContext())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Grupo familiar no encontrado"));
        }

        @Test
        @DisplayName("Debería devolver 403 cuando el usuario ya es miembro")
        void shouldReturn403WhenAlreadyMember() throws Exception {
            // Given
            CustomUserDetails user = createConsumerUser();
            doThrow(new UnauthorizedException("Ya eres miembro de este grupo familiar"))
                    .when(familyService).joinFamily(eq(testUserId), eq(testFamilyId));

            // When & Then
            mockMvc.perform(post("/api/v1/familias/{id}/unirse", testFamilyId)
                            .with(authenticateAs(user)))
                    .andDo(result -> SecurityContextHolder.clearContext())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Ya eres miembro de este grupo familiar"));
        }
    }

    // ========================================================================
    // GET /api/v1/familias/mis-familias - Familias del usuario
    // ========================================================================

    @Nested
    @DisplayName("GET /api/v1/familias/mis-familias - Familias del usuario")
    class GetUserFamiliesTests {

        @Test
        @DisplayName("Debería devolver la lista de familias del usuario")
        void shouldReturnUserFamilies() throws Exception {
            // Given
            CustomUserDetails user = createAdminUser();
            FamilyResponse family1 = new FamilyResponse(
                    testFamilyId, "Familia García", testUserId,
                    LocalDateTime.now(), 3L
            );
            FamilyResponse family2 = new FamilyResponse(
                    UUID.randomUUID(), "Familia López", testUserId,
                    LocalDateTime.now(), 2L
            );

            when(familyService.getUserFamilies(eq(testUserId)))
                    .thenReturn(List.of(family1, family2));

            // When & Then
            mockMvc.perform(get("/api/v1/familias/mis-familias")
                            .with(authenticateAs(user)))
                    .andDo(result -> SecurityContextHolder.clearContext())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].name").value("Familia García"))
                    .andExpect(jsonPath("$[1].name").value("Familia López"));

            verify(familyService).getUserFamilies(eq(testUserId));
        }

        @Test
        @DisplayName("Debería devolver lista vacía cuando el usuario no pertenece a ninguna familia")
        void shouldReturnEmptyListWhenNoFamilies() throws Exception {
            // Given
            CustomUserDetails user = createAdminUser();
            when(familyService.getUserFamilies(eq(testUserId)))
                    .thenReturn(List.of());

            // When & Then
            mockMvc.perform(get("/api/v1/familias/mis-familias")
                            .with(authenticateAs(user)))
                    .andDo(result -> SecurityContextHolder.clearContext())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    // ========================================================================
    // GET /api/v1/familias/buscar?q= - Buscar familias
    // ========================================================================

    @Nested
    @DisplayName("GET /api/v1/familias/buscar - Buscar familias")
    class SearchFamiliesTests {

        @Test
        @DisplayName("Debería buscar familias por nombre y devolver resultados")
        void shouldSearchFamiliesByName() throws Exception {
            // Given
            CustomUserDetails user = createAdminUser();
            FamilySearchResponse result1 = new FamilySearchResponse(
                    testFamilyId, "Familia García", 4L
            );
            FamilySearchResponse result2 = new FamilySearchResponse(
                    UUID.randomUUID(), "Familia García López", 2L
            );

            when(familyService.searchFamilies(eq("García"), eq(testUserId)))
                    .thenReturn(List.of(result1, result2));

            // When & Then
            mockMvc.perform(get("/api/v1/familias/buscar")
                            .param("q", "García")
                            .with(authenticateAs(user)))
                    .andDo(result -> SecurityContextHolder.clearContext())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].name").value("Familia García"))
                    .andExpect(jsonPath("$[1].name").value("Familia García López"));

            verify(familyService).searchFamilies(eq("García"), eq(testUserId));
        }

        @Test
        @DisplayName("Debería devolver lista vacía cuando no hay resultados")
        void shouldReturnEmptyListWhenNoResults() throws Exception {
            // Given
            CustomUserDetails user = createAdminUser();
            when(familyService.searchFamilies(eq("NoExiste"), eq(testUserId)))
                    .thenReturn(List.of());

            // When & Then
            mockMvc.perform(get("/api/v1/familias/buscar")
                            .param("q", "NoExiste")
                            .with(authenticateAs(user)))
                    .andDo(result -> SecurityContextHolder.clearContext())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    // ========================================================================
    // GET /api/v1/familias/{id}/miembros - Miembros de familia
    // ========================================================================

    @Nested
    @DisplayName("GET /api/v1/familias/{id}/miembros - Miembros de familia")
    class GetMembersTests {

        @Test
        @DisplayName("Debería devolver la lista de miembros de la familia")
        void shouldReturnFamilyMembers() throws Exception {
            // Given
            CustomUserDetails user = createAdminUser();
            FamilyMemberResponse member1 = new FamilyMemberResponse(
                    testMemberId, testUserId, "Admin User",
                    "admin@example.com", FamilyRole.ADMIN, LocalDateTime.now()
            );
            FamilyMemberResponse member2 = new FamilyMemberResponse(
                    UUID.randomUUID(), UUID.randomUUID(), "Consumer User",
                    "consumer@example.com", FamilyRole.CONSUMER, LocalDateTime.now()
            );

            when(familyService.getMembers(eq(testFamilyId)))
                    .thenReturn(List.of(member1, member2));

            // When & Then
            mockMvc.perform(get("/api/v1/familias/{id}/miembros", testFamilyId)
                            .with(authenticateAs(user)))
                    .andDo(result -> SecurityContextHolder.clearContext())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].userName").value("Admin User"))
                    .andExpect(jsonPath("$[0].role").value("ADMIN"))
                    .andExpect(jsonPath("$[1].userName").value("Consumer User"))
                    .andExpect(jsonPath("$[1].role").value("CONSUMER"));

            verify(familyService).getMembers(eq(testFamilyId));
        }

        @Test
        @DisplayName("Debería devolver 404 cuando la familia no existe")
        void shouldReturn404WhenFamilyNotFound() throws Exception {
            // Given
            CustomUserDetails user = createAdminUser();
            when(familyService.getMembers(eq(testFamilyId)))
                    .thenThrow(new FamilyGroupNotFoundException("Grupo familiar no encontrado"));

            // When & Then
            mockMvc.perform(get("/api/v1/familias/{id}/miembros", testFamilyId)
                            .with(authenticateAs(user)))
                    .andDo(result -> SecurityContextHolder.clearContext())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Grupo familiar no encontrado"));
        }
    }

    // ========================================================================
    // GET /api/v1/familias/mis-solicitudes - Solicitudes pendientes del usuario
    // ========================================================================

    @Nested
    @DisplayName("GET /api/v1/familias/mis-solicitudes - Solicitudes pendientes")
    class GetMyPendingRequestsTests {

        @Test
        @DisplayName("Debería devolver las solicitudes pendientes del usuario")
        void shouldReturnMyPendingRequests() throws Exception {
            // Given
            CustomUserDetails user = createConsumerUser();
            JoinRequestResponse request1 = new JoinRequestResponse(
                    testRequestId, testUserId, "Consumer User",
                    "consumer@example.com", testFamilyId,
                    "Familia García", JoinRequestStatus.PENDING, LocalDateTime.now()
            );

            when(familyService.getMyPendingRequests(eq(testUserId)))
                    .thenReturn(List.of(request1));

            // When & Then
            mockMvc.perform(get("/api/v1/familias/mis-solicitudes")
                            .with(authenticateAs(user)))
                    .andDo(result -> SecurityContextHolder.clearContext())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].status").value("PENDING"))
                    .andExpect(jsonPath("$[0].familyGroupName").value("Familia García"));

            verify(familyService).getMyPendingRequests(eq(testUserId));
        }

        @Test
        @DisplayName("Debería devolver lista vacía cuando no hay solicitudes pendientes")
        void shouldReturnEmptyListWhenNoPendingRequests() throws Exception {
            // Given
            CustomUserDetails user = createConsumerUser();
            when(familyService.getMyPendingRequests(eq(testUserId)))
                    .thenReturn(List.of());

            // When & Then
            mockMvc.perform(get("/api/v1/familias/mis-solicitudes")
                            .with(authenticateAs(user)))
                    .andDo(result -> SecurityContextHolder.clearContext())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    // ========================================================================
    // GET /api/v1/familias/{id}/solicitudes - Solicitudes pendientes (ADMIN)
    // ========================================================================

    @Nested
    @DisplayName("GET /api/v1/familias/{id}/solicitudes - Solicitudes pendientes (ADMIN)")
    class GetPendingRequestsTests {

        @Test
        @DisplayName("Debería devolver las solicitudes pendientes del grupo")
        void shouldReturnPendingRequests() throws Exception {
            // Given
            CustomUserDetails admin = createAdminUser();
            JoinRequestResponse request1 = new JoinRequestResponse(
                    testRequestId, UUID.randomUUID(), "New User",
                    "newuser@example.com", testFamilyId,
                    "Familia García", JoinRequestStatus.PENDING, LocalDateTime.now()
            );

            when(familyService.getPendingRequests(eq(testFamilyId), eq(testUserId)))
                    .thenReturn(List.of(request1));

            // When & Then
            mockMvc.perform(get("/api/v1/familias/{id}/solicitudes", testFamilyId)
                            .with(authenticateAs(admin)))
                    .andDo(result -> SecurityContextHolder.clearContext())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].userName").value("New User"))
                    .andExpect(jsonPath("$[0].status").value("PENDING"));

            verify(familyService).getPendingRequests(eq(testFamilyId), eq(testUserId));
        }

        @Test
        @DisplayName("Debería devolver lista vacía cuando no hay solicitudes pendientes")
        void shouldReturnEmptyListWhenNoPendingRequests() throws Exception {
            // Given
            CustomUserDetails admin = createAdminUser();
            when(familyService.getPendingRequests(eq(testFamilyId), eq(testUserId)))
                    .thenReturn(List.of());

            // When & Then
            mockMvc.perform(get("/api/v1/familias/{id}/solicitudes", testFamilyId)
                            .with(authenticateAs(admin)))
                    .andDo(result -> SecurityContextHolder.clearContext())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    // ========================================================================
    // PUT /api/v1/familias/solicitudes/{requestId}/aprobar - Aprobar solicitud
    // ========================================================================

    @Nested
    @DisplayName("PUT /api/v1/familias/solicitudes/{requestId}/aprobar - Aprobar solicitud")
    class ApproveJoinRequestTests {

        @Test
        @DisplayName("Debería aprobar la solicitud y devolver 200")
        void shouldApproveJoinRequest() throws Exception {
            // Given
            CustomUserDetails admin = createAdminUser();

            // When & Then
            mockMvc.perform(put("/api/v1/familias/solicitudes/{requestId}/aprobar", testRequestId)
                            .with(authenticateAs(admin)))
                    .andDo(result -> SecurityContextHolder.clearContext())
                    .andExpect(status().isOk());

            verify(familyService).approveJoinRequest(eq(testRequestId), eq(testUserId));
        }

        @Test
        @DisplayName("Debería devolver 404 cuando la solicitud no existe")
        void shouldReturn404WhenRequestNotFound() throws Exception {
            // Given
            CustomUserDetails admin = createAdminUser();
            doThrow(new JoinRequestNotFoundException("Solicitud no encontrada"))
                    .when(familyService).approveJoinRequest(eq(testRequestId), eq(testUserId));

            // When & Then
            mockMvc.perform(put("/api/v1/familias/solicitudes/{requestId}/aprobar", testRequestId)
                            .with(authenticateAs(admin)))
                    .andDo(result -> SecurityContextHolder.clearContext())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Solicitud no encontrada"));
        }
    }

    // ========================================================================
    // PUT /api/v1/familias/solicitudes/{requestId}/rechazar - Rechazar solicitud
    // ========================================================================

    @Nested
    @DisplayName("PUT /api/v1/familias/solicitudes/{requestId}/rechazar - Rechazar solicitud")
    class RejectJoinRequestTests {

        @Test
        @DisplayName("Debería rechazar la solicitud y devolver 200")
        void shouldRejectJoinRequest() throws Exception {
            // Given
            CustomUserDetails admin = createAdminUser();

            // When & Then
            mockMvc.perform(put("/api/v1/familias/solicitudes/{requestId}/rechazar", testRequestId)
                            .with(authenticateAs(admin)))
                    .andDo(result -> SecurityContextHolder.clearContext())
                    .andExpect(status().isOk());

            verify(familyService).rejectJoinRequest(eq(testRequestId), eq(testUserId));
        }

        @Test
        @DisplayName("Debería devolver 404 cuando la solicitud no existe")
        void shouldReturn404WhenRequestNotFound() throws Exception {
            // Given
            CustomUserDetails admin = createAdminUser();
            doThrow(new JoinRequestNotFoundException("Solicitud no encontrada"))
                    .when(familyService).rejectJoinRequest(eq(testRequestId), eq(testUserId));

            // When & Then
            mockMvc.perform(put("/api/v1/familias/solicitudes/{requestId}/rechazar", testRequestId)
                            .with(authenticateAs(admin)))
                    .andDo(result -> SecurityContextHolder.clearContext())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Solicitud no encontrada"));
        }
    }

    // ========================================================================
    // PUT /api/v1/familias/{id}/transferir-admin/{memberId} - Transferir admin
    // ========================================================================

    @Nested
    @DisplayName("PUT /api/v1/familias/{id}/transferir-admin/{memberId} - Transferir admin")
    class TransferAdminTests {

        @Test
        @DisplayName("Debería transferir el rol de admin y devolver 200")
        void shouldTransferAdminSuccessfully() throws Exception {
            // Given
            CustomUserDetails admin = createAdminUser();

            // When & Then
            mockMvc.perform(put("/api/v1/familias/{id}/transferir-admin/{memberId}",
                            testFamilyId, testMemberId)
                            .with(authenticateAs(admin)))
                    .andDo(result -> SecurityContextHolder.clearContext())
                    .andExpect(status().isOk());

            verify(familyService).transferAdmin(eq(testFamilyId), eq(testUserId), eq(testMemberId));
        }

        @Test
        @DisplayName("Debería devolver 404 cuando la familia no existe")
        void shouldReturn404WhenFamilyNotFound() throws Exception {
            // Given
            CustomUserDetails admin = createAdminUser();
            doThrow(new FamilyGroupNotFoundException(testFamilyId))
                    .when(familyService).transferAdmin(eq(testFamilyId), eq(testUserId), eq(testMemberId));

            // When & Then
            mockMvc.perform(put("/api/v1/familias/{id}/transferir-admin/{memberId}",
                            testFamilyId, testMemberId)
                            .with(authenticateAs(admin)))
                    .andDo(result -> SecurityContextHolder.clearContext())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Grupo familiar no encontrado: " + testFamilyId));
        }
    }

    // ========================================================================
    // DELETE /api/v1/familias/{id} - Soft delete familia
    // ========================================================================

    @Nested
    @DisplayName("DELETE /api/v1/familias/{id} - Eliminar familia")
    class DeleteFamilyTests {

        @Test
        @DisplayName("Debería eliminar la familia y devolver 204")
        void shouldDeleteFamilySuccessfully() throws Exception {
            // Given
            CustomUserDetails admin = createAdminUser();

            // When & Then
            mockMvc.perform(delete("/api/v1/familias/{id}", testFamilyId)
                            .with(authenticateAs(admin)))
                    .andDo(result -> SecurityContextHolder.clearContext())
                    .andExpect(status().isNoContent());

            verify(familyService).deleteFamily(eq(testFamilyId), eq(testUserId));
        }

        @Test
        @DisplayName("Debería devolver 404 cuando la familia no existe")
        void shouldReturn404WhenFamilyNotFound() throws Exception {
            // Given
            CustomUserDetails admin = createAdminUser();
            doThrow(new FamilyGroupNotFoundException("Grupo familiar no encontrado"))
                    .when(familyService).deleteFamily(eq(testFamilyId), eq(testUserId));

            // When & Then
            mockMvc.perform(delete("/api/v1/familias/{id}", testFamilyId)
                            .with(authenticateAs(admin)))
                    .andDo(result -> SecurityContextHolder.clearContext())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Grupo familiar no encontrado"));
        }
    }
}
