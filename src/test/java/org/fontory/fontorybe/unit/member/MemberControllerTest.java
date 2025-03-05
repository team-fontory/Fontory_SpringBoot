package org.fontory.fontorybe.unit.member;

import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.fontory.fontorybe.member.controller.MemberController;
import org.fontory.fontorybe.member.controller.dto.*;
import org.fontory.fontorybe.member.domain.Member;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

public class MemberControllerTest {
    MemberController memberController;

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
        memberController = MemberController.builder()
                .memberService(testContainer.memberService)
                .provideService(testContainer.provideService)
                .jwtTokenProvider(testContainer.jwtTokenProvider)
                .authService(testContainer.authService)
                .build();

        ProvideCreateDto provideCreateDto = new ProvideCreateDto(existMemberProvider, existMemberProvidedId, existMemberEmail);
        Provide createdProvide = testContainer.provideService.create(provideCreateDto);
        MemberCreateRequest memberCreateRequest = new MemberCreateRequest(existMemberNickName, existMemberGender, existMemberBirth, existMemberTerms, existMemberProfileImage);
        Member createdMember = testContainer.memberService.create(memberCreateRequest, createdProvide);
        existMemberId = createdMember.getId();
        existMemberProvideId = createdProvide.getId();
        userPrincipal = UserPrincipal.from(createdMember);
    }

    @Test
    @DisplayName("checkDuplicate returns true when duplicate exists")
    void testCheckDuplicateTrue() {
        //given
        String nickname = existMemberNickName;

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
        ProvideCreateDto provideCreateDto = new ProvideCreateDto(newMemberProvider, newMemberProvidedId, newMemberEmail);
        Provide createdProvide = testContainer.provideService.create(provideCreateDto);
        MemberCreateRequest memberCreateRequest = new MemberCreateRequest(newMemberNickName, newMemberGender, newMemberBirth, newMemberTerms, newMemberProfileImage);
        //when
        ResponseEntity<MemberCreateResponse> response = memberController.addMember(createdProvide, memberCreateRequest);
        MemberCreateResponse body = response.getBody();
        //then
        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED),
                () -> assertThat(body).isNotNull(),
                () -> assertThat(body.getAccessToken()).isNotNull(),
                () -> assertThat(body.getRefreshToken()).isNotNull()
        );
    }


    @Test
    @DisplayName("updateMember returns updated member response")
    void testUpdateMember() {
        //given
        MemberUpdateRequest memberUpdateRequest = new MemberUpdateRequest(
                updateNickName,
                updateProfileImage,
                updateTerms
        );
        //when
        ResponseEntity<MemberUpdateResponse> response = memberController.updateMember(memberUpdateRequest, userPrincipal);
        MemberUpdateResponse body = response.getBody();
        //then
        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(body).isNotNull(),
                () -> assertThat(body.getNickname()).isEqualTo(updateNickName),
                () -> assertThat(body.getTerms()).isEqualTo(updateTerms),
                () -> assertThat(body.getProfileImage()).isEqualTo(updateProfileImage)
        );
    }

    @Test
    @DisplayName("disableMember returns disabled member response")
    void testDisableMember() {
        //given
        //when
        ResponseEntity<MemberDisableResponse> response = memberController.disableMember(userPrincipal);
        MemberDisableResponse body = response.getBody();
        //then
        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(body).isNotNull(),
                () -> assertThat(body.getDeletedAt()).isNotNull()
        );
    }

    @Test
    @DisplayName("updateMember fails when member not found")
    void updateMemberNonExistentTest() {
        // given: UserPrincipal의 id를 조작해 존재하지 않는 회원 id로 만듦
        UserPrincipal nonExistentUser = new UserPrincipal(nonExistentId);
        MemberUpdateRequest memberUpdateRequest = new MemberUpdateRequest(updateNickName, updateProfileImage, updateTerms);
        // when & then: MemberNotFoundException 발생 예상 (글로벌 예외 핸들러가 있다면 그에 맞는 응답이 내려갈 것)
        assertThatThrownBy(() -> memberController.updateMember(memberUpdateRequest, nonExistentUser))
                .isInstanceOf(MemberNotFoundException.class);
    }

    @Test
    @DisplayName("disableMember fails when member is already disabled")
    void disableAlreadyDisabledMemberTest() {
        // given: 먼저 정상적으로 회원 비활성화 처리
        memberController.disableMember(userPrincipal);
        // when & then: 이미 비활성화된 회원을 다시 비활성화 요청 시 예외 발생
        assertThatThrownBy(() -> memberController.disableMember(userPrincipal))
                .isInstanceOf(RuntimeException.class);  // 실제 예외 타입(MemberAlreadyDisabledException 등)을 지정
    }
}
