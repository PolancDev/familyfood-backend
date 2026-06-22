package com.familyfood.application.dto.shopping;

import java.util.List;
import java.util.UUID;

public record ShoppingListResponse(
        UUID id,
        UUID familyGroupId,
        int year,
        int weekNumber,
        String semana,
        List<String> categorias,
        List<ShoppingItemResponse> items
) {}
