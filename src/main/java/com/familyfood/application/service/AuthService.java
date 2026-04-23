package com.familyfood.application.service;

import com.familyfood.application.dto.auth.*;
//import com.familyfood.application.mapper.AuthMapper;
import com.familyfood.application.mapper.AuthResponseMapper;
import com.familyfood.application.port.repository.JwtService;
import com.familyfood.application.port.repository.PasswordEncoder;
import com.familyfood.application.port.repository.UserRepository;
import com.familyfood.domain.exception.EmailAlreadyExistsException;
import com.familyfood.domain.exception.InvalidCredentialsException;
import com.familyfood.domain.exception.InvalidRoleException;
import com.familyfood.domain.model.Role;
import com.familyfood.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import static com.familyfood.domain.model.Role.ADMIN;

@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    //private final AuthMapper authMapper;
    private final AuthResponseMapper authResponseMapper;

    public RegisterResponse registro(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("El email ya está registrado");
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        Role role = parseRole(request.role());
        log.info("User role assigned: {}", role);
        
        User user = User.builder()
                .email(request.email())
                .password(encodedPassword)
                .nombre(request.nombre())
                .fechaActualizacion(LocalDateTime.now())
                .fechaCreacion(LocalDateTime.now())
                .preferencias(null)
                .role(role)
                .build();
        log.info("User que envio: {}", user);
        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(savedUser);

        return authResponseMapper.toRegisterResponse(savedUser, token);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales incorrectas"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Credenciales incorrectas");
        }

        String token = jwtService.generateToken(user);
        return authResponseMapper.toLoginResponse(token, user);
    }

    private Role parseRole(String roleStr) {
        if (roleStr == null || roleStr.isBlank()) {
            return ADMIN;
        }
        try {
            return Role.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRoleException("Rol inválido: '" + roleStr + "'. Roles válidos: ADMIN, CONSUMER");
        }
    }
}