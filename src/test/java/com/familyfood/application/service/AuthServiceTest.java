package com.familyfood.application.service;

import com.familyfood.application.dto.auth.*;
import com.familyfood.application.mapper.AuthMapper;
import com.familyfood.application.mapper.AuthResponseMapper;
import com.familyfood.application.port.repository.JwtService;
import com.familyfood.application.port.repository.PasswordEncoder;
import com.familyfood.application.port.repository.UserRepository;
import com.familyfood.domain.exception.EmailAlreadyExistsException;
import com.familyfood.domain.exception.InvalidCredentialsException;
import com.familyfood.domain.model.Role;
import com.familyfood.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthMapper authMapper;

    @Mock
    private AuthResponseMapper authResponseMapper;

    private AuthService authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
       // authService = new AuthService(userRepository, passwordEncoder, jwtService, authMapper, authResponseMapper);
        authService = new AuthService(userRepository, passwordEncoder, jwtService, authResponseMapper);
        
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .password("$2a$10$encodedPassword")
                .nombre("Test User")
                .fechaCreacion(LocalDateTime.now())
                .role(Role.ADMIN)
                .build();

        registerRequest = new RegisterRequest(
                "test@example.com",
                "password123",
                "Test User",
                null
        );

        loginRequest = new LoginRequest(
                "test@example.com",
                "password123"
        );
    }

    @Nested
    @DisplayName("Registro Tests")
    class RegistroTests {

        @Test
        @DisplayName("Should register user successfully")
        void shouldRegisterUserSuccessfully() {
            // Given
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");
            when(authResponseMapper.toRegisterResponse(any(User.class), anyString()))
                    .thenReturn(new RegisterResponse(
                            testUser.getId(),
                            testUser.getEmail(),
                            testUser.getNombre(),
                            testUser.getRole().name(),
                            "jwt-token"
                    ));

            // When
            RegisterResponse response = authService.registro(registerRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(testUser.getId());
            assertThat(response.email()).isEqualTo(registerRequest.email());
            assertThat(response.nombre()).isEqualTo(registerRequest.nombre());
            assertThat(response.token()).isEqualTo("jwt-token");

            verify(userRepository).existsByEmail(registerRequest.email());
            verify(passwordEncoder).encode(registerRequest.password());
            verify(userRepository).save(any(User.class));
            verify(jwtService).generateToken(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailAlreadyExists() {
            // Given
            when(userRepository.existsByEmail(anyString())).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> authService.registro(registerRequest))
                    .isInstanceOf(EmailAlreadyExistsException.class)
                    .hasMessageContaining("El email ya está registrado");

            verify(userRepository).existsByEmail(registerRequest.email());
            verify(passwordEncoder, never()).encode(anyString());
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login user successfully")
        void shouldLoginUserSuccessfully() {
            // Given
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
            when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");
            when(authResponseMapper.toLoginResponse(anyString(), any(User.class)))
                    .thenReturn(new LoginResponse(
                            "jwt-token",
                            new LoginResponse.UserInfo(
                                    testUser.getId(),
                                    testUser.getEmail(),
                                    testUser.getNombre(),
                                    testUser.getRole().name()
                            )
                    ));

            // When
            LoginResponse response = authService.login(loginRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.token()).isEqualTo("jwt-token");
            assertThat(response.user()).isNotNull();
            assertThat(response.user().id()).isEqualTo(testUser.getId());
            assertThat(response.user().email()).isEqualTo(testUser.getEmail());
            assertThat(response.user().nombre()).isEqualTo(testUser.getNombre());

            verify(userRepository).findByEmail(loginRequest.email());
            verify(passwordEncoder).matches(loginRequest.password(), testUser.getPassword());
            verify(jwtService).generateToken(testUser);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessageContaining("Credenciales incorrectas");

            verify(userRepository).findByEmail(loginRequest.email());
            verify(passwordEncoder, never()).matches(anyString(), anyString());
        }

        @Test
        @DisplayName("Should throw exception when password is incorrect")
        void shouldThrowExceptionWhenPasswordIsIncorrect() {
            // Given
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessageContaining("Credenciales incorrectas");

            verify(userRepository).findByEmail(loginRequest.email());
            verify(passwordEncoder).matches(loginRequest.password(), testUser.getPassword());
            verify(jwtService, never()).generateToken(any(User.class));
        }
    }
}