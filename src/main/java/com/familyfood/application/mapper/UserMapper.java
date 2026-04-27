package com.familyfood.application.mapper;

import com.familyfood.domain.model.Preferences;
import com.familyfood.domain.model.User;
import com.familyfood.infrastructure.adapter.persistence.entities.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

@Mapper(componentModel = ComponentModel.SPRING)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "miembrosFamilia", ignore = true)
    @Mapping(target = "nivelCocina", ignore = true)
    @Mapping(target = "restriccionesAlimentarias", ignore = true)
    UserEntity toEntity(User user);

    @Mapping(target = "preferencias", ignore = true)
    @Mapping(target = "password", ignore = true)
    User toDomainBasic(UserEntity entity);

    default User toDomain(UserEntity entity) {
        if (entity == null) return null;

        Preferences prefs = null;
        if (entity.getMiembrosFamilia() != null || entity.getNivelCocina() != null
                || entity.getRestriccionesAlimentarias() != null) {
            prefs = Preferences.builder()
                    .miembrosFamilia(entity.getMiembrosFamilia())
                    .nivelCocina(entity.getNivelCocina())
                    .restriccionesAlimentarias(entity.getRestriccionesAlimentarias())
                    .build();
        }

        return User.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .nombre(entity.getNombre())
                .fechaCreacion(entity.getFechaCreacion())
                .fechaActualizacion(entity.getFechaActualizacion())
                .preferencias(prefs)
                .role(entity.getRole())
                .build();
    }
}