package org.fontory.fontorybe.unit.member;

import org.fontory.fontorybe.member.controller.MemberController;
import org.fontory.fontorybe.member.controller.dto.*;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.infrastructure.entity.Gender;
import org.fontory.fontorybe.unit.mock.TestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class MemberControllerTest {
    MemberController memberController;

    /**
     * testValues
     */
    Long nonExistentId = -1L;
    Long testMemberId = 999L;
    Long testProvideId = 1L;
    Gender testGender = Gender.MALE;
    LocalDate testBirth = LocalDate.of(2025, 1, 26);
    Boolean testTerms = Boolean.TRUE;
    String testNickName = "testNickname";
    String testProfileImage = "testProfileImage";

    Boolean updateTerms = Boolean.FALSE;
    String updateNickName = "updateNickName";
    String updateProfileImage = "updateProfileImage";

    @BeforeEach
    void init() {
        TestContainer testContainer = new TestContainer();
        memberController = MemberController.builder()
                .memberService(testContainer.memberService)
                .provideService(testContainer.provideService)
                .build();

        LocalDateTime now = LocalDateTime.now();
        testContainer.memberRepository.save(
                Member.builder()
                        .id(testMemberId)
                        .nickname(testNickName)
                        .gender(testGender)
                        .birth(testBirth)
                        .terms(testTerms)
                        .profileImage(testProfileImage)
                        .provideId(testProvideId)
                        .createdAt(now)
                        .updatedAt(now)
                        .build());
    }

    @Test
    @DisplayName("checkDuplicate returns true when duplicate exists")
    void testCheckDuplicateTrue() {
        //given
        String nickname = testNickName;

        //when
        ResponseEntity<Boolean> response = memberController.checkDuplicate(nickname);
        Boolean body = response.getBody();

        //then
        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(body).isTrue()
        );
    }

    @Test
    @DisplayName("checkDuplicate returns false when duplicate does not exist")
    void testCheckDuplicateFalse() {
        //given
        String nickname = UUID.randomUUID().toString();
        //when
        ResponseEntity<Boolean> response = memberController.checkDuplicate(nickname);
        Boolean body = response.getBody();
        //then
        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(body).isFalse()
        );
    }

    @Test
    @DisplayName("addMember returns created member response")
    void testAddMember() {
        //given
        MemberCreate memberCreate = new MemberCreate(
                "newMember",
                Gender.MALE,
                LocalDate.parse("2025-01-26"),
                true,
                "newProfileImage"
        );
        //when
        ResponseEntity<MemberCreateResponse> response = memberController.addMember(memberCreate);
        MemberCreateResponse body = response.getBody();
        //then
        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED),
                () -> assertThat(body).isNotNull(),
                () -> assertThat(body.getId()).isNotNull(),
                () -> assertThat(body.getNickname()).isEqualTo("newMember")
        );
    }


    @Test
    @DisplayName("updateMember returns updated member response")
    void testUpdateMember() {
        //given
        MemberUpdate memberUpdate = new MemberUpdate(
                "updatedNick",
                "updatedProfileImage",
                false
        );
        //when
        ResponseEntity<MemberUpdateResponse> response = memberController.updateMember(memberUpdate, testMemberId);
        MemberUpdateResponse body = response.getBody();
        //then
        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(body).isNotNull(),
                () -> assertThat(body.getId()).isEqualTo(testMemberId),
                () -> assertThat(body.getNickname()).isEqualTo("updatedNick")
        );
    }

    @Test
    @DisplayName("disableMember returns disabled member response")
    void testDisableMember() {
        //given
        //when
        ResponseEntity<MemberDisableResponse> response = memberController.disableMember(testMemberId);
        MemberDisableResponse body = response.getBody();
        //then
        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(body).isNotNull(),
                () -> assertThat(body.getDeletedAt()).isNotNull()
        );
    }
}
