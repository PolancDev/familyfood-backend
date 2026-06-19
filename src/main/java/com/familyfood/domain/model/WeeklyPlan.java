package com.familyfood.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklyPlan {
    private UUID id;
    private UUID familyGroupId;
    private int year;
    private int weekNumber;
    private LocalDate startDate;
    private LocalDate endDate;

    @Builder.Default
    private List<PlanDay> dias = new ArrayList<>();

    @Builder.Default
    private Long version = 0L;

    public static WeeklyPlan create(UUID familyGroupId, int year, int weekNumber,
                                     LocalDate startDate, LocalDate endDate) {
        return WeeklyPlan.builder()
                .id(UUID.randomUUID())
                .familyGroupId(familyGroupId)
                .year(year)
                .weekNumber(weekNumber)
                .startDate(startDate)
                .endDate(endDate)
                .dias(new ArrayList<>())
                .build();
    }
}
