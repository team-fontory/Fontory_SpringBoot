package org.fontory.fontorybe.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.authentication.adapter.inbound.Login;
import org.fontory.fontorybe.authentication.application.AuthService;
import org.fontory.fontorybe.member.controller.dto.*;
import org.fontory.fontorybe.member.controller.port.MemberService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.fontory.fontorybe.authentication.adapter.outbound.JwtTokenProvider;
import org.fontory.fontorybe.authentication.adapter.inbound.dto.TokenResponse;
import org.fontory.fontorybe.provide.controller.port.ProvideService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(name = "회원관리", description = "사용자 API")
@Builder
@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final ProvideService provideService;
    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(
            summary = "닉네임 중복 확인",
            description = "주어진 닉네임으로 생성되어있는 사용자가 있는지 검색 후 결과를 반환합니다."
    )
    @GetMapping("/check-duplicate")
    public ResponseEntity<Boolean> checkDuplicate(
            @RequestParam String nickname
    ) {
        Boolean duplicateNameExists = memberService.isDuplicateNameExists(nickname);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(duplicateNameExists);
    }

    @Operation(
            summary = "회원가입"
    )
    @PostMapping
    public ResponseEntity<MemberCreateResponse> addMember(
            @RequestBody MemberCreateRequest memberCreateRequest
    ) {
        Member createdMember = memberService.create(memberCreateRequest);
        TokenResponse tokens = authService.generateTokens(createdMember);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(MemberCreateResponse.from(createdMember, tokens));
    }

    @Operation(
            summary = "회원정보 수정"
    )
    @PutMapping
    public ResponseEntity<MemberUpdateResponse> updateMember(
        @RequestBody MemberUpdateRequest memberUpdateRequest,
        @Login UserPrincipal userPrincipal
    ) {
        Long requestMemberId = userPrincipal.getId();

        Member updatedMember = memberService.update(requestMemberId, memberUpdateRequest);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(MemberUpdateResponse.from(updatedMember));
    }

    @Operation(
            summary = "회원탈퇴"
    )
    @DeleteMapping
    public ResponseEntity<MemberDisableResponse> disableMember(
            @Login UserPrincipal userPrincipal
    ) {
        Long requestMemberId = userPrincipal.getId();

        Member disabledMember = memberService.disable(requestMemberId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(MemberDisableResponse.from(disabledMember));
    }
}
