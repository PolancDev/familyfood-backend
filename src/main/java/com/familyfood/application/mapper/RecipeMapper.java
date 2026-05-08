package com.familyfood.application.mapper;

import com.familyfood.application.dto.recipe.CreateRecipeRequest;
import com.familyfood.application.dto.recipe.RecipeIngredientDTO;
import com.familyfood.application.dto.recipe.RecipeResponse;
import com.familyfood.application.dto.recipe.UpdateRecipeRequest;
import com.familyfood.domain.model.Recipe;
import com.familyfood.domain.model.RecipeIngredient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

import java.util.List;

@Mapper(componentModel = ComponentModel.SPRING)
public interface RecipeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "imagen", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "favorita", source = "favorita")
    Recipe toDomainFromCreate(CreateRecipeRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "favorita", ignore = true)
    @Mapping(target = "imagen", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "version", ignore = true)
    Recipe toDomainFromUpdate(UpdateRecipeRequest request);

    RecipeResponse toResponse(Recipe recipe);

    List<RecipeResponse> toResponseList(List<Recipe> recipes);

    RecipeIngredientDTO toIngredientDTO(RecipeIngredient ingredient);

    RecipeIngredient toIngredientDomain(RecipeIngredientDTO dto);
}
