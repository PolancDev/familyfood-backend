package com.familyfood.application.mapper;

import com.familyfood.application.dto.auth.LoginRequest;
import com.familyfood.application.dto.auth.RegisterRequest;
import com.familyfood.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

@Mapper(componentModel = ComponentModel.SPRING)
public interface AuthMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "preferencias", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(source = "encodedPassword", target = "password")
    User toDomainWithPassword(RegisterRequest request, String encodedPassword);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "preferencias", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(source = "encodedPassword", target = "password")
    User toDomainWithPassword(LoginRequest request, String encodedPassword);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "preferencias", ignore = true)
    @Mapping(target = "role", ignore = true)
    User toDomain(RegisterRequest request);
}