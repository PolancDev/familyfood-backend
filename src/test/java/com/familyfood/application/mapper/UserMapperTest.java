package com.familyfood.application.mapper;

import com.familyfood.domain.model.Preferences;
import com.familyfood.domain.model.Role;
import com.familyfood.domain.model.User;
import com.familyfood.infrastructure.adapter.persistence.entities.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests de UserMapper")
class UserMapperTest {

    private UserMapper userMapper;

    private UUID userId;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapperImpl();

        userId = UUID.randomUUID();
        now = LocalDateTime.now();
    }

    @Nested
    @DisplayName("toEntityForCreate(User)")
    class ToEntityForCreate {

        @Test
        @DisplayName("debería mapear User a UserEntity ignorando id, fechas y campos de preferencias")
        void shouldMapToEntityForCreate() {
            // Given
            User user = User.builder()
                    .id(userId)
                    .email("test@example.com")
                    .password("encodedPassword")
                    .nombre("Test User")
                    .fechaCreacion(now)
                    .fechaActualizacion(now)
                    .role(Role.CONSUMER)
                    .preferencias(Preferences.builder()
                            .miembrosFamilia(4)
                            .nivelCocina("MEDIO")
                            .restriccionesAlimentarias("Sin gluten")
                            .build())
                    .build();

            // When
            UserEntity entity = userMapper.toEntityForCreate(user);

            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isNull(); // ignored
            assertThat(entity.getEmail()).isEqualTo("test@example.com");
            assertThat(entity.getPassword()).isEqualTo("encodedPassword");
            assertThat(entity.getNombre()).isEqualTo("Test User");
            assertThat(entity.getFechaCreacion()).isNull(); // ignored
            assertThat(entity.getFechaActualizacion()).isNull(); // ignored
            assertThat(entity.getRole()).isEqualTo(Role.CONSUMER);
            assertThat(entity.getMiembrosFamilia()).isNull(); // ignored
            assertThat(entity.getNivelCocina()).isNull(); // ignored
            assertThat(entity.getRestriccionesAlimentarias()).isNull(); // ignored
        }
    }

    @Nested
    @DisplayName("toEntityForUpdate(User)")
    class ToEntityForUpdate {

        @Test
        @DisplayName("debería mapear User a UserEntity para actualización preservando el rol")
        void shouldMapToEntityForUpdate() {
            // Given
            User user = User.builder()
                    .id(userId)
                    .email("test@example.com")
                    .password("encodedPassword")
                    .nombre("Updated Name")
                    .fechaCreacion(now)
                    .fechaActualizacion(now)
                    .role(Role.ADMIN)
                    .preferencias(Preferences.builder()
                            .miembrosFamilia(4)
                            .nivelCocina("AVANZADO")
                            .restriccionesAlimentarias("Sin lactosa")
                            .build())
                    .build();

            // When
            UserEntity entity = userMapper.toEntityForUpdate(user);

            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isEqualTo(userId);
            assertThat(entity.getEmail()).isEqualTo("test@example.com");
            assertThat(entity.getPassword()).isEqualTo("encodedPassword");
            assertThat(entity.getNombre()).isEqualTo("Updated Name");
            assertThat(entity.getRole()).isEqualTo(Role.ADMIN);
            assertThat(entity.getMiembrosFamilia()).isNull(); // ignored
            assertThat(entity.getNivelCocina()).isNull(); // ignored
            assertThat(entity.getRestriccionesAlimentarias()).isNull(); // ignored
        }
    }

    @Nested
    @DisplayName("toDomain(UserEntity)")
    class ToDomain {

        @Test
        @DisplayName("debería mapear UserEntity a User con preferencias cuando los campos de preferencias existen")
        void shouldMapToDomainWithPreferences() {
            // Given
            UserEntity entity = UserEntity.builder()
                    .id(userId)
                    .email("test@example.com")
                    .password("encodedPassword")
                    .nombre("Test User")
                    .fechaCreacion(now)
                    .fechaActualizacion(now)
                    .role(Role.CONSUMER)
                    .miembrosFamilia(4)
                    .nivelCocina("MEDIO")
                    .restriccionesAlimentarias("Sin gluten")
                    .build();

            // When
            User user = userMapper.toDomain(entity);

            // Then
            assertThat(user).isNotNull();
            assertThat(user.getId()).isEqualTo(userId);
            assertThat(user.getEmail()).isEqualTo("test@example.com");
            assertThat(user.getPassword()).isEqualTo("encodedPassword");
            assertThat(user.getNombre()).isEqualTo("Test User");
            assertThat(user.getFechaCreacion()).isEqualTo(now);
            assertThat(user.getFechaActualizacion()).isEqualTo(now);
            assertThat(user.getRole()).isEqualTo(Role.CONSUMER);

            assertThat(user.getPreferencias()).isNotNull();
            assertThat(user.getPreferencias().getMiembrosFamilia()).isEqualTo(4);
            assertThat(user.getPreferencias().getNivelCocina()).isEqualTo("MEDIO");
            assertThat(user.getPreferencias().getRestriccionesAlimentarias()).isEqualTo("Sin gluten");
        }

        @Test
        @DisplayName("debería mapear UserEntity a User con preferencias nulas cuando no hay campos de preferencias")
        void shouldMapToDomainWithoutPreferences() {
            // Given
            UserEntity entity = UserEntity.builder()
                    .id(userId)
                    .email("test@example.com")
                    .password("encodedPassword")
                    .nombre("Test User")
                    .fechaCreacion(now)
                    .fechaActualizacion(now)
                    .role(Role.INVITADO)
                    .miembrosFamilia(null)
                    .nivelCocina(null)
                    .restriccionesAlimentarias(null)
                    .build();

            // When
            User user = userMapper.toDomain(entity);

            // Then
            assertThat(user).isNotNull();
            assertThat(user.getId()).isEqualTo(userId);
            assertThat(user.getEmail()).isEqualTo("test@example.com");
            assertThat(user.getPassword()).isEqualTo("encodedPassword");
            assertThat(user.getNombre()).isEqualTo("Test User");
            assertThat(user.getRole()).isEqualTo(Role.INVITADO);
            assertThat(user.getPreferencias()).isNull();
        }

        @Test
        @DisplayName("debería devolver null cuando UserEntity es null")
        void shouldReturnNullWhenEntityIsNull() {
            // Given
            UserEntity entity = null;

            // When
            User user = userMapper.toDomain(entity);

            // Then
            assertThat(user).isNull();
        }
    }

    @Nested
    @DisplayName("toDomainBasic(UserEntity)")
    class ToDomainBasic {

        @Test
        @DisplayName("debería mapear UserEntity a User sin preferencias ni contraseña")
        void shouldMapToDomainBasic() {
            // Given
            UserEntity entity = UserEntity.builder()
                    .id(userId)
                    .email("test@example.com")
                    .password("secretPassword")
                    .nombre("Test User")
                    .fechaCreacion(now)
                    .fechaActualizacion(now)
                    .role(Role.CONSUMER)
                    .miembrosFamilia(4)
                    .nivelCocina("MEDIO")
                    .restriccionesAlimentarias("Sin gluten")
                    .build();

            // When
            User user = userMapper.toDomainBasic(entity);

            // Then
            assertThat(user).isNotNull();
            assertThat(user.getId()).isEqualTo(userId);
            assertThat(user.getEmail()).isEqualTo("test@example.com");
            assertThat(user.getPassword()).isNull(); // ignored
            assertThat(user.getNombre()).isEqualTo("Test User");
            assertThat(user.getFechaCreacion()).isEqualTo(now);
            assertThat(user.getFechaActualizacion()).isEqualTo(now);
            assertThat(user.getRole()).isEqualTo(Role.CONSUMER);
            assertThat(user.getPreferencias()).isNull(); // ignored
        }
    }
}
