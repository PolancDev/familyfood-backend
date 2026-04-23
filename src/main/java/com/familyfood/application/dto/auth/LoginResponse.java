package com.familyfood.application.dto.auth;

import java.util.UUID;

public record LoginResponse(
    String token,
    UserInfo user
) {
    public record UserInfo(
        UUID id,
        String email,
        String nombre,
        String role
    ) {}
}