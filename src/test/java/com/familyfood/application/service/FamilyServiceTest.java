package com.familyfood.application.service;

import com.familyfood.application.dto.family.FamilyMemberResponse;
import com.familyfood.application.dto.family.FamilyResponse;
import com.familyfood.application.dto.family.FamilySearchResponse;
import com.familyfood.application.dto.family.JoinRequestResponse;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de FamilyService")
class FamilyServiceTest {

    @Mock
    private FamilyGroupRepository familyGroupRepository;

    @Mock
    private FamilyMemberRepository familyMemberRepository;

    @Mock
    private JoinRequestRepository joinRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FamilyMapper familyMapper;

    private FamilyService familyService;

    private UUID userId;
    private UUID familyId;
    private UUID requestId;
    private UUID adminUserId;
    private User testUser;
    private User adminUser;
    private FamilyGroup testFamilyGroup;
    private FamilyMember adminMember;
    private FamilyMember consumerMember;
    private JoinRequest pendingJoinRequest;
    private FamilyResponse testFamilyResponse;
    private FamilyMemberResponse testMemberResponse;
    private JoinRequestResponse testJoinRequestResponse;

    @BeforeEach
    void setUp() {
        familyService = new FamilyService(
                familyGroupRepository, familyMemberRepository,
                joinRequestRepository, userRepository, familyMapper
        );

        userId = UUID.randomUUID();
        familyId = UUID.randomUUID();
        requestId = UUID.randomUUID();
        adminUserId = UUID.randomUUID();

        testUser = User.builder()
                .id(userId)
                .email("user@example.com")
                .nombre("Test User")
                .password("encoded")
                .fechaCreacion(LocalDateTime.now())
                .role(Role.INVITADO)
                .build();

        adminUser = User.builder()
                .id(adminUserId)
                .email("admin@example.com")
                .nombre("Admin User")
                .password("encoded")
                .fechaCreacion(LocalDateTime.now())
                .role(Role.ADMIN)
                .build();

        testFamilyGroup = FamilyGroup.builder()
                .id(familyId)
                .name("Familia Test")
                .createdBy(adminUserId)
                .createdAt(LocalDateTime.now())
                .build();

        adminMember = FamilyMember.builder()
                .id(UUID.randomUUID())
                .userId(adminUserId)
                .familyGroupId(familyId)
                .role(FamilyRole.ADMIN)
                .joinedAt(LocalDateTime.now())
                .build();

        consumerMember = FamilyMember.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .familyGroupId(familyId)
                .role(FamilyRole.CONSUMER)
                .joinedAt(LocalDateTime.now())
                .build();

        pendingJoinRequest = JoinRequest.builder()
                .id(requestId)
                .userId(userId)
                .familyGroupId(familyId)
                .status(JoinRequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        testFamilyResponse = new FamilyResponse(
                familyId, "Familia Test", adminUserId,
                LocalDateTime.now(), 1L
        );

        testMemberResponse = new FamilyMemberResponse(
                consumerMember.getId(), userId, "Test User",
                "user@example.com", FamilyRole.CONSUMER, LocalDateTime.now()
        );

        testJoinRequestResponse = new JoinRequestResponse(
                requestId, userId, "Test User", "user@example.com",
                familyId, "Familia Test", JoinRequestStatus.PENDING, LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("Crear familia")
    class CreateFamilyTests {

        @Test
        @DisplayName("Debería crear familia correctamente")
        void shouldCreateFamilySuccessfully() {
            // Given
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(familyGroupRepository.save(any(FamilyGroup.class))).thenReturn(testFamilyGroup);
            when(familyMemberRepository.save(any(FamilyMember.class))).thenReturn(adminMember);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(familyMapper.toFamilyResponse(any(FamilyGroup.class))).thenReturn(testFamilyResponse);

            // When
            FamilyResponse response = familyService.createFamily(userId, "Familia Test");

            // Then
            assertThat(response).isNotNull();
            assertThat(response.name()).isEqualTo("Familia Test");
            assertThat(response.memberCount()).isEqualTo(1L);

            verify(userRepository).findById(userId);
            verify(familyGroupRepository).save(any(FamilyGroup.class));
            verify(familyMemberRepository).save(any(FamilyMember.class));
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando el usuario no se encuentra")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> familyService.createFamily(userId, "Familia Test"))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("Usuario no encontrado");

            verify(userRepository).findById(userId);
            verify(familyGroupRepository, never()).save(any(FamilyGroup.class));
            verify(familyMemberRepository, never()).save(any(FamilyMember.class));
        }
    }

    @Nested
    @DisplayName("Unirse a familia")
    class JoinFamilyTests {

        @Test
        @DisplayName("Debería crear solicitud de unión correctamente")
        void shouldCreateJoinRequestSuccessfully() {
            // Given
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(familyGroupRepository.findById(familyId)).thenReturn(Optional.of(testFamilyGroup));
            when(familyMemberRepository.existsByUserIdAndFamilyGroupId(userId, familyId)).thenReturn(false);
            when(joinRequestRepository.existsByUserIdAndFamilyGroupIdAndStatus(userId, familyId, JoinRequestStatus.PENDING))
                    .thenReturn(false);
            when(joinRequestRepository.save(any(JoinRequest.class))).thenReturn(pendingJoinRequest);

            // When
            familyService.joinFamily(userId, familyId);

            // Then
            verify(userRepository).findById(userId);
            verify(familyGroupRepository).findById(familyId);
            verify(familyMemberRepository).existsByUserIdAndFamilyGroupId(userId, familyId);
            verify(joinRequestRepository).existsByUserIdAndFamilyGroupIdAndStatus(userId, familyId, JoinRequestStatus.PENDING);
            verify(joinRequestRepository).save(any(JoinRequest.class));
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando el usuario no se encuentra")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> familyService.joinFamily(userId, familyId))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("Usuario no encontrado");

            verify(familyGroupRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando el grupo familiar no se encuentra")
        void shouldThrowExceptionWhenFamilyGroupNotFound() {
            // Given
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(familyGroupRepository.findById(familyId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> familyService.joinFamily(userId, familyId))
                    .isInstanceOf(FamilyGroupNotFoundException.class)
                    .hasMessageContaining("Grupo familiar no encontrado");
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando el usuario ya es miembro")
        void shouldThrowExceptionWhenAlreadyMember() {
            // Given
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(familyGroupRepository.findById(familyId)).thenReturn(Optional.of(testFamilyGroup));
            when(familyMemberRepository.existsByUserIdAndFamilyGroupId(userId, familyId)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> familyService.joinFamily(userId, familyId))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Ya eres miembro de este grupo familiar");

            verify(joinRequestRepository, never()).save(any(JoinRequest.class));
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando ya existe una solicitud pendiente")
        void shouldThrowExceptionWhenPendingRequestExists() {
            // Given
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(familyGroupRepository.findById(familyId)).thenReturn(Optional.of(testFamilyGroup));
            when(familyMemberRepository.existsByUserIdAndFamilyGroupId(userId, familyId)).thenReturn(false);
            when(joinRequestRepository.existsByUserIdAndFamilyGroupIdAndStatus(userId, familyId, JoinRequestStatus.PENDING))
                    .thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> familyService.joinFamily(userId, familyId))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Ya tienes una solicitud pendiente para este grupo");

            verify(joinRequestRepository, never()).save(any(JoinRequest.class));
        }
    }

    @Nested
    @DisplayName("Aprobar solicitud")
    class ApproveJoinRequestTests {

        @Test
        @DisplayName("Debería aprobar solicitud de unión correctamente")
        void shouldApproveJoinRequestSuccessfully() {
            // Given
            when(joinRequestRepository.findById(requestId)).thenReturn(Optional.of(pendingJoinRequest));
            when(familyGroupRepository.findById(familyId)).thenReturn(Optional.of(testFamilyGroup));
            when(familyMemberRepository.findByUserIdAndFamilyGroupId(adminUserId, familyId))
                    .thenReturn(Optional.of(adminMember));
            when(joinRequestRepository.save(any(JoinRequest.class))).thenReturn(pendingJoinRequest);
            when(familyMemberRepository.save(any(FamilyMember.class))).thenReturn(consumerMember);
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            familyService.approveJoinRequest(requestId, adminUserId);

            // Then
            verify(joinRequestRepository).findById(requestId);
            verify(familyGroupRepository).findById(familyId);
            verify(familyMemberRepository).findByUserIdAndFamilyGroupId(adminUserId, familyId);
            verify(joinRequestRepository).save(any(JoinRequest.class));
            verify(familyMemberRepository).save(any(FamilyMember.class));
            verify(userRepository).findById(userId);
            verify(userRepository).save(any(User.class));

            assertThat(pendingJoinRequest.getStatus()).isEqualTo(JoinRequestStatus.APPROVED);
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando la solicitud no se encuentra")
        void shouldThrowExceptionWhenJoinRequestNotFound() {
            // Given
            when(joinRequestRepository.findById(requestId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> familyService.approveJoinRequest(requestId, adminUserId))
                    .isInstanceOf(JoinRequestNotFoundException.class)
                    .hasMessageContaining("Solicitud no encontrada");
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando el admin no es miembro")
        void shouldThrowExceptionWhenAdminNotMember() {
            // Given
            when(joinRequestRepository.findById(requestId)).thenReturn(Optional.of(pendingJoinRequest));
            when(familyGroupRepository.findById(familyId)).thenReturn(Optional.of(testFamilyGroup));
            when(familyMemberRepository.findByUserIdAndFamilyGroupId(adminUserId, familyId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> familyService.approveJoinRequest(requestId, adminUserId))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("No eres miembro de este grupo familiar");
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando el admin no tiene rol ADMIN")
        void shouldThrowExceptionWhenAdminNotAdminRole() {
            // Given
            FamilyMember consumerAsMember = FamilyMember.builder()
                    .id(UUID.randomUUID())
                    .userId(adminUserId)
                    .familyGroupId(familyId)
                    .role(FamilyRole.CONSUMER)
                    .joinedAt(LocalDateTime.now())
                    .build();

            when(joinRequestRepository.findById(requestId)).thenReturn(Optional.of(pendingJoinRequest));
            when(familyGroupRepository.findById(familyId)).thenReturn(Optional.of(testFamilyGroup));
            when(familyMemberRepository.findByUserIdAndFamilyGroupId(adminUserId, familyId))
                    .thenReturn(Optional.of(consumerAsMember));

            // When & Then
            assertThatThrownBy(() -> familyService.approveJoinRequest(requestId, adminUserId))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("No tienes permisos de administrador en este grupo");
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando la solicitud ya ha sido procesada")
        void shouldThrowExceptionWhenRequestAlreadyProcessed() {
            // Given
            JoinRequest approvedRequest = JoinRequest.builder()
                    .id(requestId)
                    .userId(userId)
                    .familyGroupId(familyId)
                    .status(JoinRequestStatus.APPROVED)
                    .createdAt(LocalDateTime.now())
                    .build();

            when(joinRequestRepository.findById(requestId)).thenReturn(Optional.of(approvedRequest));
            when(familyGroupRepository.findById(familyId)).thenReturn(Optional.of(testFamilyGroup));
            when(familyMemberRepository.findByUserIdAndFamilyGroupId(adminUserId, familyId))
                    .thenReturn(Optional.of(adminMember));

            // When & Then
            assertThatThrownBy(() -> familyService.approveJoinRequest(requestId, adminUserId))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("La solicitud ya ha sido procesada");
        }
    }

    @Nested
    @DisplayName("Rechazar solicitud")
    class RejectJoinRequestTests {

        @Test
        @DisplayName("Debería rechazar solicitud de unión correctamente")
        void shouldRejectJoinRequestSuccessfully() {
            // Given
            when(joinRequestRepository.findById(requestId)).thenReturn(Optional.of(pendingJoinRequest));
            when(familyGroupRepository.findById(familyId)).thenReturn(Optional.of(testFamilyGroup));
            when(familyMemberRepository.findByUserIdAndFamilyGroupId(adminUserId, familyId))
                    .thenReturn(Optional.of(adminMember));
            when(joinRequestRepository.save(any(JoinRequest.class))).thenReturn(pendingJoinRequest);

            // When
            familyService.rejectJoinRequest(requestId, adminUserId);

            // Then
            verify(joinRequestRepository).findById(requestId);
            verify(familyGroupRepository).findById(familyId);
            verify(familyMemberRepository).findByUserIdAndFamilyGroupId(adminUserId, familyId);
            verify(joinRequestRepository).save(any(JoinRequest.class));

            assertThat(pendingJoinRequest.getStatus()).isEqualTo(JoinRequestStatus.REJECTED);
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando la solicitud no se encuentra")
        void shouldThrowExceptionWhenJoinRequestNotFound() {
            // Given
            when(joinRequestRepository.findById(requestId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> familyService.rejectJoinRequest(requestId, adminUserId))
                    .isInstanceOf(JoinRequestNotFoundException.class)
                    .hasMessageContaining("Solicitud no encontrada");
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando la solicitud ya ha sido procesada")
        void shouldThrowExceptionWhenRequestAlreadyProcessed() {
            // Given
            JoinRequest rejectedRequest = JoinRequest.builder()
                    .id(requestId)
                    .userId(userId)
                    .familyGroupId(familyId)
                    .status(JoinRequestStatus.REJECTED)
                    .createdAt(LocalDateTime.now())
                    .build();

            when(joinRequestRepository.findById(requestId)).thenReturn(Optional.of(rejectedRequest));
            when(familyGroupRepository.findById(familyId)).thenReturn(Optional.of(testFamilyGroup));
            when(familyMemberRepository.findByUserIdAndFamilyGroupId(adminUserId, familyId))
                    .thenReturn(Optional.of(adminMember));

            // When & Then
            assertThatThrownBy(() -> familyService.rejectJoinRequest(requestId, adminUserId))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("La solicitud ya ha sido procesada");
        }
    }

    @Nested
    @DisplayName("Obtener solicitudes pendientes")
    class GetPendingRequestsTests {

        @Test
        @DisplayName("Debería devolver solicitudes pendientes correctamente")
        void shouldReturnPendingRequestsSuccessfully() {
            // Given
            when(familyGroupRepository.findById(familyId)).thenReturn(Optional.of(testFamilyGroup));
            when(familyMemberRepository.findByUserIdAndFamilyGroupId(adminUserId, familyId))
                    .thenReturn(Optional.of(adminMember));
            when(joinRequestRepository.findByFamilyGroupIdAndStatus(familyId, JoinRequestStatus.PENDING))
                    .thenReturn(List.of(pendingJoinRequest));
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(familyMapper.toJoinRequestResponse(pendingJoinRequest, testUser, testFamilyGroup))
                    .thenReturn(testJoinRequestResponse);

            // When
            List<JoinRequestResponse> responses = familyService.getPendingRequests(familyId, adminUserId);

            // Then
            assertThat(responses).isNotNull();
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).id()).isEqualTo(requestId);
            assertThat(responses.get(0).status()).isEqualTo(JoinRequestStatus.PENDING);

            verify(familyGroupRepository).findById(familyId);
            verify(familyMemberRepository).findByUserIdAndFamilyGroupId(adminUserId, familyId);
            verify(joinRequestRepository).findByFamilyGroupIdAndStatus(familyId, JoinRequestStatus.PENDING);
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando el grupo familiar no se encuentra")
        void shouldThrowExceptionWhenFamilyGroupNotFound() {
            // Given
            when(familyGroupRepository.findById(familyId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> familyService.getPendingRequests(familyId, adminUserId))
                    .isInstanceOf(FamilyGroupNotFoundException.class)
                    .hasMessageContaining("Grupo familiar no encontrado");
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando el usuario no es admin")
        void shouldThrowExceptionWhenUserNotAdmin() {
            // Given
            when(familyGroupRepository.findById(familyId)).thenReturn(Optional.of(testFamilyGroup));
            when(familyMemberRepository.findByUserIdAndFamilyGroupId(adminUserId, familyId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> familyService.getPendingRequests(familyId, adminUserId))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("No eres miembro de este grupo familiar");
        }
    }

    @Nested
    @DisplayName("Obtener miembros")
    class GetMembersTests {

        @Test
        @DisplayName("Debería devolver miembros correctamente")
        void shouldReturnMembersSuccessfully() {
            // Given
            when(familyGroupRepository.findById(familyId)).thenReturn(Optional.of(testFamilyGroup));
            when(familyMemberRepository.findByFamilyGroupId(familyId)).thenReturn(List.of(adminMember, consumerMember));
            when(userRepository.findById(adminUserId)).thenReturn(Optional.of(adminUser));
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(familyMapper.toFamilyMemberResponse(adminMember, adminUser)).thenReturn(
                    new FamilyMemberResponse(adminMember.getId(), adminUserId, "Admin User",
                            "admin@example.com", FamilyRole.ADMIN, LocalDateTime.now())
            );
            when(familyMapper.toFamilyMemberResponse(consumerMember, testUser)).thenReturn(testMemberResponse);

            // When
            List<FamilyMemberResponse> responses = familyService.getMembers(familyId);

            // Then
            assertThat(responses).isNotNull();
            assertThat(responses).hasSize(2);

            verify(familyGroupRepository).findById(familyId);
            verify(familyMemberRepository).findByFamilyGroupId(familyId);
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando el grupo familiar no se encuentra")
        void shouldThrowExceptionWhenFamilyGroupNotFound() {
            // Given
            when(familyGroupRepository.findById(familyId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> familyService.getMembers(familyId))
                    .isInstanceOf(FamilyGroupNotFoundException.class)
                    .hasMessageContaining("Grupo familiar no encontrado");
        }
    }

    @Nested
    @DisplayName("Obtener familias del usuario")
    class GetUserFamiliesTests {

        @Test
        @DisplayName("Debería devolver familias del usuario correctamente")
        void shouldReturnUserFamiliesSuccessfully() {
            // Given
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(familyGroupRepository.findByMemberUserId(userId)).thenReturn(List.of(testFamilyGroup));
            when(familyMemberRepository.countByFamilyGroupId(familyId)).thenReturn(2L);
            when(familyMapper.toFamilyResponse(testFamilyGroup)).thenReturn(testFamilyResponse);

            // When
            List<FamilyResponse> responses = familyService.getUserFamilies(userId);

            // Then
            assertThat(responses).isNotNull();
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).name()).isEqualTo("Familia Test");
            assertThat(responses.get(0).memberCount()).isEqualTo(2L);

            verify(userRepository).findById(userId);
            verify(familyGroupRepository).findByMemberUserId(userId);
            verify(familyMemberRepository).countByFamilyGroupId(familyId);
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando el usuario no se encuentra")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> familyService.getUserFamilies(userId))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("Usuario no encontrado");

            verify(familyGroupRepository, never()).findByMemberUserId(any());
        }
    }

    @Nested
    @DisplayName("Borrado lógico")
    class SoftDeleteTests {

        @Test
        @DisplayName("debería eliminar lógicamente el grupo familiar cuando es admin")
        void shouldSoftDeleteFamilyGroupWhenAdmin() {
            // Given
            when(familyGroupRepository.findById(familyId)).thenReturn(Optional.of(testFamilyGroup));
            when(familyMemberRepository.findByUserIdAndFamilyGroupId(adminUserId, familyId))
                    .thenReturn(Optional.of(adminMember));

            // When
            familyService.deleteFamily(familyId, adminUserId);

            // Then
            assertThat(testFamilyGroup.getDeletedAt()).isNotNull();
            verify(familyGroupRepository).delete(testFamilyGroup);
        }

        @Test
        @DisplayName("debería lanzar excepción cuando un no-admin intenta eliminar")
        void shouldThrowWhenNonAdminTriesToDelete() {
            // Given
            when(familyGroupRepository.findById(familyId)).thenReturn(Optional.of(testFamilyGroup));
            when(familyMemberRepository.findByUserIdAndFamilyGroupId(userId, familyId))
                    .thenReturn(Optional.of(consumerMember));

            // When & Then
            assertThatThrownBy(() -> familyService.deleteFamily(familyId, userId))
                    .isInstanceOf(UnauthorizedException.class);
        }
    }

    @Nested
    @DisplayName("Búsqueda de familias")
    class SearchFamiliesTests {

        @Test
        @DisplayName("debería devolver familias que coincidan")
        void shouldReturnMatchingFamilies() {
            // Given
            String query = "García";
            when(familyGroupRepository.searchByName(query)).thenReturn(List.of(testFamilyGroup));
            when(familyMemberRepository.countByFamilyGroupId(familyId)).thenReturn(3L);

            // When
            List<FamilySearchResponse> results = familyService.searchFamilies(query, userId);

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).name()).isEqualTo("Familia Test");
            assertThat(results.get(0).memberCount()).isEqualTo(3);
            verify(familyGroupRepository).searchByName(query);
        }

        @Test
        @DisplayName("debería devolver lista vacía cuando la consulta está vacía")
        void shouldReturnEmptyListWhenQueryIsEmpty() {
            // When
            List<FamilySearchResponse> results = familyService.searchFamilies("", userId);

            // Then
            assertThat(results).isEmpty();
            verify(familyGroupRepository, never()).searchByName(anyString());
        }
    }

    @Nested
    @DisplayName("Mis solicitudes pendientes")
    class MyPendingRequestsTests {

        @Test
        @DisplayName("debería devolver solicitudes pendientes del usuario")
        void shouldReturnPendingRequestsForUser() {
            // Given
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(joinRequestRepository.findByUserId(userId)).thenReturn(List.of(pendingJoinRequest));
            when(familyGroupRepository.findById(familyId)).thenReturn(Optional.of(testFamilyGroup));
            when(familyMapper.toJoinRequestResponse(pendingJoinRequest, testUser, testFamilyGroup))
                    .thenReturn(testJoinRequestResponse);

            // When
            List<JoinRequestResponse> results = familyService.getMyPendingRequests(userId);

            // Then
            assertThat(results).hasSize(1);
            verify(joinRequestRepository).findByUserId(userId);
        }

        @Test
        @DisplayName("debería devolver lista vacía cuando no hay solicitudes pendientes")
        void shouldReturnEmptyListWhenNoPendingRequests() {
            // Given
            JoinRequest approvedRequest = JoinRequest.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .familyGroupId(familyId)
                    .status(JoinRequestStatus.APPROVED)
                    .createdAt(LocalDateTime.now())
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(joinRequestRepository.findByUserId(userId)).thenReturn(List.of(approvedRequest));

            // When
            List<JoinRequestResponse> results = familyService.getMyPendingRequests(userId);

            // Then
            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("Transferir administración")
    class TransferAdminTests {

        @Test
        @DisplayName("debería transferir rol de admin correctamente")
        void shouldTransferAdminSuccessfully() {
            // Given
            when(familyGroupRepository.findById(familyId)).thenReturn(Optional.of(testFamilyGroup));
            when(familyMemberRepository.findByUserIdAndFamilyGroupId(adminUserId, familyId))
                    .thenReturn(Optional.of(adminMember));
            when(familyMemberRepository.findByUserIdAndFamilyGroupId(userId, familyId))
                    .thenReturn(Optional.of(consumerMember));

            // When
            familyService.transferAdmin(familyId, adminUserId, userId);

            // Then
            assertThat(adminMember.getRole()).isEqualTo(FamilyRole.CONSUMER);
            assertThat(consumerMember.getRole()).isEqualTo(FamilyRole.ADMIN);
            verify(familyMemberRepository, times(2)).save(any(FamilyMember.class));
        }

        @Test
        @DisplayName("debería lanzar excepción cuando el destino no es miembro")
        void shouldThrowWhenTargetIsNotMember() {
            // Given
            UUID nonMemberId = UUID.randomUUID();
            when(familyGroupRepository.findById(familyId)).thenReturn(Optional.of(testFamilyGroup));
            when(familyMemberRepository.findByUserIdAndFamilyGroupId(adminUserId, familyId))
                    .thenReturn(Optional.of(adminMember));
            when(familyMemberRepository.findByUserIdAndFamilyGroupId(nonMemberId, familyId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> familyService.transferAdmin(familyId, adminUserId, nonMemberId))
                    .isInstanceOf(UnauthorizedException.class);
        }

        @Test
        @DisplayName("debería lanzar excepción cuando el destino ya es admin")
        void shouldThrowWhenTargetIsAlreadyAdmin() {
            // Given
            when(familyGroupRepository.findById(familyId)).thenReturn(Optional.of(testFamilyGroup));
            when(familyMemberRepository.findByUserIdAndFamilyGroupId(adminUserId, familyId))
                    .thenReturn(Optional.of(adminMember));
            when(familyMemberRepository.findByUserIdAndFamilyGroupId(adminUserId, familyId))
                    .thenReturn(Optional.of(adminMember));

            // When & Then
            assertThatThrownBy(() -> familyService.transferAdmin(familyId, adminUserId, adminUserId))
                    .isInstanceOf(UnauthorizedException.class);
        }
    }
}
