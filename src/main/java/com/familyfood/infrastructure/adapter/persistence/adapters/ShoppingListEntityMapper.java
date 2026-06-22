package com.familyfood.infrastructure.adapter.persistence.adapters;

import com.familyfood.domain.model.ShoppingItem;
import com.familyfood.domain.model.ShoppingList;
import com.familyfood.infrastructure.adapter.persistence.entities.ShoppingItemEntity;
import com.familyfood.infrastructure.adapter.persistence.entities.ShoppingListEntity;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = ComponentModel.SPRING)
public interface ShoppingListEntityMapper {

    ShoppingList toDomain(ShoppingListEntity entity);

    List<ShoppingList> toDomainList(List<ShoppingListEntity> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "items", ignore = true)
    ShoppingListEntity toEntityForCreate(ShoppingList domain);

    @Named("toItemDomain")
    @Mapping(target = "shoppingListId", ignore = true)
    ShoppingItem toItemDomain(ShoppingItemEntity entity);

    @IterableMapping(qualifiedByName = "toItemDomain")
    List<ShoppingItem> toItemDomainList(List<ShoppingItemEntity> entities);

    @Named("toItemEntityForCreate")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shoppingList", ignore = true)
    ShoppingItemEntity toItemEntityForCreate(ShoppingItem domain);

    @IterableMapping(qualifiedByName = "toItemEntityForCreate")
    List<ShoppingItemEntity> toItemEntityList(List<ShoppingItem> items);
}
