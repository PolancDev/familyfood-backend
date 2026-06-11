package com.familyfood.infrastructure.adapter.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Tests de PasswordEncoderAdapter")
class PasswordEncoderAdapterTest {

    private PasswordEncoderAdapter passwordEncoderAdapter;

    @BeforeEach
    void setUp() {
        passwordEncoderAdapter = new PasswordEncoderAdapter();
    }

    @Nested
    @DisplayName("Tests de codificación")
    class EncodeTests {

        @Test
        @DisplayName("Debería codificar contraseña correctamente")
        void shouldEncodePasswordSuccessfully() {
            // Given
            String rawPassword = "mySecurePassword";

            // When
            String encoded = passwordEncoderAdapter.encode(rawPassword);

            // Then
            assertThat(encoded).isNotNull();
            assertThat(encoded).isNotEqualTo(rawPassword);
            assertThat(encoded.startsWith("$2a$")).isTrue();
        }

        @Test
        @DisplayName("Debería generar hashes diferentes para la misma contraseña")
        void shouldGenerateDifferentHashesForSamePassword() {
            // Given
            String rawPassword = "mySecurePassword";

            // When
            String encoded1 = passwordEncoderAdapter.encode(rawPassword);
            String encoded2 = passwordEncoderAdapter.encode(rawPassword);

            // Then
            assertThat(encoded1).isNotEqualTo(encoded2);
        }
    }

    @Nested
    @DisplayName("Tests de verificación")
    class MatchesTests {

        @Test
        @DisplayName("Debería verificar contraseña correcta")
        void shouldMatchCorrectPassword() {
            // Given
            String rawPassword = "mySecurePassword";
            String encodedPassword = passwordEncoderAdapter.encode(rawPassword);

            // When
            boolean matches = passwordEncoderAdapter.matches(rawPassword, encodedPassword);

            // Then
            assertThat(matches).isTrue();
        }

        @Test
        @DisplayName("No debería verificar contraseña incorrecta")
        void shouldNotMatchIncorrectPassword() {
            // Given
            String correctPassword = "mySecurePassword";
            String wrongPassword = "wrongPassword";
            String encodedPassword = passwordEncoderAdapter.encode(correctPassword);

            // When
            boolean matches = passwordEncoderAdapter.matches(wrongPassword, encodedPassword);

            // Then
            assertThat(matches).isFalse();
        }
    }
}
