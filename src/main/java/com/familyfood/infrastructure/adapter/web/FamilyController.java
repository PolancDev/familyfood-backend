package com.familyfood.infrastructure.adapter.web;

import com.familyfood.application.dto.family.CreateFamilyRequest;
import com.familyfood.application.dto.family.FamilyMemberResponse;
import com.familyfood.application.dto.family.FamilyResponse;
import com.familyfood.application.dto.family.FamilySearchResponse;
import com.familyfood.application.dto.family.JoinRequestResponse;
import com.familyfood.application.service.FamilyService;
import com.familyfood.infrastructure.adapter.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para la gestión de grupos familiares.
 */
@RestController
@RequestMapping("/api/v1/familias")
@RequiredArgsConstructor
@Slf4j
public class FamilyController {

    private final FamilyService familyService;

    /**
     * Crea un nuevo grupo familiar.
     *
     * @param userDetails datos del usuario autenticado
     * @param request     datos de la solicitud
     * @return el grupo familiar creado
     */
    @PostMapping
    public ResponseEntity<FamilyResponse> createFamily(
            final @AuthenticationPrincipal UserDetails userDetails,
            final @Valid @RequestBody CreateFamilyRequest request) {
        UUID userId = extractUserId(userDetails);
        log.info("Solicitud de creación de grupo familiar: usuario={}, nombre={}", userId, request.name());
        FamilyResponse response = familyService.createFamily(userId, request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Solicita unión a un grupo familiar.
     *
     * @param userDetails datos del usuario autenticado
     * @param id          ID del grupo familiar
     * @return respuesta vacía
     */
    @PostMapping("/{id}/unirse")
    public ResponseEntity<Void> joinFamily(
            final @AuthenticationPrincipal UserDetails userDetails,
            final @PathVariable UUID id) {
        log.info("*********************************************************");
        log.info("Solicitud de unión a grupo: usuario={}, grupo={}", userDetails.getUsername(), id);
        UUID userId = extractUserId(userDetails);
        log.info("Solicitud de unión a grupo: usuario={}, grupo={}", userId, id);
        familyService.joinFamily(userId, id);
        return ResponseEntity.ok().build();
    }

    /**
     * Obtiene las familias del usuario autenticado.
     *
     * @param userDetails datos del usuario autenticado
     * @return lista de grupos familiares
     */
    @GetMapping("/mis-familias")
    public ResponseEntity<List<FamilyResponse>> getUserFamilies(
            final @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = extractUserId(userDetails);
        log.info("Obteniendo familias del usuario: {}", userId);
        List<FamilyResponse> families = familyService.getUserFamilies(userId);
        return ResponseEntity.ok(families);
    }

    /**
     * Busca grupos familiares por nombre (autocomplete).
     *
     * @param query       término de búsqueda
     * @param userDetails datos del usuario autenticado
     * @return lista de grupos que coinciden con la búsqueda
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<FamilySearchResponse>> searchFamilies(
            final @RequestParam("q") String query,
            final @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = extractUserId(userDetails);
        log.info("Búsqueda de grupos familiares: query='{}', usuario={}", query, userId);
        List<FamilySearchResponse> results = familyService.searchFamilies(query, userId);
        return ResponseEntity.ok(results);
    }

    /**
     * Obtiene los miembros de un grupo familiar.
     *
     * @param userDetails datos del usuario autenticado
     * @param id          ID del grupo familiar
     * @return lista de miembros del grupo
     */
    @GetMapping("/{id}/miembros")
    public ResponseEntity<List<FamilyMemberResponse>> getMembers(
            final @AuthenticationPrincipal UserDetails userDetails,
            final @PathVariable UUID id) {
        UUID userId = extractUserId(userDetails);
        log.info("Obteniendo miembros del grupo: {}, usuario={}", id, userId);
        List<FamilyMemberResponse> members = familyService.getMembers(id);
        return ResponseEntity.ok(members);
    }

    /**
     * Obtiene las solicitudes pendientes del usuario logueado.
     *
     * @param userDetails datos del usuario autenticado
     * @return lista de solicitudes pendientes del usuario
     */
    @GetMapping("/mis-solicitudes")
    public ResponseEntity<List<JoinRequestResponse>> getMyPendingRequests(
            final @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = extractUserId(userDetails);
        log.info("Obteniendo solicitudes pendientes del usuario: {}", userId);
        List<JoinRequestResponse> requests = familyService.getMyPendingRequests(userId);
        return ResponseEntity.ok(requests);
    }

    /**
     * Obtiene las solicitudes pendientes de un grupo (solo ADMIN).
     *
     * @param userDetails datos del usuario autenticado
     * @param id          ID del grupo familiar
     * @return lista de solicitudes pendientes
     */
    @GetMapping("/{id}/solicitudes")
    public ResponseEntity<List<JoinRequestResponse>> getPendingRequests(
            final @AuthenticationPrincipal UserDetails userDetails,
            final @PathVariable UUID id) {
        UUID adminUserId = extractUserId(userDetails);
        log.info("Obteniendo solicitudes pendientes del grupo: {}, admin={}", id, adminUserId);
        List<JoinRequestResponse> requests = familyService.getPendingRequests(id, adminUserId);
        return ResponseEntity.ok(requests);
    }

    /**
     * Aprueba una solicitud de unión (solo ADMIN).
     *
     * @param userDetails datos del usuario autenticado
     * @param requestId   ID de la solicitud
     * @return respuesta vacía
     */
    @PutMapping("/solicitudes/{requestId}/aprobar")
    public ResponseEntity<Void> approveJoinRequest(
            final @AuthenticationPrincipal UserDetails userDetails,
            final @PathVariable UUID requestId) {
        UUID adminUserId = extractUserId(userDetails);
        log.info("Aprobando solicitud: {}, admin={}", requestId, adminUserId);
        familyService.approveJoinRequest(requestId, adminUserId);
        return ResponseEntity.ok().build();
    }

    /**
     * Rechaza una solicitud de unión (solo ADMIN).
     *
     * @param userDetails datos del usuario autenticado
     * @param requestId   ID de la solicitud
     * @return respuesta vacía
     */
    @PutMapping("/solicitudes/{requestId}/rechazar")
    public ResponseEntity<Void> rejectJoinRequest(
            final @AuthenticationPrincipal UserDetails userDetails,
            final @PathVariable UUID requestId) {
        UUID adminUserId = extractUserId(userDetails);
        log.info("Rechazando solicitud: {}, admin={}", requestId, adminUserId);
        familyService.rejectJoinRequest(requestId, adminUserId);
        return ResponseEntity.ok().build();
    }

    /**
     * Transfiere el rol de administrador a otro miembro del grupo.
     * Solo el ADMIN actual puede transferir. El target debe ser miembro CONSUMER.
     *
     * @param userDetails datos del usuario autenticado
     * @param id          ID del grupo familiar
     * @param memberId    ID del miembro que recibirá el rol de ADMIN
     * @return respuesta vacía con código 200
     */
    @PutMapping("/{id}/transferir-admin/{memberId}")
    public ResponseEntity<Void> transferAdmin(
            final @AuthenticationPrincipal UserDetails userDetails,
            final @PathVariable UUID id,
            final @PathVariable UUID memberId) {
        UUID adminUserId = extractUserId(userDetails);
        log.info("Solicitud de transferencia de admin: grupo={}, nuevoAdmin={}, adminActual={}",
                id, memberId, adminUserId);
        familyService.transferAdmin(id, adminUserId, memberId);
        return ResponseEntity.ok().build();
    }

    /**
     * Elimina (soft delete) un grupo familiar. Solo el ADMIN puede hacerlo.
     *
     * @param userDetails datos del usuario autenticado
     * @param id          ID del grupo familiar
     * @return respuesta vacía con código 204
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFamily(
            final @AuthenticationPrincipal UserDetails userDetails,
            final @PathVariable UUID id) {
        UUID adminUserId = extractUserId(userDetails);
        log.info("Solicitud de eliminación de grupo familiar: grupo={}, admin={}", id, adminUserId);
        familyService.deleteFamily(id, adminUserId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Extrae el ID del usuario del CustomUserDetails.
     *
     * @param userDetails datos del usuario autenticado
     * @return UUID del usuario
     */
    private UUID extractUserId(final UserDetails userDetails) {
        CustomUserDetails customUser = (CustomUserDetails) userDetails;
        log.info("Usuario autenticado: id={}, email={}", customUser.getUserId(), customUser.getUsername());
        return customUser.getUserId();
    }
}
