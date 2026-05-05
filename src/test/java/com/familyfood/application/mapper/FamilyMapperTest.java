package com.familyfood.application.mapper;

import com.familyfood.application.dto.family.FamilyMemberResponse;
import com.familyfood.application.dto.family.FamilyResponse;
import com.familyfood.application.dto.family.JoinRequestResponse;
import com.familyfood.domain.enums.FamilyRole;
import com.familyfood.domain.enums.JoinRequestStatus;
import com.familyfood.domain.model.FamilyGroup;
import com.familyfood.domain.model.FamilyMember;
import com.familyfood.domain.model.JoinRequest;
import com.familyfood.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FamilyMapper Tests")
class FamilyMapperTest {

    private FamilyMapper familyMapper;

    private UUID familyId;
    private UUID userId;
    private UUID memberId;
    private UUID requestId;
    private LocalDateTime now;

    private FamilyGroup testFamilyGroup;
    private FamilyMember testMember;
    private JoinRequest testJoinRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        familyMapper = new FamilyMapperImpl();

        familyId = UUID.randomUUID();
        userId = UUID.randomUUID();
        memberId = UUID.randomUUID();
        requestId = UUID.randomUUID();
        now = LocalDateTime.now();

        testFamilyGroup = FamilyGroup.builder()
                .id(familyId)
                .name("Familia García")
                .createdBy(userId)
                .createdAt(now)
                .build();

        testMember = FamilyMember.builder()
                .id(memberId)
                .userId(userId)
                .familyGroupId(familyId)
                .role(FamilyRole.ADMIN)
                .joinedAt(now)
                .build();

        testJoinRequest = JoinRequest.builder()
                .id(requestId)
                .userId(userId)
                .familyGroupId(familyId)
                .status(JoinRequestStatus.PENDING)
                .createdAt(now)
                .build();

        testUser = User.builder()
                .id(userId)
                .nombre("Juan García")
                .email("juan@example.com")
                .build();
    }

    @Nested
    @DisplayName("toFamilyResponse(FamilyGroup)")
    class ToFamilyResponse {

        @Test
        @DisplayName("should map all fields from FamilyGroup to FamilyResponse")
        void shouldMapAllFields() {
            // Given
            FamilyGroup group = testFamilyGroup;

            // When
            FamilyResponse response = familyMapper.toFamilyResponse(group);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(familyId);
            assertThat(response.name()).isEqualTo("Familia García");
            assertThat(response.createdBy()).isEqualTo(userId);
            assertThat(response.createdAt()).isEqualTo(now);
            assertThat(response.memberCount()).isEqualTo(0L); // ignored, default long
        }
    }

    @Nested
    @DisplayName("toFamilyMemberResponse(FamilyMember, User)")
    class ToFamilyMemberResponse {

        @Test
        @DisplayName("should map FamilyMember and User to FamilyMemberResponse")
        void shouldMapAllFields() {
            // Given
            FamilyMember member = testMember;
            User user = testUser;

            // When
            FamilyMemberResponse response = familyMapper.toFamilyMemberResponse(member, user);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(memberId);
            assertThat(response.userId()).isEqualTo(userId);
            assertThat(response.role()).isEqualTo(FamilyRole.ADMIN);
            assertThat(response.joinedAt()).isEqualTo(now);
            assertThat(response.userName()).isEqualTo("Juan García");
            assertThat(response.userEmail()).isEqualTo("juan@example.com");
        }

        @Test
        @DisplayName("should map with null user fields")
        void shouldMapWithNullUser() {
            // Given
            FamilyMember member = testMember;
            User userWithNulls = User.builder()
                    .id(userId)
                    .nombre(null)
                    .email(null)
                    .build();

            // When
            FamilyMemberResponse response = familyMapper.toFamilyMemberResponse(member, userWithNulls);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(memberId);
            assertThat(response.userName()).isNull();
            assertThat(response.userEmail()).isNull();
        }
    }

    @Nested
    @DisplayName("toJoinRequestResponse(JoinRequest, User, FamilyGroup)")
    class ToJoinRequestResponse {

        @Test
        @DisplayName("should map JoinRequest, User and FamilyGroup to JoinRequestResponse")
        void shouldMapAllFields() {
            // Given
            JoinRequest joinRequest = testJoinRequest;
            User user = testUser;
            FamilyGroup familyGroup = testFamilyGroup;

            // When
            JoinRequestResponse response = familyMapper.toJoinRequestResponse(joinRequest, user, familyGroup);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(requestId);
            assertThat(response.userId()).isEqualTo(userId);
            assertThat(response.status()).isEqualTo(JoinRequestStatus.PENDING);
            assertThat(response.createdAt()).isEqualTo(now);
            assertThat(response.familyGroupId()).isEqualTo(familyId);
            assertThat(response.userName()).isEqualTo("Juan García");
            assertThat(response.userEmail()).isEqualTo("juan@example.com");
            assertThat(response.familyGroupName()).isEqualTo("Familia García");
        }

        @Test
        @DisplayName("should map with null user and group fields")
        void shouldMapWithNullFields() {
            // Given
            JoinRequest joinRequest = testJoinRequest;
            User userWithNulls = User.builder()
                    .id(userId)
                    .nombre(null)
                    .email(null)
                    .build();
            FamilyGroup groupWithNullName = FamilyGroup.builder()
                    .id(familyId)
                    .name(null)
                    .createdBy(userId)
                    .createdAt(now)
                    .build();

            // When
            JoinRequestResponse response = familyMapper.toJoinRequestResponse(
                    joinRequest, userWithNulls, groupWithNullName);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(requestId);
            assertThat(response.userName()).isNull();
            assertThat(response.userEmail()).isNull();
            assertThat(response.familyGroupName()).isNull();
        }
    }

}
