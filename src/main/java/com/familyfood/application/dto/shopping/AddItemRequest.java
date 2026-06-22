package com.familyfood.application.dto.shopping;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddItemRequest(
        @NotBlank String ingrediente,
        @NotNull @Positive Double cantidad,
        @NotBlank String unidad,
        String categoria
) {}
