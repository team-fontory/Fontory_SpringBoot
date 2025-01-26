package org.fontory.fontorybe.integration.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import org.fontory.fontorybe.member.controller.port.MemberService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.domain.dto.MemberCreateDto;
import org.fontory.fontorybe.member.infrastructure.entity.Gender;
import org.fontory.fontorybe.provide.controller.port.ProvideService;
import org.fontory.fontorybe.provide.domain.Provide;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;

@SpringBootTest
@SqlGroup({
        @Sql(value = "/sql/createMemberTestData.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/deleteMemberTestData.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD),
})
public class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired ProvideService provideService;

    Long testProvideId = 1L;
    Gender testGender = Gender.MALE;
    LocalDate testBirth = LocalDate.of(2025, 1, 26);
    Boolean testTerms = Boolean.TRUE;
    String testNickName = "testNickName";
    String testProfileImage = "testProfileImage";

    @Test
    @DisplayName("member - getById success test")
    public void getByIdTest() {
        MemberCreateDto memberCreateDto = new MemberCreateDto(testNickName, testGender, testBirth, testTerms, testProfileImage);
        Provide provide = provideService.getById(1L);

        Member createdMember = memberService.create(memberCreateDto, provide);

        assertAll(
                () -> assertThat(createdMember.getId()).isNotNull(),
                () -> assertThat(createdMember.getProvideId()).isEqualTo(testProvideId),
                () -> assertThat(createdMember.getNickname()).isEqualTo(testNickName),
                () -> assertThat(createdMember.getGender()).isEqualTo(testGender),
                () -> assertThat(createdMember.getBirth()).isEqualTo(testBirth),
                () -> assertThat(createdMember.isTerms()).isEqualTo(testTerms),
                () -> assertThat(createdMember.getProfileImage()).isEqualTo(testProfileImage)
//                () -> assertThat(createdMember.getCreatedAt()).isNotNull(),
        );
    }
}
