package com.familyfood.infrastructure.adapter.persistence.adapters;

import com.familyfood.application.mapper.UserMapper;
import com.familyfood.domain.model.Preferences;
import com.familyfood.domain.model.User;
import com.familyfood.infrastructure.adapter.persistence.entities.UserEntity;
import com.familyfood.infrastructure.adapter.persistence.repository.SpringDataUserRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserRepositoryAdapter Tests")
class UserRepositoryAdapterTest {

    @Mock
    private SpringDataUserRepository springDataUserRepository;

    @Mock
    private UserMapper userMapper;

    private UserRepositoryAdapter userRepositoryAdapter;

    private User testUser;
    private UserEntity testUserEntity;

    @BeforeEach
    void setUp() {
        userRepositoryAdapter = new UserRepositoryAdapter(springDataUserRepository, userMapper);
        
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .password("encodedPassword")
                .nombre("Test User")
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        testUserEntity = UserEntity.builder()
                .id(testUser.getId())
                .email(testUser.getEmail())
                .password(testUser.getPassword())
                .nombre(testUser.getNombre())
                .fechaCreacion(testUser.getFechaCreacion())
                .fechaActualizacion(testUser.getFechaActualizacion())
                .build();
    }

    @Nested
    @DisplayName("Save Tests")
    class SaveTests {

        @Test
        @DisplayName("Should save user successfully")
        void shouldSaveUserSuccessfully() {
            // Given
            when(userMapper.toEntity(any(User.class))).thenReturn(testUserEntity);
            when(springDataUserRepository.save(any(UserEntity.class))).thenReturn(testUserEntity);
            when(userMapper.toDomain(any(UserEntity.class))).thenReturn(testUser);

            // When
            User result = userRepositoryAdapter.save(testUser);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testUser.getId());
            assertThat(result.getEmail()).isEqualTo(testUser.getEmail());
            verify(userMapper).toEntity(testUser);
            verify(springDataUserRepository).save(testUserEntity);
            verify(userMapper).toDomain(testUserEntity);
        }

        @Test
        @DisplayName("Should save user with preferences")
        void shouldSaveUserWithPreferences() {
            // Given
            Preferences prefs = Preferences.builder()
                    .miembrosFamilia(4)
                    .nivelCocina("MEDIO")
                    .restriccionesAlimentarias("Ninguna")
                    .build();
            testUser.setPreferencias(prefs);

            when(userMapper.toEntity(any(User.class))).thenReturn(testUserEntity);
            when(springDataUserRepository.save(any(UserEntity.class))).thenReturn(testUserEntity);
            when(userMapper.toDomain(any(UserEntity.class))).thenReturn(testUser);

            // When
            User result = userRepositoryAdapter.save(testUser);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getPreferencias()).isNotNull();
            assertThat(result.getPreferencias().getMiembrosFamilia()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("Find By ID Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should find user by ID")
        void shouldFindUserById() {
            // Given
            when(springDataUserRepository.findById(testUser.getId())).thenReturn(Optional.of(testUserEntity));
            when(userMapper.toDomain(any(UserEntity.class))).thenReturn(testUser);

            // When
            Optional<User> result = userRepositoryAdapter.findById(testUser.getId());

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(testUser.getId());
            assertThat(result.get().getEmail()).isEqualTo(testUser.getEmail());
        }

        @Test
        @DisplayName("Should return empty when user not found")
        void shouldReturnEmptyWhenUserNotFound() {
            // Given
            UUID unknownId = UUID.randomUUID();
            when(springDataUserRepository.findById(unknownId)).thenReturn(Optional.empty());

            // When
            Optional<User> result = userRepositoryAdapter.findById(unknownId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Find By Email Tests")
    class FindByEmailTests {

        @Test
        @DisplayName("Should find user by email")
        void shouldFindUserByEmail() {
            // Given
            when(springDataUserRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUserEntity));
            when(userMapper.toDomain(any(UserEntity.class))).thenReturn(testUser);

            // When
            Optional<User> result = userRepositoryAdapter.findByEmail(testUser.getEmail());

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo(testUser.getEmail());
        }

        @Test
        @DisplayName("Should return empty for non-existent email")
        void shouldReturnEmptyForNonExistentEmail() {
            // Given
            String unknownEmail = "unknown@example.com";
            when(springDataUserRepository.findByEmail(unknownEmail)).thenReturn(Optional.empty());

            // When
            Optional<User> result = userRepositoryAdapter.findByEmail(unknownEmail);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Exists By Email Tests")
    class ExistsByEmailTests {

        @Test
        @DisplayName("Should return true when email exists")
        void shouldReturnTrueWhenEmailExists() {
            // Given
            when(springDataUserRepository.existsByEmail(testUser.getEmail())).thenReturn(true);

            // When
            boolean exists = userRepositoryAdapter.existsByEmail(testUser.getEmail());

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should return false when email does not exist")
        void shouldReturnFalseWhenEmailDoesNotExist() {
            // Given
            String unknownEmail = "unknown@example.com";
            when(springDataUserRepository.existsByEmail(unknownEmail)).thenReturn(false);

            // When
            boolean exists = userRepositoryAdapter.existsByEmail(unknownEmail);

            // Then
            assertThat(exists).isFalse();
        }
    }
}