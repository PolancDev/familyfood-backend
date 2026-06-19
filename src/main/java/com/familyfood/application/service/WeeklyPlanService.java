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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class WeeklyPlanService {

    private final WeeklyPlanRepository weeklyPlanRepository;
    private final RecipeRepository recipeRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final UserRepository userRepository;
    private final WeeklyPlanMapper weeklyPlanMapper;

    /**
     * Obtiene el plan semanal para una semana específica.
     * Si no existe, auto-crea uno vacío con 14 slots IMPROVISADO.
     */
    @Transactional
    public WeeklyPlanResponse obtenerPlan(UUID familyGroupId, Integer year, Integer weekNumber, UUID userId) {
        validateUserInFamily(userId, familyGroupId);

        // Si no se especifica, usar semana ISO actual
        LocalDate today = LocalDate.now();
        WeekFields weekFields = WeekFields.ISO;
        int effectiveYear = year != null ? year : today.get(weekFields.weekBasedYear());
        int effectiveWeek = weekNumber != null ? weekNumber : today.get(weekFields.weekOfWeekBasedYear());

        Optional<WeeklyPlan> existingPlan = weeklyPlanRepository
                .findByFamilyGroupAndWeek(familyGroupId, effectiveYear, effectiveWeek);

        if (existingPlan.isPresent()) {
            return enrichAndBuildResponse(existingPlan.get(), familyGroupId);
        }

        // Auto-crear plan vacío con 14 slots IMPROVISADO
        LocalDate startDate = computeStartDate(effectiveYear, effectiveWeek);
        LocalDate endDate = startDate.plusDays(6);

        WeeklyPlan newPlan = WeeklyPlan.create(familyGroupId, effectiveYear, effectiveWeek, startDate, endDate);

        DiaSemana[] dias = DiaSemana.values();
        List<PlanDay> emptyDays = new ArrayList<>();
        for (DiaSemana dia : dias) {
            emptyDays.add(PlanDay.create(newPlan.getId(), dia, TipoComida.COMIDA));
            emptyDays.add(PlanDay.create(newPlan.getId(), dia, TipoComida.CENA));
        }
        newPlan.setDias(emptyDays);

        WeeklyPlan savedPlan = weeklyPlanRepository.save(newPlan);
        log.info("Plan vacío auto-creado: familia={}, año={}, semana={}", familyGroupId, effectiveYear, effectiveWeek);

        return enrichAndBuildResponse(savedPlan, familyGroupId);
    }

    /**
     * Guarda un plan completo (crea o reemplaza) para una semana específica.
     * Solo ADMIN puede hacerlo.
     */
    @Transactional
    public WeeklyPlanResponse guardarPlan(UUID familyGroupId, SaveWeeklyPlanRequest request, UUID userId) {
        validateAdmin(userId, familyGroupId);

        LocalDate startDate = computeStartDate(request.year(), request.weekNumber());
        LocalDate endDate = startDate.plusDays(6);

        // Eliminar plan existente si lo hay
        weeklyPlanRepository.deleteByFamilyGroupAndWeek(familyGroupId, request.year(), request.weekNumber());

        WeeklyPlan plan = weeklyPlanMapper.toDomain(request);
        plan.setFamilyGroupId(familyGroupId);
        plan.setStartDate(startDate);
        plan.setEndDate(endDate);

        List<PlanDay> validatedDays = new ArrayList<>();
        for (SaveWeeklyPlanRequest.PlanDayRequest dayRequest : request.dias()) {
            PlanDay day = weeklyPlanMapper.toPlanDayDomain(dayRequest);
            day.setWeeklyPlanId(plan.getId());
            day.setId(UUID.randomUUID());

            if (day.getRecetaId() != null) {
                Recipe recipe = recipeRepository.findById(day.getRecetaId())
                        .orElseThrow(() -> new RecipeNotFoundException("Receta no encontrada: " + day.getRecetaId()));
                validateRecipeInFamily(recipe, familyGroupId);
                day.setRecetaNombre(recipe.getNombre());
                day.setRecetaTiempoMinutos(recipe.getTiempoMinutos());
                day.setAlergenosAdvertencia(checkAlergenos(familyGroupId, recipe));
            }

            if (day.getEstado() == EstadoDia.SOBRAS && day.getSobrasOrigenDia() != null) {
                validateSobrasReference(validatedDays, day);
            }

            validatedDays.add(day);
        }

        plan.setDias(validatedDays);
        WeeklyPlan savedPlan = weeklyPlanRepository.save(plan);

        log.info("Plan semanal guardado: familia={}, año={}, semana={}",
                familyGroupId, request.year(), request.weekNumber());

        return buildResponse(savedPlan);
    }

    /**
     * Actualiza un slot específico de un día.
     * Solo ADMIN puede hacerlo.
     */
    @Transactional
    public PlanDayResponse actualizarDia(UUID familyGroupId, int year, int weekNumber,
                                          UpdateDayRequest request, UUID userId) {
        validateAdmin(userId, familyGroupId);

        WeeklyPlan plan = weeklyPlanRepository.findByFamilyGroupAndWeek(familyGroupId, year, weekNumber)
                .orElseThrow(() -> new WeeklyPlanNotFoundException(
                        "No existe un plan para la semana " + weekNumber + " del año " + year));

        PlanDay dayToUpdate = plan.getDias().stream()
                .filter(d -> d.getDia() == request.dia() && d.getTipo() == request.tipo())
                .findFirst()
                .orElseThrow(() -> new WeeklyPlanNotFoundException(
                        "No existe el slot " + request.dia() + " " + request.tipo() + " en este plan"));

        dayToUpdate.setRecetaId(request.recetaId());
        dayToUpdate.setEstado(request.estado());
        dayToUpdate.setSobrasOrigenDia(request.sobrasOrigenDia());
        dayToUpdate.setSobrasOrigenTipo(request.sobrasOrigenTipo());

        if (request.recetaId() != null) {
            Recipe recipe = recipeRepository.findById(request.recetaId())
                    .orElseThrow(() -> new RecipeNotFoundException("Receta no encontrada: " + request.recetaId()));
            validateRecipeInFamily(recipe, familyGroupId);
            dayToUpdate.setRecetaNombre(recipe.getNombre());
            dayToUpdate.setRecetaTiempoMinutos(recipe.getTiempoMinutos());
            dayToUpdate.setAlergenosAdvertencia(checkAlergenos(familyGroupId, recipe));
        } else {
            dayToUpdate.setRecetaNombre(null);
            dayToUpdate.setRecetaTiempoMinutos(null);
            dayToUpdate.setAlergenosAdvertencia(false);
        }

        if (request.estado() == EstadoDia.SOBRAS && request.sobrasOrigenDia() != null) {
            validateSobrasReference(plan.getDias(), dayToUpdate);
        }

        weeklyPlanRepository.updateDay(dayToUpdate);

        log.info("Día actualizado: familia={}, dia={}, tipo={}, estado={}",
                familyGroupId, request.dia(), request.tipo(), request.estado());

        // dayToUpdate ya tiene recetaNombre y recetaTiempoMinutos (NO los perdió)
        return buildDayResponse(dayToUpdate);
    }

    /**
     * Genera un menú automático para la semana y lo GUARDA en BBDD.
     * Si completar=true, solo rellena los slots IMPROVISADO, preservando los ya asignados.
     */
    @Transactional
    public WeeklyPlanResponse generarMenu(UUID familyGroupId, GenerateMenuRequest request, UUID userId) {
        validateAdmin(userId, familyGroupId);

        // 1. Obtener recetas de la familia
        List<Recipe> allRecipes = recipeRepository.findByFamilyGroupId(familyGroupId);
        if (allRecipes.isEmpty()) {
            throw new WeeklyPlanNotFoundException("No hay recetas disponibles en el grupo familiar para generar un menú");
        }

        // Excluir recetas explícitas
        Set<UUID> excludedIds = request.recetasExcluidas() != null
                ? new HashSet<>(request.recetasExcluidas())
                : new HashSet<>();

        // 2. Filtrar por alérgenos y nivel de cocina
        Set<String> allAlergenos = getAllFamilyAlergenos(familyGroupId);
        String nivelCocinaAdmin = getAdminNivelCocina(userId);

        List<Recipe> filtradas = allRecipes.stream()
                .filter(r -> !excludedIds.contains(r.getId()))
                .filter(r -> !hasAlergenosConflict(r, allAlergenos))
                .filter(r -> matchesNivelCocina(r, nivelCocinaAdmin))
                .collect(Collectors.toList());

        if (filtradas.isEmpty()) {
            throw new WeeklyPlanNotFoundException("No hay recetas disponibles después de aplicar los filtros");
        }

        // 3. Evitar repetición: historial últimas 2 semanas
        Set<UUID> recentlyUsed = getRecentlyUsedRecipeIds(familyGroupId, 2);
        List<Recipe> noRepeated = filtradas.stream()
                .filter(r -> !recentlyUsed.contains(r.getId()))
                .collect(Collectors.toList());
        List<Recipe> pool = noRepeated.size() >= 14 ? noRepeated : filtradas;

        // 4. Priorizar favoritas
        if (request.preferenciasUsar()) {
            pool.sort((a, b) -> {
                int favCompare = Boolean.compare(b.isFavorita(), a.isFavorita());
                if (favCompare != 0) return favCompare;
                return a.getNombre().compareTo(b.getNombre());
            });
        } else {
            Collections.shuffle(pool, new Random());
        }

        // 5. Obtener o cargar el plan existente
        LocalDate today = LocalDate.now();
        WeekFields weekFields = WeekFields.ISO;
        int effectiveYear = request.year() != null ? request.year() : today.get(weekFields.weekBasedYear());
        int effectiveWeek = request.weekNumber() != null ? request.weekNumber() : today.get(weekFields.weekOfWeekBasedYear());
        LocalDate startDate = computeStartDate(effectiveYear, effectiveWeek);
        LocalDate endDate = startDate.plusDays(6);

        WeeklyPlan plan;
        Map<String, PlanDay> existingSlots = new LinkedHashMap<>();

        if (request.completar()) {
            // Modo completar: cargar plan existente, preservar slots ya asignados
            Optional<WeeklyPlan> existingOpt = weeklyPlanRepository
                    .findByFamilyGroupAndWeek(familyGroupId, effectiveYear, effectiveWeek);
            if (existingOpt.isPresent()) {
                plan = existingOpt.get();
                for (PlanDay day : plan.getDias()) {
                    String key = day.getDia().name() + "_" + day.getTipo().name();
                    existingSlots.put(key, day);
                }
            } else {
                plan = WeeklyPlan.create(familyGroupId, effectiveYear, effectiveWeek, startDate, endDate);
            }
        } else {
            // Modo reemplazar: borrar plan anterior si existe
            weeklyPlanRepository.deleteByFamilyGroupAndWeek(familyGroupId, effectiveYear, effectiveWeek);
            plan = WeeklyPlan.create(familyGroupId, effectiveYear, effectiveWeek, startDate, endDate);
        }

        // 6. Asignar recetas: 7 días × 2 slots = 14 slots
        DiaSemana[] dias = DiaSemana.values();
        TipoComida[] tipos = { TipoComida.COMIDA, TipoComida.CENA };

        List<PlanDay> newDays = new ArrayList<>();
        int recipeIndex = 0;
        Map<String, PlanDay> sobrasSuggestions = new LinkedHashMap<>();

        for (int i = 0; i < dias.length; i++) {
            DiaSemana dia = dias[i];
            for (TipoComida tipo : tipos) {
                String slotKey = dia.name() + "_" + tipo.name();

                if (request.completar() && existingSlots.containsKey(slotKey)) {
                    // Preservar slot existente si NO es IMPROVISADO
                    PlanDay existing = existingSlots.get(slotKey);
                    if (existing.getEstado() != EstadoDia.IMPROVISADO) {
                        newDays.add(existing);
                        continue;
                    }
                }

                if (recipeIndex >= pool.size()) {
                    recipeIndex = 0;
                }

                Recipe selectedRecipe = pool.get(recipeIndex);

                PlanDay day;
                if (request.completar() && existingSlots.containsKey(slotKey)) {
                    // Reutilizar el día existente (IMPROVISADO) → será UPDATE
                    day = existingSlots.get(slotKey);
                } else {
                    // Nuevo día → será INSERT
                    day = PlanDay.create(plan.getId(), dia, tipo);
                }
                day.setRecetaId(selectedRecipe.getId());
                day.setRecetaNombre(selectedRecipe.getNombre());
                day.setRecetaTiempoMinutos(selectedRecipe.getTiempoMinutos());
                day.setEstado(EstadoDia.NORMAL);
                day.setAlergenosAdvertencia(hasAlergenosConflict(selectedRecipe, allAlergenos));

                // Sugerir SOBRAS si raciones > numeroPersonas
                if (selectedRecipe.getRaciones() != null
                        && selectedRecipe.getRaciones() > request.numeroPersonas()
                        && i < dias.length - 1) {
                    DiaSemana nextDia = dias[i + 1];
                    String nextKey = nextDia.name() + "_" + tipo.name();

                    if (!request.completar() || !existingSlots.containsKey(nextKey)
                            || existingSlots.get(nextKey).getEstado() == EstadoDia.IMPROVISADO) {
                        PlanDay sobrasDay;
                        if (request.completar() && existingSlots.containsKey(nextKey)) {
                            // Reutilizar el día existente (IMPROVISADO) → será UPDATE
                            sobrasDay = existingSlots.get(nextKey);
                        } else {
                            // Nuevo día → será INSERT
                            sobrasDay = PlanDay.create(plan.getId(), nextDia, tipo);
                        }
                        sobrasDay.setEstado(EstadoDia.SOBRAS);
                        sobrasDay.setSobrasOrigenDia(dia);
                        sobrasDay.setSobrasOrigenTipo(tipo);
                        sobrasDay.setRecetaId(selectedRecipe.getId());
                        sobrasDay.setRecetaNombre(selectedRecipe.getNombre());
                        sobrasDay.setRecetaTiempoMinutos(selectedRecipe.getTiempoMinutos());
                        sobrasDay.setAlergenosAdvertencia(day.isAlergenosAdvertencia());
                        sobrasSuggestions.put(nextKey, sobrasDay);
                    }
                }

                newDays.add(day);
                recipeIndex++;
            }
        }

        // Reemplazar slots del día siguiente con SOBRAS
        for (int i = 0; i < newDays.size(); i++) {
            PlanDay day = newDays.get(i);
            String key = day.getDia().name() + "_" + day.getTipo().name();
            if (sobrasSuggestions.containsKey(key) && day.getEstado() == EstadoDia.IMPROVISADO) {
                newDays.set(i, sobrasSuggestions.get(key));
            }
        }

        plan.setDias(newDays);

        // 7. GUARDAR en BBDD
        WeeklyPlan savedPlan = weeklyPlanRepository.save(plan);

        log.info("Menú generado y guardado: familia={}, completar={}, recetas={}, slots={}",
                familyGroupId, request.completar(), pool.size(), savedPlan.getDias().size());

        return enrichAndBuildResponse(savedPlan, familyGroupId);
    }

    // ========== MÉTODOS PRIVADOS DE APOYO ==========

    private void validateUserInFamily(UUID userId, UUID familyGroupId) {
        if (!familyMemberRepository.existsByUserIdAndFamilyGroupId(userId, familyGroupId)) {
            throw new UnauthorizedException("No perteneces a este grupo familiar");
        }
    }

    private void validateAdmin(UUID userId, UUID familyGroupId) {
        FamilyMember member = familyMemberRepository.findByUserIdAndFamilyGroupId(userId, familyGroupId)
                .orElseThrow(() -> new UnauthorizedException("No eres miembro de este grupo familiar"));

        if (member.getRole() != FamilyRole.ADMIN) {
            throw new UnauthorizedException("Solo el administrador puede gestionar el plan semanal");
        }
    }

    private void validateRecipeInFamily(Recipe recipe, UUID familyGroupId) {
        if (recipe.getFamilyGroupId() == null || !recipe.getFamilyGroupId().equals(familyGroupId)) {
            throw new UnauthorizedException("La receta no pertenece a este grupo familiar");
        }
    }

    private void validateSobrasReference(List<PlanDay> existingDays, PlanDay currentDay) {
        boolean found = existingDays.stream()
                .anyMatch(d -> d.getDia() == currentDay.getSobrasOrigenDia()
                        && d.getTipo() == currentDay.getSobrasOrigenTipo()
                        && d.getRecetaId() != null);
        if (!found) {
            log.warn("Referencia SOBRAS no encontrada: dia={}, tipo={}",
                    currentDay.getSobrasOrigenDia(), currentDay.getSobrasOrigenTipo());
        }
    }

    private boolean checkAlergenos(UUID familyGroupId, Recipe recipe) {
        Set<String> allAlergenos = getAllFamilyAlergenos(familyGroupId);
        return hasAlergenosConflict(recipe, allAlergenos);
    }

    private Set<String> getAllFamilyAlergenos(UUID familyGroupId) {
        List<FamilyMember> members = familyMemberRepository.findByFamilyGroupId(familyGroupId);
        Set<String> alergenos = new HashSet<>();
        for (FamilyMember member : members) {
            userRepository.findById(member.getUserId()).ifPresent(user -> {
                if (user.getPreferencias() != null
                        && user.getPreferencias().getRestriccionesAlimentarias() != null) {
                    String restricciones = user.getPreferencias().getRestriccionesAlimentarias();
                    try {
                        // Formato JSON: ["marisco","gluten","lacteos"]
                        String[] items = restricciones.replace("[", "").replace("]", "")
                                .replace("\"", "").split(",");
                        for (String item : items) {
                            String trimmed = item.trim();
                            if (!trimmed.isEmpty()) {
                                alergenos.add(trimmed.toLowerCase());
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Error al parsear restricciones alimentarias: {}", restricciones);
                    }
                }
            });
        }
        return alergenos;
    }

    private boolean hasAlergenosConflict(Recipe recipe, Set<String> alergenos) {
        if (alergenos.isEmpty()) return false;
        if (recipe.getIngredientes() == null) return false;

        return recipe.getIngredientes().stream()
                .anyMatch(ing -> {
                    String nombre = ing.getNombre() != null ? ing.getNombre().toLowerCase() : "";
                    return alergenos.stream().anyMatch(nombre::contains);
                });
    }

    private boolean matchesNivelCocina(Recipe recipe, String nivelCocina) {
        if (nivelCocina == null) return true;

        // Si el nivel es BASICO, descartamos recetas con tiempo > 60 min
        // Si es MEDIO, descartamos recetas con tiempo > 120 min
        // Si es AVANZADO, sin restricción
        if (recipe.getTiempoMinutos() == null) return true;

        return switch (nivelCocina.toUpperCase()) {
            case "BASICO" -> recipe.getTiempoMinutos() <= 60;
            case "MEDIO" -> recipe.getTiempoMinutos() <= 120;
            default -> true;
        };
    }

    private String getAdminNivelCocina(UUID userId) {
        return userRepository.findById(userId)
                .map(User::getPreferencias)
                .map(Preferences::getNivelCocina)
                .orElse(null);
    }

    private Set<UUID> getRecentlyUsedRecipeIds(UUID familyGroupId, int weeks) {
        List<WeeklyPlan> history = weeklyPlanRepository.findHistoryByFamilyGroup(familyGroupId);
        Set<UUID> used = new HashSet<>();

        LocalDate cutoff = LocalDate.now().minusWeeks(weeks);

        for (WeeklyPlan plan : history) {
            if (plan.getStartDate() != null && plan.getStartDate().isAfter(cutoff)) {
                for (PlanDay day : plan.getDias()) {
                    if (day.getRecetaId() != null) {
                        used.add(day.getRecetaId());
                    }
                }
            }
        }
        return used;
    }

    private LocalDate computeStartDate(int year, int weekNumber) {
        WeekFields weekFields = WeekFields.ISO;
        return LocalDate.now()
                .withYear(year)
                .with(weekFields.weekOfYear(), weekNumber)
                .with(DayOfWeek.MONDAY);
    }

    private WeeklyPlanResponse enrichAndBuildResponse(WeeklyPlan plan, UUID familyGroupId) {
        enrichPlanDays(plan, familyGroupId);
        return buildResponse(plan);
    }

    private void enrichPlanDays(WeeklyPlan plan, UUID familyGroupId) {
        Set<String> allAlergenos = getAllFamilyAlergenos(familyGroupId);

        for (PlanDay day : plan.getDias()) {
            if (day.getRecetaId() != null) {
                recipeRepository.findById(day.getRecetaId()).ifPresent(recipe -> {
                    day.setRecetaNombre(recipe.getNombre());
                    day.setRecetaTiempoMinutos(recipe.getTiempoMinutos());
                    day.setAlergenosAdvertencia(hasAlergenosConflict(recipe, allAlergenos));
                });
            }
        }
    }

    private WeeklyPlanResponse buildResponse(WeeklyPlan plan) {
        List<PlanDayResponse> dayResponses = plan.getDias().stream()
                .map(this::buildDayResponse)
                .toList();

        return new WeeklyPlanResponse(
                plan.getId(),
                plan.getFamilyGroupId(),
                plan.getYear(),
                plan.getWeekNumber(),
                plan.getStartDate(),
                plan.getEndDate(),
                dayResponses
        );
    }

    private PlanDayResponse buildDayResponse(PlanDay day) {
        PlanDayResponse.RecetaResumen receta = null;
        if (day.getRecetaId() != null) {
            receta = new PlanDayResponse.RecetaResumen(
                    day.getRecetaId(),
                    day.getRecetaNombre(),
                    day.getRecetaTiempoMinutos()
            );
        }

        // Resolver sobrasOrigenRecetaId y nombre
        UUID sobrasOrigenRecetaId = day.getRecetaId();
        String sobrasOrigenRecetaNombre = day.getRecetaNombre();

        // Si es SOBRAS y tiene referencia a origen, buscar esa receta
        if (day.getEstado() == EstadoDia.SOBRAS
                && day.getSobrasOrigenDia() != null
                && day.getSobrasOrigenTipo() != null) {
            // La receta ya está enriquecida del origen
            if (day.getRecetaId() != null && day.getRecetaNombre() == null) {
                recipeRepository.findById(day.getRecetaId()).ifPresent(r -> {
                    day.setRecetaNombre(r.getNombre());
                    day.setRecetaTiempoMinutos(r.getTiempoMinutos());
                });
            }
            sobrasOrigenRecetaNombre = day.getRecetaNombre();
        }

        return new PlanDayResponse(
                day.getId(),
                day.getDia(),
                day.getTipo(),
                receta,
                day.getEstado(),
                day.getSobrasOrigenDia(),
                day.getSobrasOrigenTipo(),
                sobrasOrigenRecetaId,
                sobrasOrigenRecetaNombre,
                day.isAlergenosAdvertencia()
        );
    }
}
