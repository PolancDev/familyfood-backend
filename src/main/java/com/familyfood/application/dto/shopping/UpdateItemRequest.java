package com.familyfood.application.dto.shopping;

public record UpdateItemRequest(
        Boolean comprado,
        Double cantidad,
        String unidad
) {}
