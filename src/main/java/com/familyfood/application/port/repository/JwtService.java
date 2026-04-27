package com.familyfood.application.port.repository;

import com.familyfood.domain.model.User;

public interface JwtService {
    String generateToken(User user);
    String extractEmail(String token);
    boolean validateToken(String token, String email);
}