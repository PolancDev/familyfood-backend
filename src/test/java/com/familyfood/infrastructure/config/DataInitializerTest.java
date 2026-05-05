package com.familyfood.infrastructure.config;

import com.familyfood.application.port.repository.PasswordEncoder;
import com.familyfood.application.port.repository.UserRepository;
import com.familyfood.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DataInitializer - Inicializador de datos demo")
class DataInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        dataInitializer = new DataInitializer(userRepository, passwordEncoder);
    }

    @Nested
    @DisplayName("Ejecutar inicialización")
    class RunTests {

        @Test
        @DisplayName("Crear usuario admin y consumer cuando la BD está vacía")
        void shouldCreateAdminAndConsumerWhenDatabaseIsEmpty() {
            // Given
            when(userRepository.count()).thenReturn(0L);
            when(passwordEncoder.encode("admin123")).thenReturn("$2a$10$encodedAdmin");
            when(passwordEncoder.encode("consumer123")).thenReturn("$2a$10$encodedConsumer");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            dataInitializer.run();

            // Then
            verify(userRepository).count();
            verify(passwordEncoder).encode("admin123");
            verify(passwordEncoder).encode("consumer123");
            verify(userRepository, times(2)).save(userCaptor.capture());

            assertThat(userCaptor.getAllValues()).hasSize(2);

            User adminUser = userCaptor.getAllValues().get(0);
            assertThat(adminUser.getEmail()).isEqualTo("admin@familyfood.com");
            assertThat(adminUser.getPassword()).isEqualTo("$2a$10$encodedAdmin");
            assertThat(adminUser.getNombre()).isEqualTo("Admin FamilyFood");

            User consumerUser = userCaptor.getAllValues().get(1);
            assertThat(consumerUser.getEmail()).isEqualTo("consumer@familyfood.com");
            assertThat(consumerUser.getPassword()).isEqualTo("$2a$10$encodedConsumer");
            assertThat(consumerUser.getNombre()).isEqualTo("Consumer FamilyFood");
        }

        @Test
        @DisplayName("No crear usuarios cuando ya existen en la BD")
        void shouldNotCreateUsersWhenDatabaseAlreadyHasData() {
            // Given
            when(userRepository.count()).thenReturn(2L);

            // When
            dataInitializer.run();

            // Then
            verify(userRepository).count();
            verify(passwordEncoder, never()).encode(anyString());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Codificar contraseñas con BCrypt al crear usuarios")
        void shouldEncodePasswordsWithBCryptWhenCreatingUsers() {
            // Given
            when(userRepository.count()).thenReturn(0L);
            when(passwordEncoder.encode("admin123")).thenReturn("$2a$10$encodedAdmin");
            when(passwordEncoder.encode("consumer123")).thenReturn("$2a$10$encodedConsumer");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            dataInitializer.run();

            // Then
            verify(passwordEncoder).encode("admin123");
            verify(passwordEncoder).encode("consumer123");

            verify(userRepository, times(2)).save(userCaptor.capture());
            assertThat(userCaptor.getAllValues().get(0).getPassword()).isEqualTo("$2a$10$encodedAdmin");
            assertThat(userCaptor.getAllValues().get(1).getPassword()).isEqualTo("$2a$10$encodedConsumer");
        }

        @Test
        @DisplayName("Manejar excepción silenciosamente si falla la creación de usuarios")
        void shouldHandleExceptionSilentlyWhenUserCreationFails() {
            // Given
            when(userRepository.count()).thenReturn(0L);
            when(passwordEncoder.encode(anyString())).thenThrow(new RuntimeException("Error de BD"));

            // When
            dataInitializer.run();

            // Then
            verify(userRepository).count();
            verify(passwordEncoder).encode(anyString());
            verify(userRepository, never()).save(any(User.class));
        }
    }
}
