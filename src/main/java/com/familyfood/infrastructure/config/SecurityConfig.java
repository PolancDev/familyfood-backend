package com.familyfood.infrastructure.config;

import com.familyfood.infrastructure.adapter.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        SecurityFilterChain filterChain = http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    // Endpoints públicos
                    .requestMatchers("/api/v1/auth/**").permitAll()
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                    // ===== PLAN SEMANAL =====
                    // CONSUMER: solo lectura
                    .requestMatchers(HttpMethod.GET, "/api/v1/plan-semanal").hasAnyRole("ADMIN", "CONSUMER")
                    // ADMIN: acceso total
                    .requestMatchers(HttpMethod.POST, "/api/v1/plan-semanal").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/plan-semanal/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/v1/plan-semanal/generar").hasRole("ADMIN")

                    // ===== RECETAS =====
                    // CONSUMER: lectura + marcar favorita
                    .requestMatchers(HttpMethod.GET, "/api/v1/recetas").hasAnyRole("ADMIN", "CONSUMER")
                    .requestMatchers(HttpMethod.GET, "/api/v1/recetas/{id}").hasAnyRole("ADMIN", "CONSUMER")
                    .requestMatchers(HttpMethod.POST, "/api/v1/recetas/{id}/favorita").hasAnyRole("ADMIN", "CONSUMER")
                    // ADMIN: acceso total
                    .requestMatchers(HttpMethod.POST, "/api/v1/recetas").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/recetas/{id}").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/recetas/{id}").hasRole("ADMIN")

                    // ===== LISTA COMPRA =====
                    // CONSUMER: lectura, gestión de items (excepto eliminar), exportar
                    .requestMatchers(HttpMethod.GET, "/api/v1/lista-compra").hasAnyRole("ADMIN", "CONSUMER")
                    .requestMatchers(HttpMethod.GET, "/api/v1/lista-compra/items").hasAnyRole("ADMIN", "CONSUMER")
                    .requestMatchers(HttpMethod.POST, "/api/v1/lista-compra/items").hasAnyRole("ADMIN", "CONSUMER")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/lista-compra/items/{id}").hasAnyRole("ADMIN", "CONSUMER")
                    .requestMatchers(HttpMethod.GET, "/api/v1/lista-compra/exportar").hasAnyRole("ADMIN", "CONSUMER")
                    // ADMIN: generar y eliminar
                    .requestMatchers(HttpMethod.POST, "/api/v1/lista-compra/generar").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/lista-compra/items/{id}").hasRole("ADMIN")

                    // ===== USUARIO =====
                    // CONSUMER: leer propios datos, ver miembros
                    .requestMatchers(HttpMethod.GET, "/api/v1/usuario").hasAnyRole("ADMIN", "CONSUMER")
                    .requestMatchers(HttpMethod.GET, "/api/v1/usuario/miembros").hasAnyRole("ADMIN", "CONSUMER")
                    // ADMIN: modificar usuario y crear miembros
                    .requestMatchers(HttpMethod.PUT, "/api/v1/usuario").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/v1/usuario/miembros").hasRole("ADMIN")

                    // ===== PÁNICO =====
                    // CONSUMER y ADMIN: acceso total
                    .requestMatchers(HttpMethod.GET, "/api/v1/panico/**").hasAnyRole("ADMIN", "CONSUMER")

                    // Por defecto: solo ADMIN
                    .requestMatchers("/api/v1/**").hasRole("ADMIN")
                    .anyRequest().authenticated())
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((request, response, authException) -> {
                        response.setStatus(401);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\": \"Unauthorized\"}");
                    }))
            .build();

        return filterChain;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}