package com.familyfood.application.port.repository;

import com.familyfood.domain.model.ShoppingItem;
import com.familyfood.domain.model.ShoppingList;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de repositorio para listas de compra.
 */
public interface ShoppingListRepository {

    Optional<ShoppingList> findByFamilyGroupAndWeek(UUID familyGroupId, int year, int weekNumber);

    Optional<ShoppingItem> findItemById(UUID itemId);

    Optional<ShoppingList> findListByItemId(UUID itemId);

    ShoppingList save(ShoppingList shoppingList);

    ShoppingItem updateItem(ShoppingItem item);

    void delete(ShoppingList shoppingList);

    void deleteItem(UUID itemId);
}
