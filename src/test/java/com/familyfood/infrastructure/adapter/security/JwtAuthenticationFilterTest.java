package com.familyfood.infrastructure.adapter.security;

import com.familyfood.application.port.repository.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Tests")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String VALID_TOKEN = "valid.jwt.token";
    private static final String USER_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("Authorization Header Tests")
    class AuthorizationHeaderTests {

        @Test
        @DisplayName("Should continue filter chain when Authorization header is absent")
        void shouldContinueChainWhenAuthHeaderAbsent() throws Exception {
            // Given
            when(request.getHeader("Authorization")).thenReturn(null);

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("Should continue filter chain when Authorization header does not start with Bearer")
        void shouldContinueChainWhenAuthHeaderNotBearer() throws Exception {
            // Given
            when(request.getHeader("Authorization")).thenReturn("Basic some-token");

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("Should continue filter chain when Authorization header is empty")
        void shouldContinueChainWhenAuthHeaderIsEmpty() throws Exception {
            // Given
            when(request.getHeader("Authorization")).thenReturn("");

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }

    @Nested
    @DisplayName("Token Validation Tests")
    class TokenValidationTests {

        @Test
        @DisplayName("Should authenticate user when token is valid")
        void shouldAuthenticateUserWhenTokenIsValid() throws Exception {
            // Given
            when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
            when(jwtService.extractEmail(VALID_TOKEN)).thenReturn(USER_EMAIL);

            UserDetails userDetails = CustomUserDetails.builder()
                    .id(UUID.randomUUID())
                    .email(USER_EMAIL)
                    .password("encoded-password")
                    .nombre("Test User")
                    .enabled(true)
                    .role(com.familyfood.domain.model.Role.ADMIN)
                    .build();

            when(userDetailsService.loadUserByUsername(USER_EMAIL)).thenReturn(userDetails);
            when(jwtService.validateToken(VALID_TOKEN, USER_EMAIL)).thenReturn(true);

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication())
                    .isNotNull();
            assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                    .isEqualTo(USER_EMAIL);
            assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                    .extracting("authority")
                    .contains("ROLE_ADMIN");
        }

        @Test
        @DisplayName("Should not authenticate user when token is invalid")
        void shouldNotAuthenticateUserWhenTokenIsInvalid() throws Exception {
            // Given
            when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
            when(jwtService.extractEmail(VALID_TOKEN)).thenReturn(USER_EMAIL);

            UserDetails userDetails = CustomUserDetails.builder()
                    .id(UUID.randomUUID())
                    .email(USER_EMAIL)
                    .password("encoded-password")
                    .nombre("Test User")
                    .enabled(true)
                    .role(com.familyfood.domain.model.Role.ADMIN)
                    .build();

            when(userDetailsService.loadUserByUsername(USER_EMAIL)).thenReturn(userDetails);
            when(jwtService.validateToken(VALID_TOKEN, USER_EMAIL)).thenReturn(false);

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("Should not authenticate user when email extracted is null")
        void shouldNotAuthenticateUserWhenEmailIsNull() throws Exception {
            // Given
            when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
            when(jwtService.extractEmail(VALID_TOKEN)).thenReturn(null);

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(userDetailsService, never()).loadUserByUsername(anyString());
        }

        @Test
        @DisplayName("Should not authenticate user when email does not exist in database")
        void shouldNotAuthenticateUserWhenEmailNotFound() throws Exception {
            // Given
            when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
            when(jwtService.extractEmail(VALID_TOKEN)).thenReturn(USER_EMAIL);
            when(userDetailsService.loadUserByUsername(USER_EMAIL))
                    .thenThrow(new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found"));

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("Should handle JwtService exception gracefully")
        void shouldHandleJwtServiceExceptionGracefully() throws Exception {
            // Given
            when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
            when(jwtService.extractEmail(VALID_TOKEN)).thenThrow(new RuntimeException("JWT parsing error"));

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }
}
