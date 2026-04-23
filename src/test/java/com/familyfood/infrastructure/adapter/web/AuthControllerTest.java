package com.familyfood.infrastructure.adapter.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.familyfood.application.dto.auth.*;
import com.familyfood.application.service.AuthService;
import com.familyfood.domain.exception.EmailAlreadyExistsException;
import com.familyfood.domain.exception.InvalidCredentialsException;
import com.familyfood.domain.exception.InvalidRoleException;
import com.familyfood.domain.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    private MockMvc mockMvc;
    
    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("Register Endpoint Tests")
    class RegisterEndpointTests {

        @Test
        @DisplayName("Should register user successfully")
        void shouldRegisterUserSuccessfully() throws Exception {
            // Given
            RegisterRequest request = new RegisterRequest(
                    "test@example.com",
                    "password123",
                    "Test User",
                    null
            );

            RegisterResponse response = new RegisterResponse(
                    UUID.randomUUID(),
                    request.email(),
                    request.nombre(),
                    "ADMIN",
                    "jwt-token"
            );

            when(authService.registro(any(RegisterRequest.class))).thenReturn(response);

            // When & Then
            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email").value(request.email()))
                    .andExpect(jsonPath("$.nombre").value(request.nombre()))
                    .andExpect(jsonPath("$.token").value("jwt-token"));
        }

        @Test
        @DisplayName("Should return conflict when email already exists")
        void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {
            // Given
            RegisterRequest request = new RegisterRequest(
                    "existing@example.com",
                    "password123",
                    "Test User",
                    null
            );

            when(authService.registro(any(RegisterRequest.class)))
                    .thenThrow(new EmailAlreadyExistsException("El email ya está registrado"));

            // When & Then
            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("Login Endpoint Tests")
    class LoginEndpointTests {

        @Test
        @DisplayName("Should login user successfully")
        void shouldLoginUserSuccessfully() throws Exception {
            // Given
            LoginRequest request = new LoginRequest(
                    "test@example.com",
                    "password123"
            );

            LoginResponse response = new LoginResponse(
                    "jwt-token",
                    new LoginResponse.UserInfo(
                            UUID.randomUUID(),
                            request.email(),
                            "Test User",
                            "ADMIN"
                    )
            );

            when(authService.login(any(LoginRequest.class))).thenReturn(response);

            // When & Then
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("jwt-token"))
                    .andExpect(jsonPath("$.user.email").value(request.email()));
        }

        @Test
        @DisplayName("Should return unauthorized for invalid credentials")
        void shouldReturnUnauthorizedForInvalidCredentials() throws Exception {
            // Given
            LoginRequest request = new LoginRequest(
                    "test@example.com",
                    "wrongpassword"
            );

            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new InvalidCredentialsException("Credenciales incorrectas"));

            // When & Then
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Credenciales incorrectas"));
        }
    }
}