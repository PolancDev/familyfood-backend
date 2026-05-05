package com.familyfood.infrastructure.adapter.web;

import com.familyfood.application.dto.recipe.CreateRecipeRequest;
import com.familyfood.application.dto.recipe.RecipeListResponse;
import com.familyfood.application.dto.recipe.RecipeResponse;
import com.familyfood.application.dto.recipe.UpdateRecipeRequest;
import com.familyfood.application.service.RecipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/recetas")
@RequiredArgsConstructor
@Slf4j
public class RecipeController {

    private final RecipeService recipeService;

    @GetMapping
    public ResponseEntity<RecipeListResponse> listarRecetas(
            @RequestParam(required = false) Boolean favoritas,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String etiqueta,
            Authentication authentication) {
        UUID userId = getUserIdFromAuthentication(authentication);
        log.info("Listando recetas para usuario: {}, favoritas: {}, busqueda: {}, etiqueta: {}",
                userId, favoritas, busqueda, etiqueta);
        return ResponseEntity.ok(recipeService.listarRecetas(userId, favoritas, busqueda, etiqueta));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponse> obtenerReceta(@PathVariable UUID id) {
        log.info("Obteniendo receta con id: {}", id);
        return ResponseEntity.ok(recipeService.obtenerReceta(id));
    }

    @PostMapping
    public ResponseEntity<RecipeResponse> crearReceta(
            @Valid @RequestBody CreateRecipeRequest request,
            Authentication authentication) {
        UUID userId = getUserIdFromAuthentication(authentication);
        log.info("Creando receta para usuario: {}", userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(recipeService.crearReceta(request, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeResponse> actualizarReceta(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRecipeRequest request,
            Authentication authentication) {
        UUID userId = getUserIdFromAuthentication(authentication);
        log.info("Actualizando receta con id: {}", id);
        return ResponseEntity.ok(recipeService.actualizarReceta(id, request, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarReceta(@PathVariable UUID id) {
        log.info("Eliminando receta con id: {}", id);
        recipeService.eliminarReceta(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/favorita")
    public ResponseEntity<RecipeResponse> toggleFavorita(@PathVariable UUID id) {
        log.info("Toggle favorita para receta con id: {}", id);
        return ResponseEntity.ok(recipeService.toggleFavorita(id));
    }

    private UUID getUserIdFromAuthentication(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof com.familyfood.infrastructure.adapter.security.CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        throw new IllegalStateException("Usuario no autenticado");
    }
}
