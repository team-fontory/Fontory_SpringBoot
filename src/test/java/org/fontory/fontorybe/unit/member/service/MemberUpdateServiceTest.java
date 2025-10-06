package org.fontory.fontorybe.unit.member.service;

import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.fontory.fontorybe.member.controller.dto.InitMemberInfoRequest;
import org.fontory.fontorybe.member.controller.dto.MemberUpdateRequest;
import org.fontory.fontorybe.member.controller.port.MemberCreationService;
import org.fontory.fontorybe.member.controller.port.MemberLookupService;
import org.fontory.fontorybe.member.controller.port.MemberOnboardService;
import org.fontory.fontorybe.member.controller.port.MemberUpdateService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.domain.MemberDefaults;
import org.fontory.fontorybe.member.domain.exception.MemberAlreadyDisabledException;
import org.fontory.fontorybe.member.domain.exception.MemberAlreadyExistException;
import org.fontory.fontorybe.member.domain.exception.MemberDuplicateNameExistsException;
import org.fontory.fontorybe.member.domain.exception.MemberNotFoundException;
import org.fontory.fontorybe.member.infrastructure.entity.Gender;
import org.fontory.fontorybe.member.infrastructure.entity.MemberStatus;
import org.fontory.fontorybe.provide.domain.Provide;
import org.fontory.fontorybe.provide.infrastructure.entity.Provider;
import org.fontory.fontorybe.provide.service.dto.ProvideCreateDto;
import org.fontory.fontorybe.unit.mock.TestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.fontory.fontorybe.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertAll;

class MemberUpdateServiceTest {
    MemberUpdateService memberUpdateService;
    MemberLookupService memberLookupService;
    MemberOnboardService memberOnboardService;
    MemberCreationService memberCreationService;
    MemberDefaults memberDefaults;

    /**
     * testValues
     */
    Long nonExistentId = -1L;
    Long testMemberId = null;
    Long testMemberProvideId = null;

    UserPrincipal userPrincipal = null;
    TestContainer testContainer = null;

    @BeforeEach
    void init() {
        testContainer = new TestContainer();
        memberUpdateService = testContainer.memberUpdateService;
        memberOnboardService = testContainer.memberOnboardService;
        memberCreationService = testContainer.memberCreationService;
        memberLookupService = testContainer.memberLookupService;
        memberDefaults = testContainer.memberDefaults;
        Member createdMember = testContainer.createTestMember();
        testMemberId = createdMember.getId();
        testMemberProvideId = testContainer.testMemberProvide.getMemberId();
        userPrincipal = UserPrincipal.from(createdMember);
    }

    @Test
    @DisplayName("member - getOrThrownById success test")
    void getOrThrownByIdTest() {
        Member foundMember = memberLookupService.getOrThrowById(testMemberId);
        assertAll(
                () -> assertThat(foundMember.getId()).isEqualTo(testMemberId),
                () -> assertThat(foundMember.getBirth()).isEqualTo(TEST_MEMBER_BIRTH),
                () -> assertThat(foundMember.getNickname()).isEqualTo(TEST_MEMBER_NICKNAME),
                () -> assertThat(foundMember.getGender()).isEqualTo(TEST_MEMBER_GENDER),
                () -> assertThat(foundMember.getProvideId()).isEqualTo(testMemberProvideId),
                () -> assertThat(foundMember.getCreatedAt()).isNotNull(),
                () -> assertThat(foundMember.getUpdatedAt()).isNotNull(),
                () -> assertThat(foundMember.getDeletedAt()).isNull()
        );
    }

    @Test
    @DisplayName("member - getOrThrownById fail test caused by not found")
    void getOrThrownByIdTestX() {
        assertThatThrownBy(
                () -> memberLookupService.getOrThrowById(nonExistentId)
        ).isExactlyInstanceOf(MemberNotFoundException.class);
    }

    @Test
    @DisplayName("OAUTH2 Login Success and make default Member")
    void makeDefaultMember() {
        //givne
        ProvideCreateDto provideCreateDto = new ProvideCreateDto(NEW_MEMBER_PROVIDER, NEW_MEMBER_PROVIDED_ID, NEW_MEMBER_EMAIL);
        Provide createdProvide = testContainer.provideService.create(provideCreateDto);

        Member defaultMember = memberCreationService.createDefaultMember(createdProvide);

        assertAll(
                () -> assertThat(defaultMember.getId()).isNotNull(),
                () -> assertThat(defaultMember.getProvideId()).isEqualTo(createdProvide.getId()),
                () -> assertThat(defaultMember.getNickname()).isNotNull(),
                () -> assertThat(defaultMember.getGender()).isEqualTo(memberDefaults.getGender()),
                () -> assertThat(defaultMember.getBirth()).isEqualTo(memberDefaults.getBirth()),
                () -> assertThat(defaultMember.getCreatedAt()).isNotNull(),
                () -> assertThat(defaultMember.getUpdatedAt()).isNotNull()
        );
    }

    @Test
    @DisplayName("member - create success test")
    void createTest() {
        ProvideCreateDto provideCreateDto = new ProvideCreateDto(NEW_MEMBER_PROVIDER, NEW_MEMBER_PROVIDED_ID, NEW_MEMBER_EMAIL);
        Provide createdProvide = testContainer.provideService.create(provideCreateDto);

        InitMemberInfoRequest initNewMemberRequestDto = new InitMemberInfoRequest(NEW_MEMBER_NICKNAME, NEW_MEMBER_GENDER, NEW_MEMBER_BIRTH);
        Member createdMember = testContainer.create(initNewMemberRequestDto, createdProvide);

        assertAll(
                () -> assertThat(createdMember.getId()).isNotNull(),
                () -> assertThat(createdMember.getProvideId()).isEqualTo(createdProvide.getId()),
                () -> assertThat(createdMember.getNickname()).isEqualTo(NEW_MEMBER_NICKNAME),
                () -> assertThat(createdMember.getGender()).isEqualTo(NEW_MEMBER_GENDER),
                () -> assertThat(createdMember.getBirth()).isEqualTo(NEW_MEMBER_BIRTH),
                () -> assertThat(createdMember.getCreatedAt()).isNotNull(),
                () -> assertThat(createdMember.getUpdatedAt()).isNotNull()
        );
    }

    @Test
    @DisplayName("member - create fail test caused by duplicate nickname")
    void createDuplicateNicknameTest() {
        // 회원 생성
        ProvideCreateDto provideCreateDto1 = new ProvideCreateDto(NEW_MEMBER_PROVIDER, NEW_MEMBER_PROVIDED_ID, NEW_MEMBER_EMAIL);
        ProvideCreateDto provideCreateDto2 = new ProvideCreateDto(NEW_MEMBER_PROVIDER, NEW_MEMBER_PROVIDED_ID, NEW_MEMBER_EMAIL);
        Provide createdProvide1 = testContainer.provideService.create(provideCreateDto1);
        Provide createdProvide2 = testContainer.provideService.create(provideCreateDto2);

        InitMemberInfoRequest initNewMemberRequestDto = new InitMemberInfoRequest(NEW_MEMBER_NICKNAME, NEW_MEMBER_GENDER, NEW_MEMBER_BIRTH);
        testContainer.create(initNewMemberRequestDto, createdProvide1);

        // 동일 닉네임으로 또 회원 생성 시 예외 발생
        InitMemberInfoRequest duplicateInitNewMemberInfoRequest = new InitMemberInfoRequest(NEW_MEMBER_NICKNAME, NEW_MEMBER_GENDER, NEW_MEMBER_BIRTH);
        assertThatThrownBy(
                () -> testContainer.create(duplicateInitNewMemberInfoRequest, createdProvide2))
                .isExactlyInstanceOf(MemberDuplicateNameExistsException.class);
    }

    @Test
    @DisplayName("member - update success")
    void updateTest() {
        Long requestMemberId = testMemberId;
        Member member = memberLookupService.getOrThrowById(testMemberId);
        MemberUpdateRequest memberUpdateRequest = new MemberUpdateRequest(UPDATE_MEMBER_NICKNAME);

        Member updatedMember = memberUpdateService.update(requestMemberId, memberUpdateRequest);
        assertAll(
                () -> assertThat(updatedMember.getId()).isEqualTo(member.getId()),
                () -> assertThat(updatedMember.getGender()).isEqualTo(member.getGender()),
                () -> assertThat(updatedMember.getBirth()).isEqualTo(member.getBirth()),
                () -> assertThat(updatedMember.getCreatedAt()).isEqualTo(member.getCreatedAt()),
                () -> assertThat(updatedMember.getProvideId()).isEqualTo(member.getProvideId()),
                () -> assertThat(updatedMember.getNickname()).isEqualTo(UPDATE_MEMBER_NICKNAME),
                () -> assertThat(updatedMember.getUpdatedAt()).isAfter(member.getUpdatedAt())
        );
    }

    @Test
    @DisplayName("member - update fail test caused by duplicate nickname")
    void updateDuplicateNicknameTest() {
        // 두 회원을 각각 생성
        ProvideCreateDto provideCreateDto1 = new ProvideCreateDto(Provider.GOOGLE, UUID.randomUUID().toString(), UUID.randomUUID().toString());
        ProvideCreateDto provideCreateDto2 = new ProvideCreateDto(Provider.NAVER, UUID.randomUUID().toString(), UUID.randomUUID().toString());
        Provide createdProvide1 = testContainer.provideService.create(provideCreateDto1);
        Provide createdProvide2 = testContainer.provideService.create(provideCreateDto2);

        String uniqueNickname1 = UUID.randomUUID().toString();
        String uniqueNickname2 = UUID.randomUUID().toString();
        InitMemberInfoRequest initNewMemberRequestDto1 = new InitMemberInfoRequest(uniqueNickname1, Gender.MALE, NEW_MEMBER_BIRTH);
        InitMemberInfoRequest initNewMemberRequestDto2 = new InitMemberInfoRequest(uniqueNickname2, Gender.FEMALE, NEW_MEMBER_BIRTH);
        Member member1 = testContainer.create(initNewMemberRequestDto1, createdProvide1);
        testContainer.create(initNewMemberRequestDto2, createdProvide2);

        // member1의 닉네임을 이미 존재하는 이름으로 업데이트 시도하면 예외 발생
        MemberUpdateRequest memberUpdateRequest = new MemberUpdateRequest(uniqueNickname2);
        assertThatThrownBy(
                () -> memberUpdateService.update(member1.getId(), memberUpdateRequest))
                .isExactlyInstanceOf(MemberDuplicateNameExistsException.class);
    }

    @Test
    @DisplayName("member - create fail when provide create member more than one")
    void createProvideMemberMoreThanOneTest() {
        ProvideCreateDto provideCreateDto = new ProvideCreateDto(NEW_MEMBER_PROVIDER, NEW_MEMBER_PROVIDED_ID, NEW_MEMBER_EMAIL);
        Provide createdProvide = testContainer.provideService.create(provideCreateDto);

        String uniqueNickname1 = UUID.randomUUID().toString();
        String uniqueNickname2 = UUID.randomUUID().toString();
        InitMemberInfoRequest initNewMemberRequestDto1 = new InitMemberInfoRequest(uniqueNickname1, Gender.MALE, NEW_MEMBER_BIRTH);
        InitMemberInfoRequest initNewMemberRequestDto2 = new InitMemberInfoRequest(uniqueNickname2, Gender.MALE, NEW_MEMBER_BIRTH);

        testContainer.create(initNewMemberRequestDto1, createdProvide);
        assertThatThrownBy(
                () -> testContainer.create(initNewMemberRequestDto2, createdProvide))
                .isInstanceOf(MemberAlreadyExistException.class);
    }

    @Test
    @DisplayName("member - update fail test caused by member not found")
    void updateNonExistentMemberTest() {
        // 존재하지 않는 회원(-1L)을 대상으로 업데이트 시도 시 예외 발생
        MemberUpdateRequest memberUpdateRequest = new MemberUpdateRequest(UPDATE_MEMBER_NICKNAME);
        assertThatThrownBy(
                () -> memberUpdateService.update(nonExistentId, memberUpdateRequest))
                .isExactlyInstanceOf(MemberNotFoundException.class);
    }

    @Test
    @DisplayName("member - update success test with no nickname change")
    void updateNoNicknameChangeTest() {
        // 닉네임 변경 없이 다른 정보만 업데이트하는 경우
        Member member = memberLookupService.getOrThrowById(testMemberId);
        // 업데이트 DTO에서 기존 닉네임 그대로 사용
        MemberUpdateRequest memberUpdateRequest = new MemberUpdateRequest(member.getNickname());

        // 같은 닉네임을 사용하더라도 본인의 정보이므로 중복 체크가 통과되어 업데이트 성공해야 함
        Member updatedMember = memberUpdateService.update(member.getId(), memberUpdateRequest);
        assertAll(
                () -> assertThat(updatedMember.getId()).isEqualTo(member.getId()),
                () -> assertThat(updatedMember.getNickname()).isEqualTo(member.getNickname())
        );
    }

    @Test
    @DisplayName("member - duplicate name check")
    void isDuplicateNameExistsTest() {
        // 기존 회원의 닉네임은 이미 존재해야 함
        assertThat(memberLookupService.existsByNickname(TEST_MEMBER_NICKNAME)).isTrue();
        // 존재하지 않는 닉네임에 대해서는 false 반환해야 함
        assertThat(memberLookupService.existsByNickname("nonExistingNickname")).isFalse();
    }

    @Test
    @DisplayName("member - disable success test")
    void disableTest() {
        // 회원 생성
        ProvideCreateDto provideCreateDto = new ProvideCreateDto(NEW_MEMBER_PROVIDER, NEW_MEMBER_PROVIDED_ID, NEW_MEMBER_EMAIL);
        Provide createdProvide = testContainer.provideService.create(provideCreateDto);

        InitMemberInfoRequest initNewMemberRequestDto = new InitMemberInfoRequest(NEW_MEMBER_NICKNAME, NEW_MEMBER_GENDER, NEW_MEMBER_BIRTH);
        Member member = testContainer.create(initNewMemberRequestDto, createdProvide);

        // 회원 비활성화
        Member disabledMember = memberUpdateService.disable(member.getId());
        assertAll(
                () -> assertThat(disabledMember.getDeletedAt()).isNotNull(),
                () -> assertThat(disabledMember.getStatus()).isEqualTo(MemberStatus.DEACTIVATE)
        );
    }

    @Test
    @DisplayName("member - disable fail test caused by already disabled member")
    void disableAlreadyDisabledTest() {
        // 회원 생성
        ProvideCreateDto provideCreateDto = new ProvideCreateDto(NEW_MEMBER_PROVIDER, NEW_MEMBER_PROVIDED_ID, NEW_MEMBER_EMAIL);
        Provide createdProvide = testContainer.provideService.create(provideCreateDto);

        InitMemberInfoRequest initNewMemberRequestDto = new InitMemberInfoRequest(NEW_MEMBER_NICKNAME, NEW_MEMBER_GENDER, NEW_MEMBER_BIRTH);
        Member member = testContainer.create(initNewMemberRequestDto, createdProvide);

        // 회원 비활성화
        memberUpdateService.disable(member.getId());

        // 비활성화 회원 다시 비활성화시 에러발생
        assertThatThrownBy(
                () -> memberUpdateService.disable(member.getId()))
                .isExactlyInstanceOf(MemberAlreadyDisabledException.class);
    }
}