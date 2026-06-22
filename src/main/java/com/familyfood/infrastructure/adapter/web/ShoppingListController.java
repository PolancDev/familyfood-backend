package com.familyfood.infrastructure.adapter.web;

import com.familyfood.application.dto.shopping.*;
import com.familyfood.application.service.ShoppingListService;
import com.familyfood.infrastructure.adapter.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lista-compra")
@RequiredArgsConstructor
@Slf4j
public class ShoppingListController {

    private final ShoppingListService shoppingListService;

    @GetMapping
    public ResponseEntity<ShoppingListResponse> getShoppingList(
            @RequestParam UUID familyGroupId,
            @RequestParam int year,
            @RequestParam int weekNumber,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = extractUserId(userDetails);
        log.info("Obteniendo lista de compra: familia={}, año={}, semana={}", familyGroupId, year, weekNumber);
        return ResponseEntity.ok(shoppingListService.getShoppingList(familyGroupId, year, weekNumber, userId));
    }

    @PostMapping("/generar")
    public ResponseEntity<ShoppingListResponse> generateShoppingList(
            @RequestParam UUID familyGroupId,
            @Valid @RequestBody GenerateShoppingListRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = extractUserId(userDetails);
        log.info("Generando lista de compra: familia={}, año={}, semana={}, raciones={}, basicos={}",
                familyGroupId, request.year(), request.weekNumber(), request.raciones(), request.incluirBasicos());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(shoppingListService.generateShoppingList(familyGroupId, request, userId));
    }

    @PostMapping("/items")
    public ResponseEntity<ShoppingItemResponse> addItem(
            @RequestParam UUID familyGroupId,
            @RequestParam int year,
            @RequestParam int weekNumber,
            @Valid @RequestBody AddItemRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = extractUserId(userDetails);
        log.info("Añadiendo item manual a lista de compra: {}, familia={}, año={}, semana={}",
                request.ingrediente(), familyGroupId, year, weekNumber);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(shoppingListService.addItem(familyGroupId, year, weekNumber, request, userId));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<ShoppingItemResponse> updateItem(
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateItemRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = extractUserId(userDetails);
        log.info("Actualizando item de compra: id={}", itemId);
        return ResponseEntity.ok(shoppingListService.updateItem(itemId, request, userId));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable UUID itemId,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = extractUserId(userDetails);
        log.info("Eliminando item de compra: id={}", itemId);
        shoppingListService.deleteItem(itemId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exportar")
    public ResponseEntity<String> exportShoppingList(
            @RequestParam UUID familyGroupId,
            @RequestParam int year,
            @RequestParam int weekNumber,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = extractUserId(userDetails);
        log.info("Exportando lista de compra: familia={}, año={}, semana={}", familyGroupId, year, weekNumber);
        String textExport = shoppingListService.exportShoppingList(familyGroupId, year, weekNumber, userId);
        return ResponseEntity.ok()
                .header("Content-Type", "text/plain; charset=UTF-8")
                .body(textExport);
    }

    private UUID extractUserId(final UserDetails userDetails) {
        CustomUserDetails customUser = (CustomUserDetails) userDetails;
        return customUser.getUserId();
    }
}
