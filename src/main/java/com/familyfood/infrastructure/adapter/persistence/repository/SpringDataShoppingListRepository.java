package com.familyfood.infrastructure.adapter.persistence.repository;

import com.familyfood.infrastructure.adapter.persistence.entities.ShoppingListEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataShoppingListRepository extends JpaRepository<ShoppingListEntity, UUID> {

    Optional<ShoppingListEntity> findByFamilyGroupIdAndYearAndWeekNumber(
            UUID familyGroupId, int year, int weekNumber);
}
