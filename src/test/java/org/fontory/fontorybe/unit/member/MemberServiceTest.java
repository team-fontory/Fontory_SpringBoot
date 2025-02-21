package org.fontory.fontorybe.unit.member;

import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.fontory.fontorybe.member.controller.dto.MemberCreateRequest;
import org.fontory.fontorybe.member.controller.dto.MemberUpdateRequest;
import org.fontory.fontorybe.member.controller.port.MemberService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.domain.exception.MemberAlreadyDisabledException;
import org.fontory.fontorybe.member.domain.exception.MemberAlreadyExistException;
import org.fontory.fontorybe.member.domain.exception.MemberDuplicateNameExistsException;
import org.fontory.fontorybe.member.domain.exception.MemberNotFoundException;
import org.fontory.fontorybe.member.infrastructure.entity.Gender;
import org.fontory.fontorybe.provide.domain.Provide;
import org.fontory.fontorybe.provide.domain.exception.ProvideNotFoundException;
import org.fontory.fontorybe.provide.infrastructure.entity.Provider;
import org.fontory.fontorybe.provide.service.dto.ProvideCreateDto;
import org.fontory.fontorybe.unit.mock.TestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

public class MemberServiceTest {
    MemberService memberService;

    /**
     * testValues
     */
    Long nonExistentId = -1L;

    Long existMemberId = null;
    Gender existMemberGender = Gender.MALE;
    LocalDate existMemberBirth = LocalDate.of(2025, 1, 26);
    Boolean existMemberTerms = Boolean.TRUE;
    String existMemberNickName = "existMemberNickName";
    String existMemberProfileImage = "existMemberProfileImage";

    Gender newMemberGender = Gender.FEMALE;
    LocalDate newMemberBirth = LocalDate.of(2025, 1, 22);
    Boolean newMemberTerms = Boolean.FALSE;
    String newMemberNickName = "newMemberNickName";
    String newMemberProfileImage = "newMemberProfileImage";

    Boolean updateTerms = Boolean.FALSE;
    String updateNickName = "updateNickName";
    String updateProfileImage = "updateProfileImage";

    Long existMemberProvideId = null;
    String existMemberProvidedId = UUID.randomUUID().toString();
    String existMemberEmail = "existMemberEmail";
    Provider existMemberProvider = Provider.GOOGLE;

    String newMemberProvidedId = UUID.randomUUID().toString();
    String newMemberEmail = "newMemberEmail";
    Provider newMemberProvider = Provider.NAVER;

    UserPrincipal userPrincipal = null;
    TestContainer testContainer = null;

    @BeforeEach
    void init() {
        testContainer = new TestContainer();
        memberService = testContainer.memberService;
        ProvideCreateDto provideCreateDto = new ProvideCreateDto(existMemberProvider, existMemberProvidedId, existMemberEmail);
        Provide createdProvide = testContainer.provideService.create(provideCreateDto);
        String provideToken = testContainer.jwtTokenProvider.generateTemporalProvideToken(String.valueOf(createdProvide.getId()));

        MemberCreateRequest memberCreateRequest = new MemberCreateRequest(provideToken, existMemberNickName, existMemberGender, existMemberBirth, existMemberTerms, existMemberProfileImage);
        Member createdMember = testContainer.memberService.create(memberCreateRequest, testContainer.jwtTokenProvider.getProvideId(provideToken));
        existMemberId = createdMember.getId();
        existMemberProvideId = createdProvide.getId();
        userPrincipal = UserPrincipal.from(createdMember);
    }

    @Test
    @DisplayName("member - getOrThrownById success test")
    void getOrThrownByIdTest() {
        Member foundMember = memberService.getOrThrowById(existMemberId);
        assertAll(
                () -> assertThat(foundMember.getId()).isEqualTo(existMemberId),
                () -> assertThat(foundMember.getBirth()).isEqualTo(existMemberBirth),
                () -> assertThat(foundMember.getNickname()).isEqualTo(existMemberNickName),
                () -> assertThat(foundMember.getGender()).isEqualTo(existMemberGender),
                () -> assertThat(foundMember.getProvideId()).isEqualTo(existMemberProvideId),
                () -> assertThat(foundMember.getTerms()).isEqualTo(existMemberTerms),
                () -> assertThat(foundMember.getProfileImage()).isEqualTo(existMemberProfileImage),
                () -> assertThat(foundMember.getCreatedAt()).isNotNull(),
                () -> assertThat(foundMember.getUpdatedAt()).isNotNull(),
                () -> assertThat(foundMember.getDeletedAt()).isNull()
        );
    }

    @Test
    @DisplayName("member - getOrThrownById fail test caused by not found")
    void getOrThrownByIdTestX() {
        assertThatThrownBy(
                () -> memberService.getOrThrowById(nonExistentId)
        ).isExactlyInstanceOf(MemberNotFoundException.class);
    }

    @Test
    @DisplayName("member - create success test")
    void createTest() {
        ProvideCreateDto provideCreateDto = new ProvideCreateDto(newMemberProvider, newMemberProvidedId, newMemberEmail);
        Provide createdProvide = testContainer.provideService.create(provideCreateDto);
        String provideToken = testContainer.jwtTokenProvider.generateTemporalProvideToken(String.valueOf(createdProvide.getId()));

        MemberCreateRequest memberCreateRequestDto = new MemberCreateRequest(provideToken, newMemberNickName, newMemberGender, newMemberBirth, newMemberTerms, newMemberProfileImage);
        Member createdMember = memberService.create(memberCreateRequestDto, testContainer.jwtTokenProvider.getProvideId(provideToken));

        assertAll(
                () -> assertThat(createdMember.getId()).isNotNull(),
                () -> assertThat(createdMember.getProvideId()).isEqualTo(createdProvide.getId()),
                () -> assertThat(createdMember.getNickname()).isEqualTo(newMemberNickName),
                () -> assertThat(createdMember.getGender()).isEqualTo(newMemberGender),
                () -> assertThat(createdMember.getBirth()).isEqualTo(newMemberBirth),
                () -> assertThat(createdMember.getTerms()).isEqualTo(newMemberTerms),
                () -> assertThat(createdMember.getProfileImage()).isEqualTo(newMemberProfileImage),
                () -> assertThat(createdMember.getCreatedAt()).isNotNull(),
                () -> assertThat(createdMember.getUpdatedAt()).isNotNull()
        );
    }

    @Test
    @DisplayName("member - create fail test caused by provide Not found")
    void createTestX() {
        String nonExistProvideToken = testContainer.jwtTokenProvider.generateTemporalProvideToken(String.valueOf(nonExistentId));
        MemberCreateRequest memberCreateRequestDto = new MemberCreateRequest(nonExistProvideToken, newMemberNickName, newMemberGender, newMemberBirth, newMemberTerms, newMemberProfileImage);
        assertThatThrownBy(
                () -> memberService.create(memberCreateRequestDto, nonExistentId))
                .isExactlyInstanceOf(ProvideNotFoundException.class);
    }

    @Test
    @DisplayName("member - create fail test caused by duplicate nickname")
    void createDuplicateNicknameTest() {
        // 회원 생성
        ProvideCreateDto provideCreateDto1 = new ProvideCreateDto(newMemberProvider, newMemberProvidedId, newMemberEmail);
        ProvideCreateDto provideCreateDto2 = new ProvideCreateDto(newMemberProvider, newMemberProvidedId, newMemberEmail);
        Provide createdProvide1 = testContainer.provideService.create(provideCreateDto1);
        Provide createdProvide2 = testContainer.provideService.create(provideCreateDto2);
        String provideToken1 = testContainer.jwtTokenProvider.generateTemporalProvideToken(String.valueOf(createdProvide1.getId()));
        String provideToken2 = testContainer.jwtTokenProvider.generateTemporalProvideToken(String.valueOf(createdProvide2.getId()));

        MemberCreateRequest memberCreateRequestDto = new MemberCreateRequest(provideToken1, newMemberNickName, newMemberGender, newMemberBirth, newMemberTerms, newMemberProfileImage);
        memberService.create(memberCreateRequestDto, testContainer.jwtTokenProvider.getProvideId(provideToken1));

        // 동일 닉네임으로 또 회원 생성 시 예외 발생
        MemberCreateRequest duplicateMemberCreateRequestDto = new MemberCreateRequest(provideToken2, newMemberNickName, newMemberGender, newMemberBirth, newMemberTerms, newMemberProfileImage);
        assertThatThrownBy(
                () -> memberService.create(duplicateMemberCreateRequestDto, testContainer.jwtTokenProvider.getProvideId(provideToken2)))
                .isExactlyInstanceOf(MemberDuplicateNameExistsException.class);
    }

    @Test
    @DisplayName("member - update success")
    void updateTest() {
        Long requestMemberId = existMemberId;
        Member member = memberService.getOrThrowById(existMemberId);
        MemberUpdateRequest memberUpdateRequest = new MemberUpdateRequest(updateNickName, updateProfileImage, updateTerms);

        Member updatedMember = memberService.update(requestMemberId, memberUpdateRequest);
        assertAll(
                () -> assertThat(updatedMember.getId()).isEqualTo(member.getId()),
                () -> assertThat(updatedMember.getGender()).isEqualTo(member.getGender()),
                () -> assertThat(updatedMember.getBirth()).isEqualTo(member.getBirth()),
                () -> assertThat(updatedMember.getCreatedAt()).isEqualTo(member.getCreatedAt()),
                () -> assertThat(updatedMember.getProvideId()).isEqualTo(member.getProvideId()),
                () -> assertThat(updatedMember.getNickname()).isEqualTo(updateNickName),
                () -> assertThat(updatedMember.getProfileImage()).isEqualTo(updateProfileImage),
                () -> assertThat(updatedMember.getTerms()).isEqualTo(updateTerms),
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
        String provideToken1 = testContainer.jwtTokenProvider.generateTemporalProvideToken(String.valueOf(createdProvide1.getId()));
        String provideToken2 = testContainer.jwtTokenProvider.generateTemporalProvideToken(String.valueOf(createdProvide2.getId()));

        String uniqueNickname1 = UUID.randomUUID().toString();
        String uniqueNickname2 = UUID.randomUUID().toString();
        MemberCreateRequest memberCreateRequestDto1 = new MemberCreateRequest(provideToken1, uniqueNickname1, Gender.MALE, newMemberBirth, existMemberTerms, existMemberProfileImage);
        MemberCreateRequest memberCreateRequestDto2 = new MemberCreateRequest(provideToken2, uniqueNickname2, Gender.FEMALE, newMemberBirth, existMemberTerms, existMemberProfileImage);
        Member member1 = memberService.create(memberCreateRequestDto1, testContainer.jwtTokenProvider.getProvideId(provideToken1));
        memberService.create(memberCreateRequestDto2, testContainer.jwtTokenProvider.getProvideId(provideToken2));

        // member1의 닉네임을 이미 존재하는 이름으로 업데이트 시도하면 예외 발생
        MemberUpdateRequest memberUpdateRequest = new MemberUpdateRequest(uniqueNickname2, updateProfileImage, updateTerms);
        assertThatThrownBy(
                () -> memberService.update(member1.getId(), memberUpdateRequest))
                .isExactlyInstanceOf(MemberDuplicateNameExistsException.class);
    }

    @Test
    @DisplayName("member - create fail when provide create member more than one")
    void createProvideMemberMoreThanOneTest() {
        ProvideCreateDto provideCreateDto = new ProvideCreateDto(newMemberProvider, newMemberProvidedId, newMemberEmail);
        Provide createdProvide = testContainer.provideService.create(provideCreateDto);
        String provideToken = testContainer.jwtTokenProvider.generateTemporalProvideToken(String.valueOf(createdProvide.getId()));

        String uniqueNickname1 = UUID.randomUUID().toString();
        String uniqueNickname2 = UUID.randomUUID().toString();
        MemberCreateRequest memberCreateRequestDto1 = new MemberCreateRequest(provideToken, uniqueNickname1, Gender.MALE, newMemberBirth, existMemberTerms, existMemberProfileImage);
        MemberCreateRequest memberCreateRequestDto2 = new MemberCreateRequest(provideToken, uniqueNickname2, Gender.MALE, newMemberBirth, existMemberTerms, existMemberProfileImage);

        Member member1 = memberService.create(memberCreateRequestDto1, testContainer.jwtTokenProvider.getProvideId(provideToken));
        assertThatThrownBy(
                () -> memberService.create(memberCreateRequestDto2, testContainer.jwtTokenProvider.getProvideId(provideToken)))
                .isInstanceOf(MemberAlreadyExistException.class);
    }

    @Test
    @DisplayName("member - update fail test caused by member not found")
    void updateNonExistentMemberTest() {
        // 존재하지 않는 회원(-1L)을 대상으로 업데이트 시도 시 예외 발생
        MemberUpdateRequest memberUpdateRequest = new MemberUpdateRequest(updateNickName, updateProfileImage, updateTerms);
        assertThatThrownBy(
                () -> memberService.update(nonExistentId, memberUpdateRequest))
                .isExactlyInstanceOf(MemberNotFoundException.class);
    }

    @Test
    @DisplayName("member - update success test with no nickname change")
    void updateNoNicknameChangeTest() {
        // 닉네임 변경 없이 다른 정보만 업데이트하는 경우
        Member member = memberService.getOrThrowById(existMemberId);
        // 업데이트 DTO에서 기존 닉네임 그대로 사용
        MemberUpdateRequest memberUpdateRequest = new MemberUpdateRequest(member.getNickname(), updateProfileImage, updateTerms);

        // 같은 닉네임을 사용하더라도 본인의 정보이므로 중복 체크가 통과되어 업데이트 성공해야 함
        Member updatedMember = memberService.update(member.getId(), memberUpdateRequest);
        assertAll(
                () -> assertThat(updatedMember.getId()).isEqualTo(member.getId()),
                () -> assertThat(updatedMember.getNickname()).isEqualTo(member.getNickname()),
                () -> assertThat(updatedMember.getProfileImage()).isEqualTo(updateProfileImage),
                () -> assertThat(updatedMember.getTerms()).isEqualTo(updateTerms)
        );
    }

    @Test
    @DisplayName("member - duplicate name check")
    void isDuplicateNameExistsTest() {
        // 기존 회원의 닉네임은 이미 존재해야 함
        assertThat(memberService.isDuplicateNameExists(existMemberNickName)).isTrue();
        // 존재하지 않는 닉네임에 대해서는 false 반환해야 함
        assertThat(memberService.isDuplicateNameExists("nonExistingNickname")).isFalse();
    }

    @Test
    @DisplayName("member - disable success test")
    void disableTest() {
        // 회원 생성
        ProvideCreateDto provideCreateDto = new ProvideCreateDto(newMemberProvider, newMemberProvidedId, newMemberEmail);
        Provide createdProvide = testContainer.provideService.create(provideCreateDto);
        String provideToken = testContainer.jwtTokenProvider.generateTemporalProvideToken(String.valueOf(createdProvide.getId()));
        MemberCreateRequest memberCreateRequestDto = new MemberCreateRequest(provideToken, newMemberNickName, newMemberGender, newMemberBirth, newMemberTerms, newMemberProfileImage);
        Member member = memberService.create(memberCreateRequestDto, testContainer.jwtTokenProvider.getProvideId(provideToken));

        // 회원 비활성화
        Member disabledMember = memberService.disable(member.getId());
        assertThat(disabledMember.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("member - disable fail test caused by already disabled member")
    void disableAlreadyDisabledTest() {
        // 회원 생성
        ProvideCreateDto provideCreateDto = new ProvideCreateDto(newMemberProvider, newMemberProvidedId, newMemberEmail);
        Provide createdProvide = testContainer.provideService.create(provideCreateDto);
        String provideToken = testContainer.jwtTokenProvider.generateTemporalProvideToken(String.valueOf(createdProvide.getId()));
        MemberCreateRequest memberCreateRequestDto = new MemberCreateRequest(provideToken, newMemberNickName, newMemberGender, newMemberBirth, newMemberTerms, newMemberProfileImage);
        Member member = memberService.create(memberCreateRequestDto, testContainer.jwtTokenProvider.getProvideId(provideToken));

        // 회원 비활성화
        memberService.disable(member.getId());

        // 비활성화 회원 다시 비활성화시 에러발생
        assertThatThrownBy(
                () -> memberService.disable(member.getId()))
                .isExactlyInstanceOf(MemberAlreadyDisabledException.class);
    }
}