package com.familyfood.application.port.repository;

import com.familyfood.domain.model.PlanDay;
import com.familyfood.domain.model.WeeklyPlan;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de repositorio para planes semanales.
 */
public interface WeeklyPlanRepository {

    Optional<WeeklyPlan> findByFamilyGroupAndWeek(UUID familyGroupId, int year, int weekNumber);

    WeeklyPlan save(WeeklyPlan plan);

    PlanDay updateDay(PlanDay day);

    List<WeeklyPlan> findHistoryByFamilyGroup(UUID familyGroupId);

    void deleteByFamilyGroupAndWeek(UUID familyGroupId, int year, int weekNumber);
}
