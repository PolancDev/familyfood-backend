package com.familyfood.infrastructure.adapter.persistence.repository;

import com.familyfood.infrastructure.adapter.persistence.entities.PlanDayEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataPlanDayRepository extends JpaRepository<PlanDayEntity, UUID> {
}
