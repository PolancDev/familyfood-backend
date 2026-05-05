package com.familyfood.infrastructure.adapter.web;

import com.familyfood.domain.exception.*;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler - Manejador global de excepciones")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Nested
    @DisplayName("Manejar EmailAlreadyExistsException")
    class HandleEmailAlreadyExistsTests {

        @Test
        @DisplayName("Devolver 409 Conflict cuando el email ya existe")
        void shouldReturnConflictWhenEmailAlreadyExists() {
            // Given
            EmailAlreadyExistsException ex = new EmailAlreadyExistsException("El email ya está registrado");

            // When
            ResponseEntity<Map<String, Object>> response = exceptionHandler.handleEmailAlreadyExists(ex);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).containsEntry("status", 409);
            assertThat(response.getBody()).containsEntry("error", "El email ya está registrado");
            assertThat(response.getBody()).containsKey("timestamp");
        }
    }

    @Nested
    @DisplayName("Manejar InvalidCredentialsException")
    class HandleInvalidCredentialsTests {

        @Test
        @DisplayName("Devolver 401 Unauthorized cuando las credenciales son inválidas")
        void shouldReturnUnauthorizedWhenCredentialsAreInvalid() {
            // Given
            InvalidCredentialsException ex = new InvalidCredentialsException("Credenciales incorrectas");

            // When
            ResponseEntity<Map<String, Object>> response = exceptionHandler.handleInvalidCredentials(ex);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).containsEntry("status", 401);
            assertThat(response.getBody()).containsEntry("error", "Credenciales incorrectas");
            assertThat(response.getBody()).containsKey("timestamp");
        }
    }

    @Nested
    @DisplayName("Manejar UserNotFoundException")
    class HandleUserNotFoundTests {

        @Test
        @DisplayName("Devolver 404 Not Found cuando el usuario no existe")
        void shouldReturnNotFoundWhenUserDoesNotExist() {
            // Given
            UserNotFoundException ex = new UserNotFoundException("Usuario no encontrado");

            // When
            ResponseEntity<Map<String, Object>> response = exceptionHandler.handleUserNotFound(ex);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).containsEntry("status", 404);
            assertThat(response.getBody()).containsEntry("error", "Usuario no encontrado");
            assertThat(response.getBody()).containsKey("timestamp");
        }
    }

    @Nested
    @DisplayName("Manejar FamilyGroupNotFoundException")
    class HandleFamilyGroupNotFoundTests {

        @Test
        @DisplayName("Devolver 404 Not Found cuando el grupo familiar no existe")
        void shouldReturnNotFoundWhenFamilyGroupDoesNotExist() {
            // Given
            FamilyGroupNotFoundException ex = new FamilyGroupNotFoundException("Grupo familiar no encontrado");

            // When
            ResponseEntity<Map<String, Object>> response = exceptionHandler.handleFamilyGroupNotFound(ex);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).containsEntry("status", 404);
            assertThat(response.getBody()).containsEntry("error", "Grupo familiar no encontrado");
            assertThat(response.getBody()).containsKey("timestamp");
        }
    }

    @Nested
    @DisplayName("Manejar JoinRequestNotFoundException")
    class HandleJoinRequestNotFoundTests {

        @Test
        @DisplayName("Devolver 404 Not Found cuando la solicitud de unión no existe")
        void shouldReturnNotFoundWhenJoinRequestDoesNotExist() {
            // Given
            JoinRequestNotFoundException ex = new JoinRequestNotFoundException("Solicitud no encontrada");

            // When
            ResponseEntity<Map<String, Object>> response = exceptionHandler.handleJoinRequestNotFound(ex);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).containsEntry("status", 404);
            assertThat(response.getBody()).containsEntry("error", "Solicitud no encontrada");
            assertThat(response.getBody()).containsKey("timestamp");
        }
    }

    @Nested
    @DisplayName("Manejar InvalidRoleException")
    class HandleInvalidRoleTests {

        @Test
        @DisplayName("Devolver 400 Bad Request cuando el rol es inválido")
        void shouldReturnBadRequestWhenRoleIsInvalid() {
            // Given
            InvalidRoleException ex = new InvalidRoleException("Rol no válido");

            // When
            ResponseEntity<Map<String, Object>> response = exceptionHandler.handleInvalidRole(ex);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).containsEntry("status", 400);
            assertThat(response.getBody()).containsEntry("error", "Rol no válido");
            assertThat(response.getBody()).containsKey("timestamp");
        }
    }

    @Nested
    @DisplayName("Manejar RecipeNotFoundException")
    class HandleRecipeNotFoundTests {

        @Test
        @DisplayName("Devolver 404 Not Found cuando la receta no existe")
        void shouldReturnNotFoundWhenRecipeDoesNotExist() {
            // Given
            RecipeNotFoundException ex = new RecipeNotFoundException("Receta no encontrada");

            // When
            ResponseEntity<Map<String, Object>> response = exceptionHandler.handleRecipeNotFound(ex);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).containsEntry("status", 404);
            assertThat(response.getBody()).containsEntry("error", "Receta no encontrada");
            assertThat(response.getBody()).containsKey("timestamp");
        }
    }

    @Nested
    @DisplayName("Manejar OptimisticLockException")
    class HandleOptimisticLockTests {

        @Test
        @DisplayName("Devolver 409 Conflict cuando hay conflicto de concurrencia")
        void shouldReturnConflictWhenOptimisticLockingOccurs() {
            // Given
            OptimisticLockException ex = new OptimisticLockException("Conflicto de concurrencia");

            // When
            ResponseEntity<Map<String, Object>> response = exceptionHandler.handleOptimisticLock(ex);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).containsEntry("status", 409);
            assertThat(response.getBody()).containsEntry("error", "Conflicto de concurrencia");
            assertThat(response.getBody()).containsEntry("mensaje", "La receta ha sido modificada por otro usuario. Recarga los datos e inténtalo de nuevo.");
            assertThat(response.getBody()).containsKey("timestamp");
        }
    }

    @Nested
    @DisplayName("Manejar MethodArgumentNotValidException")
    class HandleValidationExceptionsTests {

        @Test
        @DisplayName("Devolver 400 Bad Request con errores de validación")
        void shouldReturnBadRequestWithValidationErrors() {
            // Given
            MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
            BindingResult bindingResult = mock(BindingResult.class);

            FieldError fieldError1 = new FieldError("object", "email", "El email no puede estar vacío");
            FieldError fieldError2 = new FieldError("object", "password", "La contraseña debe tener al menos 6 caracteres");

            when(ex.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

            // When
            ResponseEntity<Map<String, Object>> response = exceptionHandler.handleValidationExceptions(ex);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).containsEntry("status", 400);
            assertThat(response.getBody()).containsEntry("error", "Validation Failed");
            assertThat(response.getBody()).containsKey("timestamp");

            @SuppressWarnings("unchecked")
            Map<String, String> errors = (Map<String, String>) response.getBody().get("errors");
            assertThat(errors).isNotNull();
            assertThat(errors).containsEntry("email", "El email no puede estar vacío");
            assertThat(errors).containsEntry("password", "La contraseña debe tener al menos 6 caracteres");
        }
    }

    @Nested
    @DisplayName("Manejar UnauthorizedException")
    class HandleUnauthorizedTests {

        @Test
        @DisplayName("Devolver 403 Forbidden cuando el acceso no está autorizado")
        void shouldReturnForbiddenWhenAccessIsUnauthorized() {
            // Given
            UnauthorizedException ex = new UnauthorizedException("No tienes permisos para esta operación");

            // When
            ResponseEntity<Map<String, Object>> response = exceptionHandler.handleUnauthorized(ex);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).containsEntry("status", 403);
            assertThat(response.getBody()).containsEntry("error", "No tienes permisos para esta operación");
            assertThat(response.getBody()).containsKey("timestamp");
        }
    }
}
