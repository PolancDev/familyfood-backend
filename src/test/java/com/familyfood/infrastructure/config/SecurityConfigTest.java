package com.familyfood.infrastructure.config;

import com.familyfood.infrastructure.adapter.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests para SecurityConfig.
 * <p>
 * SecurityConfig es configuración declarativa de Spring Security que define beans
 * (SecurityFilterChain, PasswordEncoder, AuthenticationManager, AuthenticationProvider).
 * La mayoría de sus métodos son @Bean que dependen del contexto completo de Spring,
 * por lo que no se pueden testear unitariamente con Mockito.
 * <p>
 * Este test verifica:
 * - La anotación @EnableWebSecurity está presente
 * - El PasswordEncoder devuelto es BCryptPasswordEncoder (se puede instanciar sin Spring)
 * - La configuración de CORS se construye correctamente con orígenes, métodos y credenciales
 * - La estructura de la clase (anotaciones, constructor)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de SecurityConfig")
class SecurityConfigTest {

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private UserDetailsService userDetailsService;

    private SecurityConfig createSecurityConfig() {
        return new SecurityConfig(jwtAuthenticationFilter, userDetailsService);
    }

    // ========================================================================
    // Tests de anotaciones de clase
    // ========================================================================

    @Nested
    @DisplayName("Anotaciones de clase")
    class ClassAnnotationsTests {

        @Test
        @DisplayName("Debería tener la anotación @EnableWebSecurity")
        void shouldHaveEnableWebSecurityAnnotation() {
            // Given
            EnableWebSecurity annotation = SecurityConfig.class.getAnnotation(EnableWebSecurity.class);

            // Then
            assertThat(annotation).isNotNull();
        }

        @Test
        @DisplayName("Debería tener la anotación @Configuration")
        void shouldHaveConfigurationAnnotation() {
            // Given
            org.springframework.context.annotation.Configuration annotation =
                    SecurityConfig.class.getAnnotation(org.springframework.context.annotation.Configuration.class);

            // Then
            assertThat(annotation).isNotNull();
        }
    }

    // ========================================================================
    // Tests de PasswordEncoder
    // ========================================================================

    @Nested
    @DisplayName("PasswordEncoder (BCrypt)")
    class PasswordEncoderTests {

        @Test
        @DisplayName("Debería crear una instancia de BCryptPasswordEncoder")
        void shouldCreateBCryptPasswordEncoder() {
            // Given
            SecurityConfig securityConfig = createSecurityConfig();

            // When
            PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

            // Then
            assertThat(passwordEncoder).isNotNull();
            assertThat(passwordEncoder).isInstanceOf(BCryptPasswordEncoder.class);
        }

        @Test
        @DisplayName("Debería codificar una contraseña y verificar que coincide")
        void shouldEncodeAndMatchPasswords() {
            // Given
            SecurityConfig securityConfig = createSecurityConfig();
            PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
            String rawPassword = "mySecretPassword123!";

            // When
            String encodedPassword = passwordEncoder.encode(rawPassword);

            // Then
            assertThat(encodedPassword).isNotNull();
            assertThat(encodedPassword).isNotEqualTo(rawPassword);
            assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
            assertThat(passwordEncoder.matches("wrongPassword", encodedPassword)).isFalse();
        }

        @Test
        @DisplayName("Debería generar hashes diferentes para la misma contraseña (salt aleatorio)")
        void shouldGenerateDifferentHashesForSamePassword() {
            // Given
            SecurityConfig securityConfig = createSecurityConfig();
            PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
            String rawPassword = "samePassword";

            // When
            String hash1 = passwordEncoder.encode(rawPassword);
            String hash2 = passwordEncoder.encode(rawPassword);

            // Then
            assertThat(hash1).isNotEqualTo(hash2);
            assertThat(passwordEncoder.matches(rawPassword, hash1)).isTrue();
            assertThat(passwordEncoder.matches(rawPassword, hash2)).isTrue();
        }

        @Test
        @DisplayName("Debería devolver false para contraseña vacía")
        void shouldReturnFalseForEmptyPassword() {
            // Given
            SecurityConfig securityConfig = createSecurityConfig();
            PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
            String encoded = passwordEncoder.encode("validPassword");

            // When & Then
            assertThat(passwordEncoder.matches("", encoded)).isFalse();
        }

        @Test
        @DisplayName("Debería lanzar excepción si la contraseña raw es null")
        void shouldThrowExceptionWhenRawPasswordIsNull() {
            // Given
            SecurityConfig securityConfig = createSecurityConfig();
            PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
            String encoded = passwordEncoder.encode("validPassword");

            // When & Then
            assertThatThrownBy(() -> passwordEncoder.matches(null, encoded))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("rawPassword cannot be null");
        }
    }

    // ========================================================================
    // Tests de configuración CORS
    // ========================================================================

    @Nested
    @DisplayName("Configuración CORS")
    class CorsConfigurationTests {

        @Test
        @DisplayName("Debería crear la fuente de configuración CORS")
        void shouldCreateCorsConfigurationSource() {
            // Given
            SecurityConfig securityConfig = createSecurityConfig();

            // When
            var corsSource = securityConfig.corsConfigurationSource();

            // Then
            assertThat(corsSource).isNotNull();
            assertThat(corsSource).isInstanceOf(UrlBasedCorsConfigurationSource.class);
        }

        @Test
        @DisplayName("Debería permitir el origen http://localhost:4200")
        void shouldAllowLocalhost4200Origin() {
            // Given
            SecurityConfig securityConfig = createSecurityConfig();

            // When
            UrlBasedCorsConfigurationSource corsSource =
                    (UrlBasedCorsConfigurationSource) securityConfig.corsConfigurationSource();
            CorsConfiguration corsConfig = corsSource.getCorsConfigurations().get("/**");

            // Then
            assertThat(corsConfig).isNotNull();
            assertThat(corsConfig.getAllowedOrigins()).containsExactly("http://localhost:4200");
        }

        @Test
        @DisplayName("Debería permitir los métodos HTTP especificados")
        void shouldAllowSpecifiedHttpMethods() {
            // Given
            SecurityConfig securityConfig = createSecurityConfig();

            // When
            UrlBasedCorsConfigurationSource corsSource =
                    (UrlBasedCorsConfigurationSource) securityConfig.corsConfigurationSource();
            CorsConfiguration corsConfig = corsSource.getCorsConfigurations().get("/**");

            // Then
            assertThat(corsConfig).isNotNull();
            assertThat(corsConfig.getAllowedMethods()).containsExactlyInAnyOrder(
                    "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
            );
        }

        @Test
        @DisplayName("Debería permitir todas las cabeceras")
        void shouldAllowAllHeaders() {
            // Given
            SecurityConfig securityConfig = createSecurityConfig();

            // When
            UrlBasedCorsConfigurationSource corsSource =
                    (UrlBasedCorsConfigurationSource) securityConfig.corsConfigurationSource();
            CorsConfiguration corsConfig = corsSource.getCorsConfigurations().get("/**");

            // Then
            assertThat(corsConfig).isNotNull();
            assertThat(corsConfig.getAllowedHeaders()).containsExactly("*");
        }

        @Test
        @DisplayName("Debería permitir credenciales")
        void shouldAllowCredentials() {
            // Given
            SecurityConfig securityConfig = createSecurityConfig();

            // When
            UrlBasedCorsConfigurationSource corsSource =
                    (UrlBasedCorsConfigurationSource) securityConfig.corsConfigurationSource();
            CorsConfiguration corsConfig = corsSource.getCorsConfigurations().get("/**");

            // Then
            assertThat(corsConfig).isNotNull();
            assertThat(corsConfig.getAllowCredentials()).isTrue();
        }

        @Test
        @DisplayName("Debería aplicar la configuración CORS a todas las rutas")
        void shouldApplyCorsConfigToAllRoutes() {
            // Given
            SecurityConfig securityConfig = createSecurityConfig();

            // When
            UrlBasedCorsConfigurationSource corsSource =
                    (UrlBasedCorsConfigurationSource) securityConfig.corsConfigurationSource();

            // Then
            assertThat(corsSource.getCorsConfigurations()).containsKey("/**");
            assertThat(corsSource.getCorsConfigurations()).hasSize(1);
        }

        @Test
        @DisplayName("NO debería permitir orígenes no configurados")
        void shouldNotAllowUnconfiguredOrigins() {
            // Given
            SecurityConfig securityConfig = createSecurityConfig();

            // When
            UrlBasedCorsConfigurationSource corsSource =
                    (UrlBasedCorsConfigurationSource) securityConfig.corsConfigurationSource();
            CorsConfiguration corsConfig = corsSource.getCorsConfigurations().get("/**");

            // Then
            List<String> allowedOrigins = corsConfig.getAllowedOrigins();
            assertThat(allowedOrigins).doesNotContain("http://evil-site.com");
            assertThat(allowedOrigins).doesNotContain("*");
        }
    }
}
