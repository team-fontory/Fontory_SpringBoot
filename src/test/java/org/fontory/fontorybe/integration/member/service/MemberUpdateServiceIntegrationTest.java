package org.fontory.fontorybe.integration.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.fontory.fontorybe.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.UUID;

import org.fontory.fontorybe.member.controller.dto.InitMemberInfoRequest;
import org.fontory.fontorybe.member.controller.port.MemberCreationService;
import org.fontory.fontorybe.member.controller.port.MemberLookupService;
import org.fontory.fontorybe.member.controller.port.MemberOnboardService;
import org.fontory.fontorybe.member.controller.port.MemberUpdateService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.controller.dto.MemberUpdateRequest;
import org.fontory.fontorybe.member.domain.exception.MemberAlreadyDisabledException;
import org.fontory.fontorybe.member.domain.exception.MemberDuplicateNameExistsException;
import org.fontory.fontorybe.member.domain.exception.MemberNotFoundException;
import org.fontory.fontorybe.provide.controller.port.ProvideService;
import org.fontory.fontorybe.provide.domain.Provide;
import org.fontory.fontorybe.provide.infrastructure.entity.Provider;
import org.fontory.fontorybe.provide.service.dto.ProvideCreateDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

@SpringBootTest
@Sql(value = "/sql/createMemberTestData.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/sql/deleteMemberTestData.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class MemberUpdateServiceIntegrationTest {

    @Autowired private MemberUpdateService memberUpdateService;
    @Autowired private MemberOnboardService memberOnboardService;
    @Autowired private MemberLookupService memberLookupService;
    @Autowired private MemberCreationService memberCreationService;
    @Autowired private ProvideService provideService;

    /**
     * 기존 테스트에서 사용한 값
     */
    private final Long nonExistentId = -1L;

    @Test
    @DisplayName("member - getOrThrowById success test")
    void getOrThrowByIdTest() {
        Member foundMember = memberLookupService.getOrThrowById(TEST_MEMBER_ID);
        assertAll(
                () -> assertThat(foundMember.getId()).isEqualTo(TEST_MEMBER_ID),
                () -> assertThat(foundMember.getBirth()).isEqualTo(TEST_MEMBER_BIRTH),
                () -> assertThat(foundMember.getNickname()).isEqualTo(TEST_MEMBER_NICKNAME),
                () -> assertThat(foundMember.getGender()).isEqualTo(TEST_MEMBER_GENDER),
                () -> assertThat(foundMember.getProvideId()).isEqualTo(TEST_PROVIDE_ID),
                () -> assertThat(foundMember.getTerms()).isEqualTo(TEST_MEMBER_TERMS),
                () -> assertThat(foundMember.getProfileImageKey()).isEqualTo(TEST_MEMBER_PROFILE_KEY),
                () -> assertThat(foundMember.getCreatedAt()).isNotNull(),
                () -> assertThat(foundMember.getUpdatedAt()).isNotNull(),
                () -> assertThat(foundMember.getDeletedAt()).isNull()
        );
    }

    @Test
    @DisplayName("member - getOrThrowById fail test caused by not found")
    void getOrThrowByIdTestX() {
        assertThatThrownBy(
                () -> memberLookupService.getOrThrowById(nonExistentId)
        ).isExactlyInstanceOf(MemberNotFoundException.class);
    }

    @Test
    @DisplayName("member - create success test")
    void createTest() {
        InitMemberInfoRequest initNewMemberRequest = new InitMemberInfoRequest(NEW_MEMBER_NICKNAME, NEW_MEMBER_GENDER, NEW_MEMBER_BIRTH, NEW_MEMBER_TERMS, NEW_MEMBER_PROFILE_KEY);
        ProvideCreateDto newProvideCreateDto = new ProvideCreateDto(NEW_MEMBER_PROVIDER, NEW_MEMBER_PROVIDED_ID, NEW_MEMBER_EMAIL);
        Provide createdProvide = provideService.create(newProvideCreateDto);
        Member createdMember = create(initNewMemberRequest, createdProvide);

        assertAll(
                () -> assertThat(createdMember.getId()).isNotNull(),
                () -> assertThat(createdMember.getProvideId()).isEqualTo(createdProvide.getId()),
                () -> assertThat(createdMember.getNickname()).isEqualTo(NEW_MEMBER_NICKNAME),
                () -> assertThat(createdMember.getGender()).isEqualTo(NEW_MEMBER_GENDER),
                () -> assertThat(createdMember.getBirth()).isEqualTo(NEW_MEMBER_BIRTH),
                () -> assertThat(createdMember.getTerms()).isEqualTo(NEW_MEMBER_TERMS),
                () -> assertThat(createdMember.getProfileImageKey()).isEqualTo(NEW_MEMBER_PROFILE_KEY),
                () -> assertThat(createdMember.getCreatedAt()).isNotNull(),
                () -> assertThat(createdMember.getUpdatedAt()).isNotNull()
        );
    }

    @Test
    @DisplayName("member - create fail test caused by duplicate nickname")
    void createDuplicateNicknameTest() {
        ProvideCreateDto provideCreateDto1 = new ProvideCreateDto(Provider.GOOGLE, UUID.randomUUID().toString(), UUID.randomUUID().toString());
        ProvideCreateDto provideCreateDto2 = new ProvideCreateDto(Provider.NAVER, UUID.randomUUID().toString(), UUID.randomUUID().toString());
        Provide createdProvide1 = provideService.create(provideCreateDto1);
        Provide createdProvide2 = provideService.create(provideCreateDto2);
        // 첫 번째 회원 생성
        InitMemberInfoRequest initNewMemberRequest = new InitMemberInfoRequest(NEW_MEMBER_NICKNAME, NEW_MEMBER_GENDER, NEW_MEMBER_BIRTH, NEW_MEMBER_TERMS, NEW_MEMBER_PROFILE_KEY);
        create(initNewMemberRequest, createdProvide1);

        // 동일 닉네임으로 또 회원 생성 시 예외 발생
        InitMemberInfoRequest duplicateInitNewMemberInfoRequest = new InitMemberInfoRequest(NEW_MEMBER_NICKNAME, NEW_MEMBER_GENDER, NEW_MEMBER_BIRTH, NEW_MEMBER_TERMS, NEW_MEMBER_PROFILE_KEY);
        assertThatThrownBy(
                () -> create(duplicateInitNewMemberInfoRequest, createdProvide2))
                .isExactlyInstanceOf(MemberDuplicateNameExistsException.class);
    }

    @Test
    @DisplayName("member - update success test")
    void updateTest() {
        // 기존에 존재하는 회원(TEST_MEMBER_ID) 조회
        Member member = memberLookupService.getOrThrowById(TEST_MEMBER_ID);
        MemberUpdateRequest memberUpdateRequest = new MemberUpdateRequest(UPDATE_MEMBER_NICKNAME, UPDATE_MEMBER_TERMS);

        // 업데이트 요청: 단일 회원 ID만 전달
        Member updatedMember = memberUpdateService.update(TEST_MEMBER_ID, memberUpdateRequest);
        assertAll(
                () -> assertThat(updatedMember.getId()).isEqualTo(member.getId()),
                () -> assertThat(updatedMember.getGender()).isEqualTo(member.getGender()),
                () -> assertThat(updatedMember.getBirth()).isEqualTo(member.getBirth()),
                () -> assertThat(updatedMember.getCreatedAt()).isEqualTo(member.getCreatedAt()),
                () -> assertThat(updatedMember.getProvideId()).isEqualTo(member.getProvideId()),
                () -> assertThat(updatedMember.getNickname()).isEqualTo(UPDATE_MEMBER_NICKNAME),
                () -> assertThat(updatedMember.getProfileImageKey()).isEqualTo(member.getProfileImageKey()),
                () -> assertThat(updatedMember.getTerms()).isEqualTo(UPDATE_MEMBER_TERMS),
                () -> assertThat(updatedMember.getUpdatedAt()).isAfter(member.getUpdatedAt())
        );
    }

    @Test
    @DisplayName("member - update fail test caused by duplicate nickname")
    void updateDuplicateNicknameTest() {
        ProvideCreateDto provideCreateDto1 = new ProvideCreateDto(Provider.GOOGLE, UUID.randomUUID().toString(), UUID.randomUUID().toString());
        ProvideCreateDto provideCreateDto2 = new ProvideCreateDto(Provider.NAVER, UUID.randomUUID().toString(), UUID.randomUUID().toString());
        Provide createdProvide1 = provideService.create(provideCreateDto1);
        Provide createdProvide2 = provideService.create(provideCreateDto2);

        // 두 회원을 각각 생성
        String uniqueNickname1 = UUID.randomUUID().toString();
        String uniqueNickname2 = UUID.randomUUID().toString();
        InitMemberInfoRequest initNewMemberInfoRequestDto1 = new InitMemberInfoRequest(uniqueNickname1, NEW_MEMBER_GENDER, NEW_MEMBER_BIRTH, NEW_MEMBER_TERMS, NEW_MEMBER_PROFILE_KEY);
        InitMemberInfoRequest initNewMemberInfoRequestDto2 = new InitMemberInfoRequest(uniqueNickname2, NEW_MEMBER_GENDER, NEW_MEMBER_BIRTH, NEW_MEMBER_TERMS, NEW_MEMBER_PROFILE_KEY);
        Member member1 = create(initNewMemberInfoRequestDto1, createdProvide1);
        create(initNewMemberInfoRequestDto2, createdProvide2);

        // member1의 닉네임을 이미 존재하는 이름(uniqueNickname2)으로 업데이트 시도하면 예외 발생
        MemberUpdateRequest memberUpdateRequest = new MemberUpdateRequest(uniqueNickname2, UPDATE_MEMBER_TERMS);
        Long member1Id = member1.getId();

        assertThatThrownBy(
                () -> memberUpdateService.update(member1Id, memberUpdateRequest))
                .isExactlyInstanceOf(MemberDuplicateNameExistsException.class);
    }

    @Test
    @DisplayName("member - update fail test caused by member not found")
    void updateNonExistentMemberTest() {
        MemberUpdateRequest memberUpdateRequest = new MemberUpdateRequest(UPDATE_MEMBER_NICKNAME, UPDATE_MEMBER_TERMS);
        assertThatThrownBy(
                () -> memberUpdateService.update(nonExistentId, memberUpdateRequest))
                .isExactlyInstanceOf(MemberNotFoundException.class);
    }

    @Test
    @DisplayName("member - disable success test")
    void disableTest() {
        // 회원 생성
        ProvideCreateDto provideCreateDto = new ProvideCreateDto(Provider.GOOGLE, UUID.randomUUID().toString(), UUID.randomUUID().toString());
        Provide createdProvide = provideService.create(provideCreateDto);

        InitMemberInfoRequest initNewMemberRequest = new InitMemberInfoRequest(NEW_MEMBER_NICKNAME, NEW_MEMBER_GENDER, NEW_MEMBER_BIRTH, NEW_MEMBER_TERMS, NEW_MEMBER_PROFILE_KEY);
        Member member = create(initNewMemberRequest, createdProvide);

        // 회원 비활성화 요청 (단일 ID)
        Member disabledMember = memberUpdateService.disable(member.getId());
        assertThat(disabledMember.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("member - disable fail test caused by already disabled member")
    void disableAlreadyDisabledTest() {
        // 회원 생성
        ProvideCreateDto provideCreateDto = new ProvideCreateDto(Provider.GOOGLE, UUID.randomUUID().toString(), UUID.randomUUID().toString());
        Provide createdProvide = provideService.create(provideCreateDto);

        InitMemberInfoRequest initNewMemberRequest = new InitMemberInfoRequest(NEW_MEMBER_NICKNAME, NEW_MEMBER_GENDER, NEW_MEMBER_BIRTH, NEW_MEMBER_TERMS, NEW_MEMBER_PROFILE_KEY);
        Member member = create(initNewMemberRequest, createdProvide);

        // 최초 비활성화 처리
        memberUpdateService.disable(member.getId());
        Long id = member.getId();

        // 이미 비활성화된 회원에 대해 다시 disable 요청 시 예외 발생
        assertThatThrownBy(
                () -> memberUpdateService.disable(id))
                .isExactlyInstanceOf(MemberAlreadyDisabledException.class);
    }

    private Member create(InitMemberInfoRequest initNewMemberInfoRequest, Provide provide) {
        Member defaultMember = memberCreationService.createDefaultMember(provide);
        return memberOnboardService.initNewMemberInfo(defaultMember.getId(), initNewMemberInfoRequest);
    }
}