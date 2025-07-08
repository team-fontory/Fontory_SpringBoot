package org.fontory.fontorybe.unit.member.controller;

import org.fontory.fontorybe.TestConstants;
import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.fontory.fontorybe.member.controller.RegistrationController;
import org.fontory.fontorybe.member.controller.dto.InitMemberInfoRequest;
import org.fontory.fontorybe.member.controller.dto.MemberCreateResponse;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.unit.mock.TestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class RegistrationControllerTest {
    RegistrationController registrationController;
    InitMemberInfoRequest newInitMemberInfoRequest;
    TestContainer testContainer;
    String existMemberNickName;

    @BeforeEach
    void init() {
        testContainer = new TestContainer();
        registrationController = testContainer.registrationController;
        newInitMemberInfoRequest = testContainer.newInitMemberInfoRequest;
        existMemberNickName = TestConstants.TEST_MEMBER_NICKNAME;
        testContainer.createTestMember();
    }

    @Test
    @DisplayName("checkDuplicate returns true when duplicate exists")
    void testCheckDuplicateTrue() {
        //given
        String nickname = existMemberNickName;

        //when
        ResponseEntity<Boolean> response = registrationController.checkDuplicate(nickname);
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
        ResponseEntity<Boolean> response = registrationController.checkDuplicate(nickname);
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
        Member notInitedMember = testContainer.createNotInitedMember();

        //when
        ResponseEntity<MemberCreateResponse> response = registrationController.register(UserPrincipal.from(notInitedMember), newInitMemberInfoRequest);
        MemberCreateResponse body = response.getBody();

        //then
        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED),
                () -> assertThat(body).isNotNull()
        );
    }
}
