package org.fontory.fontorybe.integration.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.util.UUID;

import org.fontory.fontorybe.member.controller.dto.MemberCreate;
import org.fontory.fontorybe.member.controller.port.MemberService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.controller.dto.MemberUpdate;
import org.fontory.fontorybe.member.domain.exception.MemberAlreadyDisabledException;
import org.fontory.fontorybe.member.domain.exception.MemberDuplicateNameExistsException;
import org.fontory.fontorybe.member.domain.exception.MemberNotFoundException;
import org.fontory.fontorybe.member.domain.exception.MemberOwnerMismatchException;
import org.fontory.fontorybe.member.infrastructure.entity.Gender;
import org.fontory.fontorybe.provide.domain.exception.ProvideNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

@SpringBootTest
@Sql(value = "/sql/createMemberTestData.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/sql/deleteMemberTestData.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class MemberServiceTest {

    @Autowired MemberService memberService;

    /**
     * testValues
     */
    Long nonExistentId = -1L;
    Long testMemberId = 999L;
    Long testProvideId = 1L;
    Gender testGender = Gender.MALE;
    LocalDate testBirth = LocalDate.of(2025, 1, 26);
    Boolean testTerms = Boolean.TRUE;
    String testNickName = "testNickName";
    String testProfileImage = "testProfileImage";

    Gender updateGender = Gender.FEMALE;
    LocalDate updateBirth = LocalDate.of(2025, 1, 27);
    Boolean updateTerms = Boolean.FALSE;
    String updateNickName = "updateNickName";
    String updateProfileImage = "updateProfileImage";



    @Test
    @DisplayName("member - getOrThrownById success test")
    void getOrThrownByIdTest() {
        Member foundMember = memberService.getOrThrowById(testMemberId);
        assertAll(
                () -> assertThat(foundMember.getId()).isEqualTo(testMemberId)
                // 기타 필요한 검증 추가
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
        MemberCreate memberCreateDto = new MemberCreate(testNickName, testGender, testBirth, testTerms, testProfileImage);
        Member createdMember = memberService.create(memberCreateDto, testProvideId);

        assertAll(
                () -> assertThat(createdMember.getId()).isNotNull(),
                () -> assertThat(createdMember.getProvideId()).isEqualTo(testProvideId),
                () -> assertThat(createdMember.getNickname()).isEqualTo(testNickName),
                () -> assertThat(createdMember.getGender()).isEqualTo(testGender),
                () -> assertThat(createdMember.getBirth()).isEqualTo(testBirth),
                () -> assertThat(createdMember.getTerms()).isEqualTo(testTerms),
                () -> assertThat(createdMember.getProfileImage()).isEqualTo(testProfileImage),
                () -> assertThat(createdMember.getCreatedAt()).isNotNull(),
                () -> assertThat(createdMember.getUpdatedAt()).isNotNull()
        );
    }

    @Test
    @DisplayName("member - create fail test caused by provide Not found")
    void createTestX() {
        MemberCreate memberCreateDto = new MemberCreate(testNickName, testGender, testBirth, testTerms, testProfileImage);
        assertThatThrownBy(
                () -> memberService.create(memberCreateDto, nonExistentId))
                .isExactlyInstanceOf(ProvideNotFoundException.class);
    }

    @Test
    @DisplayName("member - create fail test caused by duplicate nickname")
    void createDuplicateNicknameTest() {
        // 회원 생성
        MemberCreate memberCreateDto = new MemberCreate(testNickName, testGender, testBirth, testTerms, testProfileImage);
        memberService.create(memberCreateDto, testProvideId);

        // 동일 닉네임으로 또 회원 생성 시 예외 발생
        MemberCreate duplicateMemberCreateDto = new MemberCreate(testNickName, testGender, testBirth, testTerms, testProfileImage);
        assertThatThrownBy(
                () -> memberService.create(duplicateMemberCreateDto, testProvideId))
                .isExactlyInstanceOf(MemberDuplicateNameExistsException.class);
    }

    @Test
    @DisplayName("member - update success")
    void updateTest() {
        Long requestMemberId = testMemberId;
        Member member = memberService.getOrThrowById(testMemberId);
        MemberUpdate memberUpdate = new MemberUpdate(updateNickName, updateProfileImage, updateTerms);

        Member updatedMember = memberService.update(requestMemberId, testMemberId, memberUpdate);
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
        String uniqueNickname1 = UUID.randomUUID().toString();
        String uniqueNickname2 = UUID.randomUUID().toString();
        MemberCreate memberCreateDto1 = new MemberCreate(uniqueNickname1, testGender, testBirth, testTerms, testProfileImage);
        MemberCreate memberCreateDto2 = new MemberCreate(uniqueNickname2, testGender, testBirth, testTerms, testProfileImage);
        Member member1 = memberService.create(memberCreateDto1, testProvideId);
        memberService.create(memberCreateDto2, testProvideId);

        // member1의 닉네임을 이미 존재하는 이름으로 업데이트 시도하면 예외 발생
        MemberUpdate memberUpdate = new MemberUpdate(uniqueNickname2, updateProfileImage, updateTerms);
        assertThatThrownBy(
                () -> memberService.update(member1.getId(), member1.getId(), memberUpdate))
                .isExactlyInstanceOf(MemberDuplicateNameExistsException.class);
    }

    @Test
    @DisplayName("member - update fail test caused by member owner mismatch")
    void updateOwnerMismatchTest() {
        // 회원 생성
        MemberCreate memberCreateDto = new MemberCreate(testNickName, testGender, testBirth, testTerms, testProfileImage);
        Member member = memberService.create(memberCreateDto, testProvideId);

        // 요청 회원 ID가 대상 회원 ID와 다를 경우 예외 발생
        MemberUpdate memberUpdate = new MemberUpdate(updateNickName, updateProfileImage, updateTerms);
        Long wrongOwnerId = member.getId() + 1000; // 임의의 다른 ID
        assertThatThrownBy(
                () -> memberService.update(wrongOwnerId, member.getId(), memberUpdate))
                .isExactlyInstanceOf(MemberOwnerMismatchException.class);
    }

    @Test
    @DisplayName("member - update fail test caused by member not found")
    void updateNonExistentMemberTest() {
        // 존재하지 않는 회원(-1L)을 대상으로 업데이트 시도 시 예외 발생
        MemberUpdate memberUpdate = new MemberUpdate(updateNickName, updateProfileImage, updateTerms);
        assertThatThrownBy(
                () -> memberService.update(testMemberId, nonExistentId, memberUpdate))
                .isExactlyInstanceOf(MemberNotFoundException.class);
    }

    @Test
    @DisplayName("member - disable success test")
    void disableTest() {
        // 회원 생성
        MemberCreate memberCreateDto = new MemberCreate(testNickName, testGender, testBirth, testTerms, testProfileImage);
        Member member = memberService.create(memberCreateDto, testProvideId);

        // 회원 비활성화
        Member disabledMember = memberService.disable(member.getId(), member.getId());
        assertThat(disabledMember.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("member - disable fail test caused by already disabled member")
    void disableAlreadyDisabledTest() {
        // 회원 생성
        MemberCreate memberCreateDto = new MemberCreate(testNickName, testGender, testBirth, testTerms, testProfileImage);
        Member member = memberService.create(memberCreateDto, testProvideId);

        // 회원 비활성화
        memberService.disable(member.getId(), member.getId());

        // 비활성화 회원 다시 비활성화시 에러발생
        assertThatThrownBy(
                () -> memberService.disable(member.getId(), member.getId()))
                .isExactlyInstanceOf(MemberAlreadyDisabledException.class);
    }

    @Test
    @DisplayName("member - disable fail test caused by member owner mismatch")
    void disableOwnerMismatchTest() {
        // 회원 생성 후, 다른 사용자로 disable 시도 시 예외 발생
        MemberCreate memberCreateDto = new MemberCreate(testNickName, testGender, testBirth, testTerms, testProfileImage);
        Member member = memberService.create(memberCreateDto, testProvideId);

        Long wrongOwnerId = member.getId() + 1000; // 임의의 다른 ID
        assertThatThrownBy(
                () -> memberService.disable(wrongOwnerId, member.getId()))
                .isExactlyInstanceOf(MemberOwnerMismatchException.class);
    }
}
