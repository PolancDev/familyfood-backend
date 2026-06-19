package com.familyfood.application.dto.plan;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record WeeklyPlanResponse(
    UUID id,
    UUID familyGroupId,
    int year,
    int weekNumber,
    LocalDate startDate,
    LocalDate endDate,
    List<PlanDayResponse> dias
) {}
