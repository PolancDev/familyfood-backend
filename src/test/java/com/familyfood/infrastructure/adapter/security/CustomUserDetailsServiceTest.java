package com.familyfood.infrastructure.adapter.security;

import com.familyfood.application.mapper.UserMapper;
import com.familyfood.domain.model.Role;
import com.familyfood.domain.model.User;
import com.familyfood.infrastructure.adapter.persistence.entities.UserEntity;
import com.familyfood.infrastructure.adapter.persistence.repository.SpringDataUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService Tests")
class CustomUserDetailsServiceTest {

    @Mock
    private SpringDataUserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private static final String USER_EMAIL = "test@example.com";
    private static final UUID USER_ID = UUID.randomUUID();

    private UserEntity createUserEntity(Role role) {
        return UserEntity.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .password("encoded-password")
                .nombre("Test User")
                .role(role)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();
    }

    private User createDomainUser(Role role) {
        return User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .password("encoded-password")
                .nombre("Test User")
                .role(role)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Load User By Username Tests")
    class LoadUserByUsernameTests {

        @Test
        @DisplayName("Should load user successfully when email exists")
        void shouldLoadUserSuccessfullyWhenEmailExists() {
            // Given
            UserEntity userEntity = createUserEntity(Role.ADMIN);
            User domainUser = createDomainUser(Role.ADMIN);

            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(userEntity));
            when(userMapper.toDomain(userEntity)).thenReturn(domainUser);

            // When
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(USER_EMAIL);

            // Then
            assertThat(userDetails).isNotNull();
            assertThat(userDetails.getUsername()).isEqualTo(USER_EMAIL);
            assertThat(userDetails.getPassword()).isEqualTo("encoded-password");
            assertThat(userDetails.getAuthorities())
                    .extracting("authority")
                    .containsExactly("ROLE_ADMIN");
            assertThat(userDetails.isEnabled()).isTrue();

            verify(userRepository).findByEmail(USER_EMAIL);
            verify(userMapper).toDomain(userEntity);
        }

        @Test
        @DisplayName("Should load user with CONSUMER role successfully")
        void shouldLoadUserWithConsumerRole() {
            // Given
            UserEntity userEntity = createUserEntity(Role.CONSUMER);
            User domainUser = createDomainUser(Role.CONSUMER);

            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(userEntity));
            when(userMapper.toDomain(userEntity)).thenReturn(domainUser);

            // When
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(USER_EMAIL);

            // Then
            assertThat(userDetails.getAuthorities())
                    .extracting("authority")
                    .containsExactly("ROLE_CONSUMER");
        }

        @Test
        @DisplayName("Should load user with INVITADO role successfully")
        void shouldLoadUserWithInvitadoRole() {
            // Given
            UserEntity userEntity = createUserEntity(Role.INVITADO);
            User domainUser = createDomainUser(Role.INVITADO);

            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(userEntity));
            when(userMapper.toDomain(userEntity)).thenReturn(domainUser);

            // When
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(USER_EMAIL);

            // Then
            assertThat(userDetails.getAuthorities())
                    .extracting("authority")
                    .containsExactly("ROLE_INVITADO");
        }

        @Test
        @DisplayName("Should throw UsernameNotFoundException when email does not exist")
        void shouldThrowExceptionWhenEmailNotFound() {
            // Given
            String nonExistentEmail = "nonexistent@example.com";
            when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(nonExistentEmail))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining(nonExistentEmail);

            verify(userRepository).findByEmail(nonExistentEmail);
            verify(userMapper, never()).toDomain(any());
        }

        @Test
        @DisplayName("Should call repository with correct email")
        void shouldCallRepositoryWithCorrectEmail() {
            // Given
            UserEntity userEntity = createUserEntity(Role.ADMIN);
            User domainUser = createDomainUser(Role.ADMIN);

            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(userEntity));
            when(userMapper.toDomain(userEntity)).thenReturn(domainUser);

            // When
            customUserDetailsService.loadUserByUsername(USER_EMAIL);

            // Then
            verify(userRepository).findByEmail(USER_EMAIL);
            verifyNoMoreInteractions(userRepository);
        }
    }
}
