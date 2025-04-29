package org.fontory.fontorybe.unit.member.controller;

import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.fontory.fontorybe.file.application.port.CloudStorageService;
import org.fontory.fontorybe.member.controller.MemberController;
import org.fontory.fontorybe.member.controller.dto.ProfileResponse;
import org.fontory.fontorybe.member.controller.port.MemberOnboardService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.domain.exception.MemberNotFoundException;
import org.fontory.fontorybe.unit.mock.TestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.fontory.fontorybe.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertAll;

class MemberControllerTest {
    MemberController memberController;
    MemberOnboardService memberOnboardService;
    CloudStorageService cloudStorageService;

    /**
     * testValues
     */
    TestContainer testContainer;
    Member testMember;
    UserPrincipal testMemberUserPrincipal;

    @BeforeEach
    void init() {
        testContainer = new TestContainer();
        memberController = testContainer.memberController;
        memberOnboardService = testContainer.memberOnboardService;
        cloudStorageService = testContainer.cloudStorageService;

        testMember = testContainer.createTestMember();
        testMemberUserPrincipal = UserPrincipal.from(testMember);
    }

    @Test
    @DisplayName("fails inquiring not exist member")
    void getInfoNotExistsMemberFailTest() {
        //given
        //when & then
        assertThatThrownBy(
                () -> memberController.getInfoMember(testMemberUserPrincipal, NON_EXIST_ID)
        ).isExactlyInstanceOf(MemberNotFoundException.class);
    }

    @Test
    @DisplayName("Success inquiring exist member")
    void getInfoMemberSuccessTest() {
        //given
        Member newMember = testContainer.createNewMember();
        UserPrincipal newMemberUserPrincipal = UserPrincipal.from(newMember);

        //when
        ResponseEntity<ProfileResponse> response = memberController.getInfoMember(newMemberUserPrincipal, testMember.getId());

        //then
        ProfileResponse body = response.getBody();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(body).isNotNull();

        assertAll(
                () -> assertThat(body.getMemberId()).isEqualTo(testMember.getId()),
                () -> assertThat(body.getProfileImageUrl()).isEqualTo(cloudStorageService.getProfileImageUrl(testMember.getProfileImageKey())),
                () -> assertThat(body.getNickname()).isEqualTo(testMember.getNickname())
        );
    }
}
