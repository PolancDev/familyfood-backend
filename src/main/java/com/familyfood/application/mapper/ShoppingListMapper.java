package com.familyfood.application.mapper;

import com.familyfood.application.dto.shopping.ShoppingItemResponse;
import com.familyfood.application.dto.shopping.ShoppingListResponse;
import com.familyfood.domain.model.ShoppingItem;
import com.familyfood.domain.model.ShoppingList;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = ComponentModel.SPRING)
public interface ShoppingListMapper {

    @Mapping(target = "semana", ignore = true)
    @Mapping(target = "categorias", ignore = true)
    @Mapping(target = "items", ignore = true)
    ShoppingListResponse toResponse(ShoppingList domain);

    @Mapping(target = "recetasOrigen", ignore = true)
    ShoppingItemResponse toItemResponse(ShoppingItem item);

    List<ShoppingItemResponse> toItemResponseList(List<ShoppingItem> items);
}
