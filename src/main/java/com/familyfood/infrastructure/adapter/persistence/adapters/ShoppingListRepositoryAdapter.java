package com.familyfood.infrastructure.adapter.persistence.adapters;

import com.familyfood.application.port.repository.ShoppingListRepository;
import com.familyfood.domain.model.ShoppingItem;
import com.familyfood.domain.model.ShoppingList;
import com.familyfood.infrastructure.adapter.persistence.entities.ShoppingItemEntity;
import com.familyfood.infrastructure.adapter.persistence.entities.ShoppingListEntity;
import com.familyfood.infrastructure.adapter.persistence.repository.SpringDataShoppingItemRepository;
import com.familyfood.infrastructure.adapter.persistence.repository.SpringDataShoppingListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ShoppingListRepositoryAdapter implements ShoppingListRepository {

    private final SpringDataShoppingListRepository listRepository;
    private final SpringDataShoppingItemRepository itemRepository;
    private final ShoppingListEntityMapper entityMapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<ShoppingList> findByFamilyGroupAndWeek(UUID familyGroupId, int year, int weekNumber) {
        return listRepository.findByFamilyGroupIdAndYearAndWeekNumber(familyGroupId, year, weekNumber)
                .map(this::enrichFromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ShoppingItem> findItemById(UUID itemId) {
        return itemRepository.findById(itemId)
                .map(entityMapper::toItemDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ShoppingList> findListByItemId(UUID itemId) {
        return itemRepository.findById(itemId)
                .map(ShoppingItemEntity::getShoppingList)
                .map(this::enrichFromEntity);
    }

    @Override
    @Transactional
    public ShoppingList save(ShoppingList domain) {
        ShoppingListEntity entity;
        if (domain.getId() != null && listRepository.existsById(domain.getId())) {
            log.info("Lista de compra con ID existente -> UPDATE: {}", domain.getId());
            entity = listRepository.findById(domain.getId()).orElseThrow();
            updateEntityFromDomain(entity, domain);
        } else {
            log.info("Lista de compra sin ID -> CREATE");
            if (domain.getId() == null) {
                domain.setId(UUID.randomUUID());
            }
            entity = entityMapper.toEntityForCreate(domain);
            entity.setId(domain.getId());

            // Guardar lista primero
            final ShoppingListEntity savedEntity = listRepository.save(entity);

            // Luego guardar los items
            List<ShoppingItemEntity> itemEntities = domain.getItems().stream()
                    .map(d -> {
                        if (d.getId() == null) d.setId(UUID.randomUUID());
                        ShoppingItemEntity itemEntity = entityMapper.toItemEntityForCreate(d);
                        itemEntity.setId(d.getId());
                        itemEntity.setShoppingList(savedEntity);
                        return itemEntity;
                    })
                    .toList();

            itemEntities = itemRepository.saveAll(itemEntities);
            savedEntity.setItems(itemEntities);
            entity = savedEntity;
        }

        return enrichFromEntity(entity);
    }

    @Override
    @Transactional
    public ShoppingItem updateItem(ShoppingItem item) {
        ShoppingItemEntity entity = itemRepository.findById(item.getId())
                .orElseThrow(() -> new RuntimeException("ShoppingItem no encontrado: " + item.getId()));

        entity.setIngrediente(item.getIngrediente());
        entity.setCantidad(item.getCantidad());
        entity.setUnidad(item.getUnidad());
        entity.setCategoria(item.getCategoria());
        entity.setComprado(item.isComprado());
        entity.setEsManual(item.isEsManual());
        entity.setVersion(item.getVersion());

        ShoppingItemEntity saved = itemRepository.save(entity);
        return entityMapper.toItemDomain(saved);
    }

    @Override
    @Transactional
    public void delete(ShoppingList shoppingList) {
        listRepository.findByFamilyGroupIdAndYearAndWeekNumber(
                shoppingList.getFamilyGroupId(), shoppingList.getYear(), shoppingList.getWeekNumber())
                .ifPresent(list -> {
                    // Eliminar items primero (con flush para evitar duplicate key)
                    itemRepository.deleteAll(list.getItems());
                    itemRepository.flush();
                    list.getItems().clear();
                    listRepository.delete(list);
                    log.info("Lista de compra eliminada: familia={}, año={}, semana={}",
                            shoppingList.getFamilyGroupId(), shoppingList.getYear(), shoppingList.getWeekNumber());
                });
    }

    @Override
    @Transactional
    public void deleteItem(UUID itemId) {
        itemRepository.findById(itemId).ifPresent(item -> {
            ShoppingListEntity list = item.getShoppingList();
            list.getItems().remove(item);
            itemRepository.delete(item);
            listRepository.save(list);
            log.info("Item de compra eliminado: id={}", itemId);
        });
    }

    // ========== HELPERS ==========

    private void updateEntityFromDomain(ShoppingListEntity entity, ShoppingList domain) {
        entity.setFamilyGroupId(domain.getFamilyGroupId());
        entity.setYear(domain.getYear());
        entity.setWeekNumber(domain.getWeekNumber());
        entity.setVersion(domain.getVersion());

        // Indexar items existentes por ID para distinguir UPDATE vs CREATE
        Map<UUID, ShoppingItemEntity> existingById = entity.getItems().stream()
                .collect(Collectors.toMap(ShoppingItemEntity::getId, d -> d));

        // IDs de los items que se conservan (no se borran)
        Set<UUID> keptIds = new HashSet<>();

        List<ShoppingItemEntity> updatedItems = new ArrayList<>();
        for (ShoppingItem d : domain.getItems()) {
            if (d.getId() != null && existingById.containsKey(d.getId())) {
                // UPDATE: modificar entidad existente
                ShoppingItemEntity existing = existingById.get(d.getId());
                existing.setIngrediente(d.getIngrediente());
                existing.setCantidad(d.getCantidad());
                existing.setUnidad(d.getUnidad());
                existing.setCategoria(d.getCategoria());
                existing.setComprado(d.isComprado());
                existing.setEsManual(d.isEsManual());
                existing.setVersion(d.getVersion());
                keptIds.add(d.getId());
                updatedItems.add(existing);
            } else {
                // CREATE: nueva entidad
                if (d.getId() == null) d.setId(UUID.randomUUID());
                ShoppingItemEntity itemEntity = entityMapper.toItemEntityForCreate(d);
                itemEntity.setId(d.getId());
                itemEntity.setShoppingList(entity);
                keptIds.add(d.getId());
                updatedItems.add(itemEntity);
            }
        }

        // Eliminar items huérfanos (estaban antes pero ya no están en la lista)
        List<ShoppingItemEntity> toDelete = entity.getItems().stream()
                .filter(e -> !keptIds.contains(e.getId()))
                .toList();
        if (!toDelete.isEmpty()) {
            itemRepository.deleteAll(toDelete);
            itemRepository.flush();
        }

        // Reemplazar colección
        entity.getItems().clear();
        entity.getItems().addAll(updatedItems);
        listRepository.save(entity);
    }

    private ShoppingList enrichFromEntity(ShoppingListEntity entity) {
        ShoppingList list = entityMapper.toDomain(entity);
        List<ShoppingItem> items = entity.getItems().stream()
                .map(entityMapper::toItemDomain)
                .toList();
        list.setItems(items);
        return list;
    }
}
