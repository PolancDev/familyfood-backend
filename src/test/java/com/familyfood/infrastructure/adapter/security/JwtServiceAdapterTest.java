package com.familyfood.infrastructure.adapter.security;

import com.familyfood.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtServiceAdapter Tests")
class JwtServiceAdapterTest {

    @InjectMocks
    private JwtServiceAdapter jwtServiceAdapter;

    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtServiceAdapter, "jwtSecret",
                "familyfood-secret-key-must-be-at-least-256-bits-long-for-hs256");
        ReflectionTestUtils.setField(jwtServiceAdapter, "jwtExpiration", 86400000L);

        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .password("password123")
                .nombre("Test User")
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Generate Token Tests")
    class GenerateTokenTests {

        @Test
        @DisplayName("Should generate JWT token successfully")
        void shouldGenerateJwtTokenSuccessfully() {
            // When
            String token = jwtServiceAdapter.generateToken(testUser);

            // Then
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
        }

        @Test
        @DisplayName("Should generate unique tokens for different users")
        void shouldGenerateUniqueTokensForDifferentUsers() {
            // Given
            User anotherUser = User.builder()
                    .id(UUID.randomUUID())
                    .email("another@example.com")
                    .password("password456")
                    .nombre("Another User")
                    .fechaCreacion(LocalDateTime.now())
                    .build();

            // When
            String token1 = jwtServiceAdapter.generateToken(testUser);
            String token2 = jwtServiceAdapter.generateToken(anotherUser);

            // Then
            assertThat(token1).isNotEqualTo(token2);
        }
    }

    @Nested
    @DisplayName("Extract Email Tests")
    class ExtractEmailTests {

        @Test
        @DisplayName("Should extract email from token")
        void shouldExtractEmailFromToken() {
            // Given
            String token = jwtServiceAdapter.generateToken(testUser);

            // When
            String email = jwtServiceAdapter.extractEmail(token);

            // Then
            assertThat(email).isEqualTo(testUser.getEmail());
        }
    }

    @Nested
    @DisplayName("Validate Token Tests")
    class ValidateTokenTests {

        @Test
        @DisplayName("Should validate token for correct email")
        void shouldValidateTokenForCorrectEmail() {
            // Given
            String token = jwtServiceAdapter.generateToken(testUser);

            // When
            boolean isValid = jwtServiceAdapter.validateToken(token, testUser.getEmail());

            // Then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("Should not validate token for incorrect email")
        void shouldNotValidateTokenForIncorrectEmail() {
            // Given
            String token = jwtServiceAdapter.generateToken(testUser);

            // When
            boolean isValid = jwtServiceAdapter.validateToken(token, "wrong@example.com");

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should not validate invalid token")
        void shouldNotValidateInvalidToken() {
            // Given
            String invalidToken = "invalid.token.here";

            // When
            boolean isValid = jwtServiceAdapter.validateToken(invalidToken, testUser.getEmail());

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should not validate token with wrong format")
        void shouldNotValidateTokenWithWrongFormat() {
            // Given
            String malformedToken = "not.a.jwt.token";

            // When
            boolean isValid = jwtServiceAdapter.validateToken(malformedToken, testUser.getEmail());

            // Then
            assertThat(isValid).isFalse();
        }
    }
}