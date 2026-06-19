package com.familyfood.infrastructure.adapter.persistence.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "weekly_plans",
        uniqueConstraints = @UniqueConstraint(columnNames = {"family_group_id", "year", "week_number"}))
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class WeeklyPlanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "family_group_id", nullable = false)
    private UUID familyGroupId;

    @Column(nullable = false)
    private int year;

    @Column(name = "week_number", nullable = false)
    private int weekNumber;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @OneToMany(mappedBy = "weeklyPlan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PlanDayEntity> dias = new ArrayList<>();

    @Version
    @Column(nullable = false)
    private Long version;

    public void addDay(PlanDayEntity day) {
        dias.add(day);
        day.setWeeklyPlan(this);
    }
}
