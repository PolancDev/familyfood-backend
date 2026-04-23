package com.familyfood.application.dto.auth;

import java.util.UUID;

public record RegisterResponse(
    UUID id,
    String email,
    String nombre,
    String role,
    String token
) {}