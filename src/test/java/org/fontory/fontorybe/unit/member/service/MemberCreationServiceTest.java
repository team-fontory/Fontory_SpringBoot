package org.fontory.fontorybe.unit.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.util.UUID;
import org.fontory.fontorybe.member.controller.port.MemberCreationService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.domain.exception.MemberAlreadyExistException;
import org.fontory.fontorybe.member.infrastructure.entity.Gender;
import org.fontory.fontorybe.provide.controller.port.ProvideService;
import org.fontory.fontorybe.provide.domain.Provide;
import org.fontory.fontorybe.provide.infrastructure.entity.Provider;
import org.fontory.fontorybe.provide.service.dto.ProvideCreateDto;
import org.fontory.fontorybe.unit.mock.TestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberCreationServiceTest {
    
    private MemberCreationService memberCreationService;
    private ProvideService provideService;
    private TestContainer testContainer;
    
    @BeforeEach
    void setUp() {
        testContainer = new TestContainer();
        memberCreationService = testContainer.memberCreationService;
        provideService = testContainer.provideService;
    }
    
    @Test
    @DisplayName("createDefaultMember - should create default member successfully")
    void createDefaultMemberSuccessTest() {
        // given
        ProvideCreateDto provideCreateDto = new ProvideCreateDto(
                Provider.GOOGLE,
                UUID.randomUUID().toString(),
                "test@example.com"
        );
        Provide provide = provideService.create(provideCreateDto);
        
        // when
        Member result = memberCreationService.createDefaultMember(provide);
        
        // then
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getId()).isNotNull(),
                () -> assertThat(result.getNickname()).isNotNull(),
                () -> assertThat(result.getNickname()).matches("^[a-f0-9-]+$"), // UUID format
                () -> assertThat(result.getGender()).isEqualTo(Gender.NONE), // Default from MemberDefaults
                () -> assertThat(result.getBirth()).isEqualTo(LocalDate.of(1999, 12, 31)), // Default from MemberDefaults
                () -> assertThat(result.getDeletedAt()).isNull(),
                () -> assertThat(result.getCreatedAt()).isNotNull(),
                () -> assertThat(result.getUpdatedAt()).isNotNull()
        );
        
        // Verify provide was updated with member
        // Note: We cannot verify provide was updated as getByEmailOrThrow method doesn't exist in ProvideService
        // The TestContainer implementation should handle the association
    }
    
    @Test
    @DisplayName("createDefaultMember - should throw MemberAlreadyExistException when provide already has member")
    void createDefaultMemberAlreadyExistsTest() {
        // given - create provide with member
        ProvideCreateDto provideCreateDto = new ProvideCreateDto(
                Provider.GOOGLE,
                UUID.randomUUID().toString(),
                "existing@example.com"
        );
        Provide provide = provideService.create(provideCreateDto);
        
        // Create member for this provide
        Member createdMember = memberCreationService.createDefaultMember(provide);
        
        // Manually set the member ID on the provide to simulate it having a member
        // (In real implementation, this would be done by ProvideService.setMember)
        Provide provideWithMember = Provide.builder()
                .id(provide.getId())
                .provider(provide.getProvider())
                .providedId(provide.getProvidedId()) 
                .email(provide.getEmail())
                .memberId(createdMember.getId())
                .build();
        
        // when & then
        assertThatThrownBy(
                () -> memberCreationService.createDefaultMember(provideWithMember)
        ).isExactlyInstanceOf(MemberAlreadyExistException.class);
    }
    
    @Test
    @DisplayName("createDefaultMember - should generate unique nickname for each member")
    void createDefaultMemberUniqueNicknameTest() {
        // given
        ProvideCreateDto provideCreateDto1 = new ProvideCreateDto(
                Provider.GOOGLE,
                UUID.randomUUID().toString(),
                "user1@example.com"
        );
        ProvideCreateDto provideCreateDto2 = new ProvideCreateDto(
                Provider.GOOGLE,
                UUID.randomUUID().toString(),
                "user2@example.com"
        );
        
        Provide provide1 = provideService.create(provideCreateDto1);
        Provide provide2 = provideService.create(provideCreateDto2);
        
        // when
        Member member1 = memberCreationService.createDefaultMember(provide1);
        Member member2 = memberCreationService.createDefaultMember(provide2);
        
        // then
        assertAll(
                () -> assertThat(member1.getNickname()).isNotNull(),
                () -> assertThat(member2.getNickname()).isNotNull(),
                () -> assertThat(member1.getNickname()).isNotEqualTo(member2.getNickname())
        );
    }
    
    @Test
    @DisplayName("createDefaultMember - should properly associate member with provide")
    void createDefaultMemberProvideAssociationTest() {
        // given
        String email = "association@example.com";
        String providerId = UUID.randomUUID().toString();
        ProvideCreateDto provideCreateDto = new ProvideCreateDto(
                Provider.GOOGLE,
                providerId,
                email
        );
        Provide provide = provideService.create(provideCreateDto);
        
        // when
        Member member = memberCreationService.createDefaultMember(provide);
        
        // then - verify the member has correct provide association
        assertAll(
                () -> assertThat(member.getProvideId()).isNotNull(),
                () -> assertThat(member.getNickname()).isNotNull()
        );
    }
    
    @Test
    @DisplayName("createDefaultMember - should use default values from MemberDefaults")
    void createDefaultMemberDefaultValuesTest() {
        // given
        ProvideCreateDto provideCreateDto = new ProvideCreateDto(
                Provider.GOOGLE,
                UUID.randomUUID().toString(),
                "defaults@example.com"
        );
        Provide provide = provideService.create(provideCreateDto);
        
        // when
        Member member = memberCreationService.createDefaultMember(provide);
        
        // then - verify default values from TestContainer's MemberDefaults
        assertAll(
                () -> assertThat(member.getGender()).isEqualTo(Gender.NONE),
                () -> assertThat(member.getBirth()).isEqualTo(LocalDate.of(1999, 12, 31))
        );
    }
}