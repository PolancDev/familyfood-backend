package com.familyfood.infrastructure.adapter.persistence.adapters;

import com.familyfood.application.port.repository.WeeklyPlanRepository;
import com.familyfood.domain.model.PlanDay;
import com.familyfood.domain.model.WeeklyPlan;
import com.familyfood.infrastructure.adapter.persistence.entities.PlanDayEntity;
import com.familyfood.infrastructure.adapter.persistence.entities.WeeklyPlanEntity;
import com.familyfood.infrastructure.adapter.persistence.repository.SpringDataPlanDayRepository;
import com.familyfood.infrastructure.adapter.persistence.repository.SpringDataWeeklyPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class WeeklyPlanRepositoryAdapter implements WeeklyPlanRepository {

    private final SpringDataWeeklyPlanRepository planRepository;
    private final SpringDataPlanDayRepository dayRepository;
    private final WeeklyPlanEntityMapper entityMapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<WeeklyPlan> findByFamilyGroupAndWeek(UUID familyGroupId, int year, int weekNumber) {
        return planRepository.findByFamilyGroupIdAndYearAndWeekNumber(familyGroupId, year, weekNumber)
                .map(this::enrichFromEntity);
    }

    @Override
    @Transactional
    public WeeklyPlan save(WeeklyPlan plan) {
        WeeklyPlanEntity entity;
        if (plan.getId() != null && planRepository.existsById(plan.getId())) {
            log.info("Plan semanal con ID existente -> UPDATE: {}", plan.getId());
            entity = planRepository.findById(plan.getId()).orElseThrow();
            updateEntityFromDomain(entity, plan);
        } else {
            log.info("Plan semanal sin ID -> CREATE");
            if (plan.getId() == null) {
                plan.setId(UUID.randomUUID());
            }
            entity = entityMapper.toEntityForCreate(plan);
            entity.setId(plan.getId());

            // Guardar plan primero
            final WeeklyPlanEntity savedEntity = planRepository.save(entity);

            // Luego guardar los días
            List<PlanDayEntity> dayEntities = plan.getDias().stream()
                    .map(d -> {
                        if (d.getId() == null) d.setId(UUID.randomUUID());
                        PlanDayEntity dayEntity = entityMapper.toPlanDayEntityForCreate(d);
                        dayEntity.setId(d.getId());
                        dayEntity.setWeeklyPlan(savedEntity);
                        return dayEntity;
                    })
                    .toList();

            dayEntities = dayRepository.saveAll(dayEntities);
            savedEntity.setDias(dayEntities);
            entity = savedEntity;
        }

        return enrichFromEntity(entity);
    }

    @Override
    @Transactional
    public PlanDay updateDay(PlanDay day) {
        PlanDayEntity entity = dayRepository.findById(day.getId())
                .orElseThrow(() -> new RuntimeException("PlanDay no encontrado: " + day.getId()));

        entity.setDia(day.getDia().name());
        entity.setTipo(day.getTipo().name());
        entity.setRecetaId(day.getRecetaId());
        entity.setEstado(day.getEstado().name());
        entity.setSobrasOrigenDia(day.getSobrasOrigenDia() != null ? day.getSobrasOrigenDia().name() : null);
        entity.setSobrasOrigenTipo(day.getSobrasOrigenTipo() != null ? day.getSobrasOrigenTipo().name() : null);
        entity.setVersion(day.getVersion());

        PlanDayEntity saved = dayRepository.save(entity);
        return entityMapper.toPlanDayDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WeeklyPlan> findHistoryByFamilyGroup(UUID familyGroupId) {
        return planRepository.findByFamilyGroupIdOrderByYearDescWeekNumberDesc(familyGroupId)
                .stream()
                .map(this::enrichFromEntity)
                .toList();
    }

    @Override
    @Transactional
    public void deleteByFamilyGroupAndWeek(UUID familyGroupId, int year, int weekNumber) {
        planRepository.findByFamilyGroupIdAndYearAndWeekNumber(familyGroupId, year, weekNumber)
                .ifPresent(plan -> {
                    // Eliminar días primero (con flush para evitar duplicate key)
                    dayRepository.deleteAll(plan.getDias());
                    dayRepository.flush();
                    plan.getDias().clear();
                    planRepository.delete(plan);
                    log.info("Plan semanal eliminado: familia={}, año={}, semana={}", familyGroupId, year, weekNumber);
                });
    }

    // ========== HELPERS ==========

    private void updateEntityFromDomain(WeeklyPlanEntity entity, WeeklyPlan plan) {
        entity.setFamilyGroupId(plan.getFamilyGroupId());
        entity.setYear(plan.getYear());
        entity.setWeekNumber(plan.getWeekNumber());
        entity.setStartDate(plan.getStartDate());
        entity.setEndDate(plan.getEndDate());
        entity.setVersion(plan.getVersion());

        // Indexar días existentes por ID para distinguir UPDATE vs CREATE
        Map<UUID, PlanDayEntity> existingById = entity.getDias().stream()
                .collect(Collectors.toMap(PlanDayEntity::getId, d -> d));

        // IDs de los días que se conservan (no se borran)
        Set<UUID> keptIds = new HashSet<>();

        List<PlanDayEntity> updatedDays = new ArrayList<>();
        for (PlanDay d : plan.getDias()) {
            if (d.getId() != null && existingById.containsKey(d.getId())) {
                // UPDATE: modificar entidad existente
                PlanDayEntity existing = existingById.get(d.getId());
                existing.setDia(d.getDia().name());
                existing.setTipo(d.getTipo().name());
                existing.setRecetaId(d.getRecetaId());
                existing.setEstado(d.getEstado().name());
                existing.setSobrasOrigenDia(d.getSobrasOrigenDia() != null ? d.getSobrasOrigenDia().name() : null);
                existing.setSobrasOrigenTipo(d.getSobrasOrigenTipo() != null ? d.getSobrasOrigenTipo().name() : null);
                existing.setVersion(d.getVersion());
                keptIds.add(d.getId());
                updatedDays.add(existing);
            } else {
                // CREATE: nueva entidad
                if (d.getId() == null) d.setId(UUID.randomUUID());
                PlanDayEntity dayEntity = entityMapper.toPlanDayEntityForCreate(d);
                dayEntity.setId(d.getId());
                dayEntity.setWeeklyPlan(entity);
                keptIds.add(d.getId());
                updatedDays.add(dayEntity);
            }
        }

        // Eliminar días huérfanos (estaban antes pero ya no están en el plan)
        List<PlanDayEntity> toDelete = entity.getDias().stream()
                .filter(e -> !keptIds.contains(e.getId()))
                .toList();
        if (!toDelete.isEmpty()) {
            dayRepository.deleteAll(toDelete);
            dayRepository.flush();
        }

        // Reemplazar colección
        entity.getDias().clear();
        entity.getDias().addAll(updatedDays);
        planRepository.save(entity);
    }

    private WeeklyPlan enrichFromEntity(WeeklyPlanEntity entity) {
        WeeklyPlan plan = entityMapper.toDomain(entity);
        List<PlanDay> days = entity.getDias().stream()
                .map(entityMapper::toPlanDayDomain)
                .toList();
        plan.setDias(days);
        return plan;
    }
}
