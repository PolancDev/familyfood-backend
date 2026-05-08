package com.familyfood.application.service;

import com.familyfood.application.dto.family.*;
import com.familyfood.application.mapper.FamilyMapper;
import com.familyfood.application.port.repository.FamilyGroupRepository;
import com.familyfood.application.port.repository.FamilyMemberRepository;
import com.familyfood.application.port.repository.JoinRequestRepository;
import com.familyfood.application.port.repository.UserRepository;
import com.familyfood.domain.enums.FamilyRole;
import com.familyfood.domain.enums.JoinRequestStatus;
import com.familyfood.domain.exception.*;
import com.familyfood.domain.model.FamilyGroup;
import com.familyfood.domain.model.FamilyMember;
import com.familyfood.domain.model.JoinRequest;
import com.familyfood.domain.model.Role;
import com.familyfood.domain.model.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class FamilyService {

    private final FamilyGroupRepository familyGroupRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final JoinRequestRepository joinRequestRepository;
    private final UserRepository userRepository;
    private final FamilyMapper familyMapper;

    /**
     * Crea un nuevo grupo familiar y asigna al creador como ADMIN.
     */
    @Transactional
    public FamilyResponse createFamily(UUID userId, String name) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

                // Verificar si la entidad está gestionada
        log.info("Usuario encontrado al crear familia: {}", user);        
        log.info("UsuarioId que me llega {}", userId);
        FamilyGroup familyGroup = FamilyGroup.create(name, userId);
        FamilyGroup savedGroup = familyGroupRepository.save(familyGroup);

        FamilyMember adminMember = FamilyMember.create(userId, savedGroup.getId(), FamilyRole.ADMIN);
        familyMemberRepository.save(adminMember);

        user.setRole(Role.ADMIN);
        log.info("Usuario a actualizar: {}", user);
        userRepository.save(user);

        log.info("Grupo familiar '{}' creado por usuario {}. Rol actualizado a ADMIN", name, userId);

        FamilyResponse response = familyMapper.toFamilyResponse(savedGroup);
        return new FamilyResponse(
                response.id(),
                response.name(),
                response.createdBy(),
                response.createdAt(),
                1L
        );
    }

    /**
     * Solicita unirse a un grupo familiar. Crea una solicitud PENDING.
     */
    @Transactional
    public void joinFamily(UUID userId, UUID familyId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        FamilyGroup familyGroup = familyGroupRepository.findById(familyId)
                .orElseThrow(() -> new FamilyGroupNotFoundException("Grupo familiar no encontrado"));

        if (familyMemberRepository.existsByUserIdAndFamilyGroupId(userId, familyId)) {
            throw new UnauthorizedException("Ya eres miembro de este grupo familiar");
        }

        if (joinRequestRepository.existsByUserIdAndFamilyGroupIdAndStatus(userId, familyId, JoinRequestStatus.PENDING)) {
            throw new UnauthorizedException("Ya tienes una solicitud pendiente para este grupo");
        }

        JoinRequest joinRequest = JoinRequest.create(userId, familyId);
        joinRequestRepository.save(joinRequest);

        log.info("Solicitud de unión creada: usuario {} -> grupo {}", userId, familyId);
    }

    /**
     * Aprueba una solicitud de unión. Solo el ADMIN del grupo puede hacerlo.
     */
    @Transactional
    public void approveJoinRequest(UUID requestId, UUID adminUserId) {
        JoinRequest joinRequest = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new JoinRequestNotFoundException("Solicitud no encontrada"));

        FamilyGroup familyGroup = familyGroupRepository.findById(joinRequest.getFamilyGroupId())
                .orElseThrow(() -> new FamilyGroupNotFoundException("Grupo familiar no encontrado"));

        validateAdmin(adminUserId, familyGroup.getId());

        if (joinRequest.getStatus() != JoinRequestStatus.PENDING) {
            throw new UnauthorizedException("La solicitud ya ha sido procesada");
        }

        joinRequest.approve();
        joinRequestRepository.save(joinRequest);

        FamilyMember newMember = FamilyMember.create(joinRequest.getUserId(), familyGroup.getId(), FamilyRole.CONSUMER);
        familyMemberRepository.save(newMember);

        User requestingUser = userRepository.findById(joinRequest.getUserId())
                .orElseThrow(() -> new UserNotFoundException("Usuario solicitante no encontrado"));
        requestingUser.setRole(Role.CONSUMER);
        userRepository.save(requestingUser);

        log.info("Solicitud {} aprobada. Usuario {} añadido al grupo {}. Rol actualizado a CONSUMER",
                requestId, joinRequest.getUserId(), familyGroup.getId());
    }

    /**
     * Rechaza una solicitud de unión. Solo el ADMIN del grupo puede hacerlo.
     */
    @Transactional
    public void rejectJoinRequest(UUID requestId, UUID adminUserId) {
        JoinRequest joinRequest = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new JoinRequestNotFoundException("Solicitud no encontrada"));

        FamilyGroup familyGroup = familyGroupRepository.findById(joinRequest.getFamilyGroupId())
                .orElseThrow(() -> new FamilyGroupNotFoundException("Grupo familiar no encontrado"));

        validateAdmin(adminUserId, familyGroup.getId());

        if (joinRequest.getStatus() != JoinRequestStatus.PENDING) {
            throw new UnauthorizedException("La solicitud ya ha sido procesada");
        }

        joinRequest.reject();
        joinRequestRepository.save(joinRequest);

        log.info("Solicitud {} rechazada por el administrador {}", requestId, adminUserId);
    }

    /**
     * Obtiene las solicitudes pendientes de un grupo. Solo el ADMIN puede verlas.
     */
    @Transactional(readOnly = true)
    public List<JoinRequestResponse> getPendingRequests(UUID familyId, UUID adminUserId) {
        FamilyGroup familyGroup = familyGroupRepository.findById(familyId)
                .orElseThrow(() -> new FamilyGroupNotFoundException("Grupo familiar no encontrado"));

        validateAdmin(adminUserId, familyGroup.getId());

        List<JoinRequest> pendingRequests = joinRequestRepository.findByFamilyGroupIdAndStatus(familyId, JoinRequestStatus.PENDING);

        return pendingRequests.stream()
                .map(request -> {
                    User requestingUser = userRepository.findById(request.getUserId())
                            .orElse(null);
                    return familyMapper.toJoinRequestResponse(request, requestingUser, familyGroup);
                })
                .collect(Collectors.toList());
    }

    /**
     * Obtiene los miembros de un grupo familiar.
     */
    @Transactional(readOnly = true)
    public List<FamilyMemberResponse> getMembers(UUID familyId) {
        FamilyGroup familyGroup = familyGroupRepository.findById(familyId)
                .orElseThrow(() -> new FamilyGroupNotFoundException("Grupo familiar no encontrado"));

        List<FamilyMember> members = familyMemberRepository.findByFamilyGroupId(familyId);

        return members.stream()
                .map(member -> {
                    User user = userRepository.findById(member.getUserId())
                            .orElse(null);
                    return familyMapper.toFamilyMemberResponse(member, user);
                })
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todas las familias de las que un usuario es miembro.
     */
    @Transactional(readOnly = true)
    public List<FamilyResponse> getUserFamilies(UUID userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        List<FamilyGroup> families = familyGroupRepository.findByMemberUserId(userId);

        return families.stream()
                .map(family -> {
                    long memberCount = familyMemberRepository.countByFamilyGroupId(family.getId());
                    FamilyResponse response = familyMapper.toFamilyResponse(family);
                    return new FamilyResponse(
                            response.id(),
                            response.name(),
                            response.createdBy(),
                            response.createdAt(),
                            memberCount
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Valida que un usuario sea ADMIN del grupo familiar.
     */
    private void validateAdmin(UUID userId, UUID familyGroupId) {
        FamilyMember member = familyMemberRepository.findByUserIdAndFamilyGroupId(userId, familyGroupId)
                .orElseThrow(() -> new UnauthorizedException("No eres miembro de este grupo familiar"));

        if (member.getRole() != FamilyRole.ADMIN) {
            throw new UnauthorizedException("No tienes permisos de administrador en este grupo");
        }
    }
}
