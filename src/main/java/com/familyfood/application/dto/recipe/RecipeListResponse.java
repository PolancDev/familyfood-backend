package com.familyfood.application.dto.recipe;

import java.util.List;

public record RecipeListResponse(
    List<RecipeResponse> recetas
) {}
