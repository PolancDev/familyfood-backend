package com.familyfood.application.dto.shopping;

import java.util.List;
import java.util.UUID;

public record ShoppingItemResponse(
        UUID id,
        String ingrediente,
        Double cantidad,
        String unidad,
        String categoria,
        boolean comprado,
        boolean esManual,
        List<UUID> recetasOrigen
) {}
