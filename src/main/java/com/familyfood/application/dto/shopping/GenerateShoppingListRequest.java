package com.familyfood.application.dto.shopping;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record GenerateShoppingListRequest(
        @NotNull Integer year,
        @NotNull Integer weekNumber,
        @NotNull @Positive Integer raciones,
        boolean incluirBasicos
) {}
