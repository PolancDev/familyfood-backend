package com.familyfood.application.mapper;

import com.familyfood.application.dto.family.FamilyMemberResponse;
import com.familyfood.application.dto.family.FamilyResponse;
import com.familyfood.application.dto.family.JoinRequestResponse;
import com.familyfood.domain.model.FamilyGroup;
import com.familyfood.domain.model.FamilyMember;
import com.familyfood.domain.model.JoinRequest;
import com.familyfood.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper MapStruct para entidades de grupos familiares.
 */
@Mapper(componentModel = "spring")
public interface FamilyMapper {

    /**
     * Convierte un FamilyGroup a FamilyResponse.
     *
     * @param familyGroup grupo familiar
     * @return respuesta del grupo
     */
    @Mapping(target = "memberCount", ignore = true)
    FamilyResponse toFamilyResponse(FamilyGroup familyGroup);

    /**
     * Convierte un FamilyMember y User a FamilyMemberResponse.
     *
     * @param familyMember miembro del grupo
     * @param user         usuario asociado
     * @return respuesta del miembro
     */
    @Mapping(target = "id", source = "familyMember.id")
    @Mapping(target = "userId", source = "familyMember.userId")
    @Mapping(target = "role", source = "familyMember.role")
    @Mapping(target = "joinedAt", source = "familyMember.joinedAt")
    @Mapping(target = "userName", source = "user.nombre")
    @Mapping(target = "userEmail", source = "user.email")
    FamilyMemberResponse toFamilyMemberResponse(
            FamilyMember familyMember, User user);

    /**
     * Convierte JoinRequest, User y FamilyGroup a JoinRequestResponse.
     *
     * @param joinRequest solicitud de unión
     * @param user        usuario solicitante
     * @param familyGroup grupo familiar
     * @return respuesta de la solicitud
     */
    @Mapping(target = "id", source = "joinRequest.id")
    @Mapping(target = "userId", source = "joinRequest.userId")
    @Mapping(target = "status", source = "joinRequest.status")
    @Mapping(target = "createdAt", source = "joinRequest.createdAt")
    @Mapping(target = "familyGroupId", source = "familyGroup.id")
    @Mapping(target = "userName", source = "user.nombre")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "familyGroupName", source = "familyGroup.name")
    JoinRequestResponse toJoinRequestResponse(
            JoinRequest joinRequest, User user, FamilyGroup familyGroup);
}
