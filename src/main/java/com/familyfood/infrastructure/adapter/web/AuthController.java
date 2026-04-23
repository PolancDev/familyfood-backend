package com.familyfood.infrastructure.adapter.web;

import com.familyfood.application.dto.auth.*;
import com.familyfood.application.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> registro(@Valid @RequestBody RegisterRequest request) {
        log.info("Recibida solicitud de registro para email: {}", request.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registro(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Recibida solicitud de login para email: {}", request.email());
        return ResponseEntity.ok(authService.login(request));
    }
}