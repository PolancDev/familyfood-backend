package com.familyfood.infrastructure.adapter.web;

import com.familyfood.application.dto.plan.*;
import com.familyfood.application.service.WeeklyPlanService;
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
@RequestMapping("/api/v1/plan-semanal")
@RequiredArgsConstructor
@Slf4j
public class WeeklyPlanController {

    private final WeeklyPlanService weeklyPlanService;

    @GetMapping
    public ResponseEntity<WeeklyPlanResponse> obtenerPlan(
            @RequestParam UUID familyGroupId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer weekNumber,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = extractUserId(userDetails);
        log.info("Obteniendo plan semanal: familia={}, año={}, semana={}", familyGroupId, year, weekNumber);
        return ResponseEntity.ok(weeklyPlanService.obtenerPlan(familyGroupId, year, weekNumber, userId));
    }

    @PostMapping
    public ResponseEntity<WeeklyPlanResponse> guardarPlan(
            @RequestParam UUID familyGroupId,
            @Valid @RequestBody SaveWeeklyPlanRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = extractUserId(userDetails);
        log.info("Guardando plan semanal: familia={}, año={}, semana={}",
                familyGroupId, request.year(), request.weekNumber());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(weeklyPlanService.guardarPlan(familyGroupId, request, userId));
    }

    @PutMapping("/dia")
    public ResponseEntity<PlanDayResponse> actualizarDia(
            @RequestParam UUID familyGroupId,
            @RequestParam int year,
            @RequestParam int weekNumber,
            @Valid @RequestBody UpdateDayRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = extractUserId(userDetails);
        log.info("Actualizando día: familia={}, año={}, semana={}, dia={}, tipo={}",
                familyGroupId, year, weekNumber, request.dia(), request.tipo());
        return ResponseEntity.ok(weeklyPlanService.actualizarDia(
                familyGroupId, year, weekNumber, request, userId));
    }

    @PostMapping("/generar")
    public ResponseEntity<WeeklyPlanResponse> generarMenu(
            @RequestParam UUID familyGroupId,
            @Valid @RequestBody GenerateMenuRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = extractUserId(userDetails);
        log.info("Generando menú: familia={}, personas={}, año={}, semana={}",
                familyGroupId, request.numeroPersonas(), request.year(), request.weekNumber());
        return ResponseEntity.ok(weeklyPlanService.generarMenu(familyGroupId, request, userId));
    }

    private UUID extractUserId(final UserDetails userDetails) {
        CustomUserDetails customUser = (CustomUserDetails) userDetails;
        return customUser.getUserId();
    }
}
