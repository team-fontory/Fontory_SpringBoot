package org.fontory.fontorybe.integration.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import org.fontory.fontorybe.member.controller.port.MemberService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.domain.dto.MemberCreateDto;
import org.fontory.fontorybe.member.domain.dto.MemberUpdateDto;
import org.fontory.fontorybe.member.infrastructure.entity.Gender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;

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
//                        ....
        );
    }

    @Test
    @DisplayName("member - getOrThrownById fail test caused by not found")
    void getOrThrownByIdTestX() {
        assertThatThrownBy(
                () -> memberService.getOrThrowById(nonExistentId)
        ).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("member - create success test")
    void createTest() {
        MemberCreateDto memberCreateDto = new MemberCreateDto(testNickName, testGender, testBirth, testTerms, testProfileImage);
        Member createdMember = memberService.create(memberCreateDto, testProvideId);

        assertAll(
                () -> assertThat(createdMember.getId()).isNotNull(),
                () -> assertThat(createdMember.getProvideId()).isEqualTo(testProvideId),
                () -> assertThat(createdMember.getNickname()).isEqualTo(testNickName),
                () -> assertThat(createdMember.getGender()).isEqualTo(testGender),
                () -> assertThat(createdMember.getBirth()).isEqualTo(testBirth),
                () -> assertThat(createdMember.isTerms()).isEqualTo(testTerms),
                () -> assertThat(createdMember.getProfileImage()).isEqualTo(testProfileImage),
                () -> assertThat(createdMember.getCreatedAt()).isNotNull(),
                () -> assertThat(createdMember.getUpdatedAt()).isNotNull()
        );
    }

    @Test
    @DisplayName("member - create fail test caused by provide Not found")
    void createTestX() {
        MemberCreateDto memberCreateDto = new MemberCreateDto(testNickName, testGender, testBirth, testTerms, testProfileImage);
        assertThatThrownBy(
                () -> memberService.create(memberCreateDto, nonExistentId))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("member - update success")
    void updateTest() {
        Member member = memberService.getOrThrowById(testMemberId);
        MemberUpdateDto memberUpdateDto = new MemberUpdateDto(updateNickName, updateGender, updateBirth, updateTerms, updateProfileImage);
        Member updatedMember = memberService.update(testMemberId, memberUpdateDto);
        assertAll(
                () -> assertThat(updatedMember.getId()).isEqualTo(member.getId()),
                () -> assertThat(updatedMember.getNickname()).isEqualTo(updateNickName),
                () -> assertThat(updatedMember.getGender()).isEqualTo(updateGender),
                () -> assertThat(updatedMember.getBirth()).isEqualTo(updateBirth),
                () -> assertThat(updatedMember.getProfileImage()).isEqualTo(updateProfileImage),
                () -> assertThat(updatedMember.getProvideId()).isEqualTo(member.getProvideId()),
                () -> assertThat(updatedMember.getCreatedAt()).isEqualTo(member.getCreatedAt()),
                () -> assertThat(updatedMember.getUpdatedAt()).isAfter(member.getUpdatedAt())
        );
    }

}
