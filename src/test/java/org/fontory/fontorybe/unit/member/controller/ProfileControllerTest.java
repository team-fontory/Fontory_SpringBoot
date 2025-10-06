package org.fontory.fontorybe.unit.member.controller;

import jakarta.servlet.http.Cookie;
import org.fontory.fontorybe.TestConstants;
import org.fontory.fontorybe.authentication.application.AuthConstants;
import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.fontory.fontorybe.file.application.port.CloudStorageService;
import org.fontory.fontorybe.member.controller.ProfileController;
import org.fontory.fontorybe.member.controller.dto.MemberDisableResponse;
import org.fontory.fontorybe.member.controller.dto.MemberUpdateRequest;
import org.fontory.fontorybe.member.controller.dto.MyProfileResponse;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.domain.exception.MemberAlreadyDisabledException;
import org.fontory.fontorybe.member.domain.exception.MemberNotFoundException;
import org.fontory.fontorybe.member.infrastructure.entity.MemberStatus;
import org.fontory.fontorybe.member.service.port.MemberRepository;
import org.fontory.fontorybe.unit.mock.TestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.fontory.fontorybe.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertAll;

class ProfileControllerTest {
    Member testMember;
    TestContainer testContainer;
    MemberRepository memberRepository;
    ProfileController profileController;
    UserPrincipal testMemberUserPrincipal;
    List<MultipartFile> mockFiles;
    CloudStorageService cloudStorageService;

    @BeforeEach
    void init() {
        testContainer = new TestContainer();
        memberRepository = testContainer.memberRepository;
        profileController = testContainer.profileController;
        cloudStorageService = testContainer.cloudStorageService;
        testMember = testContainer.createTestMember();
        testMemberUserPrincipal = UserPrincipal.from(testMember);
        MockMultipartFile file = new MockMultipartFile(
                "file",              // RequestPart 이름
                "test.png",          // 원본 파일명
                "image/png",         // Content-Type
                "dummy-image-data".getBytes()  // 파일 내용
        );
        mockFiles = Collections.singletonList(file);
    }

    @Test
    @DisplayName("disableMember returns disabled member response")
    void testDisableMember() {
        //given
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        //when
        ResponseEntity<MemberDisableResponse> response = profileController.disableMember(servletResponse, testMemberUserPrincipal);

        //then
        MemberDisableResponse body = response.getBody();
        Cookie clearedAccessTokenCookies = servletResponse.getCookie(AuthConstants.ACCESS_TOKEN_COOKIE_NAME);
        Cookie clearedRefreshTokenCookies = servletResponse.getCookie(AuthConstants.REFRESH_TOKEN_COOKIE_NAME);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(body).isNotNull();
        assertThat(clearedAccessTokenCookies).isNotNull();
        assertThat(clearedRefreshTokenCookies).isNotNull();
        Optional<Member> disabledMember = memberRepository.findById(body.getMemberId());
        assertThat(disabledMember).isNotNull();
        disabledMember.ifPresent(member -> assertThat(member.getStatus()).isEqualTo(MemberStatus.DEACTIVATE));
        disabledMember.ifPresent(member -> assertThat(member.getDeletedAt()).isNotNull());

        assertAll(
                () -> assertThat(clearedAccessTokenCookies.getValue()).isEmpty(),
                () -> assertThat(clearedAccessTokenCookies.getMaxAge()).isZero(),
                () -> assertThat(clearedRefreshTokenCookies.getValue()).isEmpty(),
                () -> assertThat(clearedRefreshTokenCookies.getMaxAge()).isZero(),
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(body.getMemberId()).isEqualTo(testMemberUserPrincipal.getId()),
                () -> assertThat(body).isNotNull(),
                () -> assertThat(body.getDeletedAt()).isNotNull()
        );
    }

    @Test
    @DisplayName("disableMember fails when member is already disabled")
    void disableAlreadyDisabledMemberTest() {
        // given
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        profileController.disableMember(servletResponse, testMemberUserPrincipal);

        // when & then
        assertThatThrownBy(() -> profileController.disableMember(servletResponse, testMemberUserPrincipal))
                .isInstanceOf(MemberAlreadyDisabledException.class);
    }

    @Test
    @DisplayName("updateMember returns updated member response")
    void testUpdateMember() {
        //given
        MemberUpdateRequest memberUpdateRequest = new MemberUpdateRequest(
                UPDATE_MEMBER_NICKNAME
        );

        //when
        ResponseEntity<MyProfileResponse> response = profileController.updateMember(testMemberUserPrincipal, memberUpdateRequest);
        MyProfileResponse body = response.getBody();

        //then
        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(body).isNotNull(),
                () -> assertThat(body.getNickname()).isEqualTo(UPDATE_MEMBER_NICKNAME)
        );
    }

    @Test
    @DisplayName("updateMember fails when member not found")
    void updateMemberNonExistentTest() {
        // given
        UserPrincipal nonExistentUserPrincipal = new UserPrincipal(NON_EXIST_ID);
        MemberUpdateRequest memberUpdateRequest = new MemberUpdateRequest(TestConstants.UPDATE_MEMBER_NICKNAME);

        // when & then
        assertThatThrownBy(() -> profileController.updateMember(nonExistentUserPrincipal, memberUpdateRequest))
                .isInstanceOf(MemberNotFoundException.class);
    }
}
