package com.familyfood.infrastructure.adapter.persistence.repository;

import com.familyfood.infrastructure.adapter.persistence.entities.WeeklyPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataWeeklyPlanRepository extends JpaRepository<WeeklyPlanEntity, UUID> {

    Optional<WeeklyPlanEntity> findByFamilyGroupIdAndYearAndWeekNumber(
            UUID familyGroupId, int year, int weekNumber);

    List<WeeklyPlanEntity> findByFamilyGroupIdOrderByYearDescWeekNumberDesc(UUID familyGroupId);

    @Modifying
    @Query("DELETE FROM WeeklyPlanEntity w WHERE w.familyGroupId = :familyGroupId AND w.year = :year AND w.weekNumber = :weekNumber")
    void deleteByFamilyGroupIdAndYearAndWeekNumber(
            @Param("familyGroupId") UUID familyGroupId,
            @Param("year") int year,
            @Param("weekNumber") int weekNumber);
}
