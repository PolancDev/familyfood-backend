package com.familyfood.infrastructure.adapter.persistence.adapters;

import com.familyfood.domain.model.Recipe;
import com.familyfood.domain.model.RecipeIngredient;
import com.familyfood.infrastructure.adapter.persistence.entities.RecipeEntity;
import com.familyfood.infrastructure.adapter.persistence.entities.RecipeIngredientEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

import java.util.List;

@Mapper(componentModel = ComponentModel.SPRING)
public interface RecipeEntityMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    RecipeEntity toEntityForCreate(Recipe recipe);

    RecipeEntity toEntityForUpdate(Recipe recipe);

    Recipe toDomain(RecipeEntity entity);

    List<Recipe> toDomainList(List<RecipeEntity> entities);

    RecipeIngredientEntity toIngredientEntity(RecipeIngredient ingredient);

    RecipeIngredient toIngredientDomain(RecipeIngredientEntity entity);

    List<RecipeIngredientEntity> toIngredientEntityList(List<RecipeIngredient> ingredients);

    List<RecipeIngredient> toIngredientDomainList(List<RecipeIngredientEntity> entities);
}
