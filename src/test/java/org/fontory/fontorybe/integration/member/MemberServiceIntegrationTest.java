package org.fontory.fontorybe.integration.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.util.UUID;

import org.fontory.fontorybe.authentication.adapter.outbound.JwtTokenProvider;
import org.fontory.fontorybe.member.controller.dto.MemberCreateRequest;
import org.fontory.fontorybe.member.controller.port.MemberService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.controller.dto.MemberUpdateRequest;
import org.fontory.fontorybe.member.domain.exception.MemberAlreadyDisabledException;
import org.fontory.fontorybe.member.domain.exception.MemberDuplicateNameExistsException;
import org.fontory.fontorybe.member.domain.exception.MemberNotFoundException;
import org.fontory.fontorybe.member.infrastructure.entity.Gender;
import org.fontory.fontorybe.provide.controller.port.ProvideService;
import org.fontory.fontorybe.provide.domain.Provide;
import org.fontory.fontorybe.provide.domain.exception.ProvideNotFoundException;
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
class MemberServiceIntegrationTest {

    @Autowired private MemberService memberService;
    @Autowired private ProvideService provideService;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    /**
     * 기존 테스트에서 사용한 값
     */
    private final Long nonExistentId = -1L;
    private final Long existMemberId = 999L;          // @Sql 로 생성된 기존 회원 ID
    private final Long existMemberProvideId = 1L;           // 기존 제공자 ID
    private final Gender existMemberGender = Gender.MALE;
    private final LocalDate existMemberBirth = LocalDate.of(2025, 1, 26);
    private final Boolean existMemberTerms = Boolean.TRUE;
    private final String existMemberNickName = "existMemberNickName";
    private final String existMemberProfileImage = "existMemberProfileImage";

    private final Gender newMemberGender = Gender.FEMALE;
    private final LocalDate newMemberBirth = LocalDate.of(2025, 1, 22);
    private final boolean newMemberTerms = false;
    private final String newMemberNickName = "newMemberNickName";
    private final String newMemberProfileImage = "newMemberProfileImage";

    private final Boolean updateTerms = Boolean.FALSE;
    private final String updateNickName = "updateNickName";
    private final String updateProfileImage = "updateProfileImage";

    @Test
    @DisplayName("member - getOrThrowById success test")
    void getOrThrowByIdTest() {
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
    @DisplayName("member - getOrThrowById fail test caused by not found")
    void getOrThrowByIdTestX() {
        assertThatThrownBy(
                () -> memberService.getOrThrowById(nonExistentId)
        ).isExactlyInstanceOf(MemberNotFoundException.class);
    }

    @Test
    @DisplayName("member - create success test")
    void createTest() {
        String temporalProvideToken = jwtTokenProvider.generateTemporalProvideToken(String.valueOf(existMemberProvideId));
        MemberCreateRequest memberCreateRequestDto = new MemberCreateRequest(temporalProvideToken, newMemberNickName, newMemberGender, newMemberBirth, newMemberTerms, newMemberProfileImage);
        Member createdMember = memberService.create(memberCreateRequestDto);

        assertAll(
                () -> assertThat(createdMember.getId()).isNotNull(),
                () -> assertThat(createdMember.getProvideId()).isEqualTo(existMemberProvideId),
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
        String temporalProvideToken = jwtTokenProvider.generateTemporalProvideToken(String.valueOf(nonExistentId));
        MemberCreateRequest memberCreateRequestDto = new MemberCreateRequest(temporalProvideToken, newMemberNickName, newMemberGender, newMemberBirth, newMemberTerms, newMemberProfileImage);
        assertThatThrownBy(
                () -> memberService.create(memberCreateRequestDto))
                .isExactlyInstanceOf(ProvideNotFoundException.class);
    }

    @Test
    @DisplayName("member - create fail test caused by duplicate nickname")
    void createDuplicateNicknameTest() {
        ProvideCreateDto provideCreateDto1 = new ProvideCreateDto(Provider.GOOGLE, UUID.randomUUID().toString(), UUID.randomUUID().toString());
        ProvideCreateDto provideCreateDto2 = new ProvideCreateDto(Provider.NAVER, UUID.randomUUID().toString(), UUID.randomUUID().toString());
        Provide createdProvide1 = provideService.create(provideCreateDto1);
        Provide createdProvide2 = provideService.create(provideCreateDto2);
        String provideToken1 = jwtTokenProvider.generateTemporalProvideToken(String.valueOf(createdProvide1.getId()));
        String provideToken2 = jwtTokenProvider.generateTemporalProvideToken(String.valueOf(createdProvide2.getId()));
        // 첫 번째 회원 생성
        MemberCreateRequest memberCreateRequestDto = new MemberCreateRequest(provideToken1, newMemberNickName, newMemberGender, newMemberBirth, newMemberTerms, newMemberProfileImage);
        memberService.create(memberCreateRequestDto);

        // 동일 닉네임으로 또 회원 생성 시 예외 발생
        MemberCreateRequest duplicateMemberCreateRequestDto = new MemberCreateRequest(provideToken2, newMemberNickName, newMemberGender, newMemberBirth, newMemberTerms, newMemberProfileImage);
        assertThatThrownBy(
                () -> memberService.create(duplicateMemberCreateRequestDto))
                .isExactlyInstanceOf(MemberDuplicateNameExistsException.class);
    }

    @Test
    @DisplayName("member - update success test")
    void updateTest() {
        // 기존에 존재하는 회원(existMemberId) 조회
        Member member = memberService.getOrThrowById(existMemberId);
        MemberUpdateRequest memberUpdateRequest = new MemberUpdateRequest(updateNickName, updateProfileImage, updateTerms);

        // 업데이트 요청: 단일 회원 ID만 전달
        Member updatedMember = memberService.update(existMemberId, memberUpdateRequest);
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
        ProvideCreateDto provideCreateDto1 = new ProvideCreateDto(Provider.GOOGLE, UUID.randomUUID().toString(), UUID.randomUUID().toString());
        ProvideCreateDto provideCreateDto2 = new ProvideCreateDto(Provider.NAVER, UUID.randomUUID().toString(), UUID.randomUUID().toString());
        Provide createdProvide1 = provideService.create(provideCreateDto1);
        Provide createdProvide2 = provideService.create(provideCreateDto2);
        String provideToken1 = jwtTokenProvider.generateTemporalProvideToken(String.valueOf(createdProvide1.getId()));
        String provideToken2 = jwtTokenProvider.generateTemporalProvideToken(String.valueOf(createdProvide2.getId()));
        // 두 회원을 각각 생성
        String uniqueNickname1 = UUID.randomUUID().toString();
        String uniqueNickname2 = UUID.randomUUID().toString();
        MemberCreateRequest memberCreateRequestDto1 = new MemberCreateRequest(provideToken1, uniqueNickname1, newMemberGender, newMemberBirth, newMemberTerms, newMemberProfileImage);
        MemberCreateRequest memberCreateRequestDto2 = new MemberCreateRequest(provideToken2, uniqueNickname2, newMemberGender, newMemberBirth, newMemberTerms, newMemberProfileImage);
        Member member1 = memberService.create(memberCreateRequestDto1);
        memberService.create(memberCreateRequestDto2);

        // member1의 닉네임을 이미 존재하는 이름(uniqueNickname2)으로 업데이트 시도하면 예외 발생
        MemberUpdateRequest memberUpdateRequest = new MemberUpdateRequest(uniqueNickname2, updateProfileImage, updateTerms);
        assertThatThrownBy(
                () -> memberService.update(member1.getId(), memberUpdateRequest))
                .isExactlyInstanceOf(MemberDuplicateNameExistsException.class);
    }

    @Test
    @DisplayName("member - update fail test caused by member not found")
    void updateNonExistentMemberTest() {
        MemberUpdateRequest memberUpdateRequest = new MemberUpdateRequest(updateNickName, updateProfileImage, updateTerms);
        assertThatThrownBy(
                () -> memberService.update(nonExistentId, memberUpdateRequest))
                .isExactlyInstanceOf(MemberNotFoundException.class);
    }

    @Test
    @DisplayName("member - disable success test")
    void disableTest() {
        // 회원 생성
        String temporalProvideToken = jwtTokenProvider.generateTemporalProvideToken(String.valueOf(existMemberProvideId));
        MemberCreateRequest memberCreateRequestDto = new MemberCreateRequest(temporalProvideToken, newMemberNickName, newMemberGender, newMemberBirth, newMemberTerms, newMemberProfileImage);
        Member member = memberService.create(memberCreateRequestDto);

        // 회원 비활성화 요청 (단일 ID)
        Member disabledMember = memberService.disable(member.getId());
        assertThat(disabledMember.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("member - disable fail test caused by already disabled member")
    void disableAlreadyDisabledTest() {
        // 회원 생성
        String temporalProvideToken = jwtTokenProvider.generateTemporalProvideToken(String.valueOf(existMemberProvideId));
        MemberCreateRequest memberCreateRequestDto = new MemberCreateRequest(temporalProvideToken, newMemberNickName, newMemberGender, newMemberBirth, newMemberTerms, newMemberProfileImage);
        Member member = memberService.create(memberCreateRequestDto);

        // 최초 비활성화 처리
        memberService.disable(member.getId());

        // 이미 비활성화된 회원에 대해 다시 disable 요청 시 예외 발생
        assertThatThrownBy(
                () -> memberService.disable(member.getId()))
                .isExactlyInstanceOf(MemberAlreadyDisabledException.class);
    }
}