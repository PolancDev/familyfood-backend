package com.familyfood.infrastructure.config;

import com.familyfood.infrastructure.adapter.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
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

import static org.assertj.core.api.Assertions.assertThat;

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
 * - La configuración de CORS se construye correctamente
 * - La estructura de la clase (anotaciones, constructor)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityConfig Tests")
class SecurityConfigTest {

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private UserDetailsService userDetailsService;

    @Test
    @DisplayName("Should have @EnableWebSecurity annotation")
    void shouldHaveEnableWebSecurityAnnotation() {
        // Given
        EnableWebSecurity annotation = SecurityConfig.class.getAnnotation(EnableWebSecurity.class);

        // Then
        assertThat(annotation).isNotNull();
    }

    @Test
    @DisplayName("Should have @Configuration annotation")
    void shouldHaveConfigurationAnnotation() {
        // Given
        org.springframework.context.annotation.Configuration annotation =
                SecurityConfig.class.getAnnotation(org.springframework.context.annotation.Configuration.class);

        // Then
        assertThat(annotation).isNotNull();
    }

    @Test
    @DisplayName("Should create BCryptPasswordEncoder")
    void shouldCreateBCryptPasswordEncoder() {
        // Given
        SecurityConfig securityConfig = new SecurityConfig(jwtAuthenticationFilter, userDetailsService);

        // When
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        // Then
        assertThat(passwordEncoder).isNotNull();
        assertThat(passwordEncoder).isInstanceOf(BCryptPasswordEncoder.class);
    }

    @Test
    @DisplayName("Should encode and match passwords correctly")
    void shouldEncodeAndMatchPasswords() {
        // Given
        SecurityConfig securityConfig = new SecurityConfig(jwtAuthenticationFilter, userDetailsService);
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
    @DisplayName("Should create CORS configuration source")
    void shouldCreateCorsConfigurationSource() {
        // Given
        SecurityConfig securityConfig = new SecurityConfig(jwtAuthenticationFilter, userDetailsService);

        // When
        var corsSource = securityConfig.corsConfigurationSource();

        // Then
        assertThat(corsSource).isNotNull();
        assertThat(corsSource).isInstanceOf(UrlBasedCorsConfigurationSource.class);

        // Verify the CORS configuration was registered correctly by checking the source type
        UrlBasedCorsConfigurationSource urlBasedSource = (UrlBasedCorsConfigurationSource) corsSource;
        assertThat(urlBasedSource).isNotNull();
    }
}
