package com.familyfood.application.mapper;

import com.familyfood.application.dto.auth.LoginResponse;
import com.familyfood.application.dto.auth.RegisterResponse;
import com.familyfood.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;

@Mapper(componentModel = ComponentModel.SPRING)
public interface AuthResponseMapper {

    LoginResponse toLoginResponse(String token, User user);

    RegisterResponse toRegisterResponse(User user, String token);
}