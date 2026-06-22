package com.familyfood.infrastructure.adapter.persistence.repository;

import com.familyfood.infrastructure.adapter.persistence.entities.ShoppingItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataShoppingItemRepository extends JpaRepository<ShoppingItemEntity, UUID> {

    void deleteByShoppingListId(UUID shoppingListId);
}
