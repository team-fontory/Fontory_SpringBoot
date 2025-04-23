package org.fontory.fontorybe.member.controller;

import org.fontory.fontorybe.authentication.adapter.inbound.annotation.Login;
import org.fontory.fontorybe.authentication.adapter.inbound.annotation.OAuth2;
import org.fontory.fontorybe.authentication.application.dto.TokenResponse;
import org.fontory.fontorybe.authentication.application.AuthService;
import org.fontory.fontorybe.authentication.application.port.JwtTokenProvider;
import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.fontory.fontorybe.member.controller.dto.MemberCreateRequest;
import org.fontory.fontorybe.member.controller.dto.MemberCreateResponse;
import org.fontory.fontorybe.member.controller.dto.MemberDisableResponse;
import org.fontory.fontorybe.member.controller.dto.MemberUpdateRequest;
import org.fontory.fontorybe.member.controller.dto.MemberUpdateResponse;
import org.fontory.fontorybe.member.controller.port.MemberService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.provide.controller.port.ProvideService;
import org.fontory.fontorybe.provide.domain.Provide;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
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
    private final ObjectMapper objectMapper;

    /**
     * Convert an object to JSON string for logging
     * If conversion fails, falls back to toString()
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Failed to convert object to JSON for logging: {}", e.getMessage());
            return obj.toString();
        }
    }

    @Operation(
            summary = "닉네임 중복 확인",
            description = "주어진 닉네임으로 생성되어있는 사용자가 있는지 검색 후 결과를 반환합니다."
    )
    @GetMapping("/check-duplicate")
    public ResponseEntity<Boolean> checkDuplicate(
            @RequestParam String nickname
    ) {
        log.info("Request received: Check if nickname is duplicate: {}", nickname);
        
        boolean duplicateNameExists = memberService.isDuplicateNameExists(nickname);
        log.info("Response sent: Nickname {} is {}", nickname, duplicateNameExists ? "duplicate" : "available");
        
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(duplicateNameExists);
    }

    @Operation(
            summary = "회원가입"
    )
    @PostMapping
    public ResponseEntity<MemberCreateResponse> addMember(
            @OAuth2 Provide provide,
            @RequestBody MemberCreateRequest memberCreateRequest
    ) {
        log.info("Request received: Create member with request: {} and provider: {}", 
                toJson(memberCreateRequest), provide.getProvider());
        
        Member createdMember = memberService.create(memberCreateRequest, provide);
        TokenResponse tokens = authService.issueNewTokens(createdMember);
        
        log.info("Response sent: Member created with ID: {}, nickname: {}", 
                createdMember.getId(), createdMember.getNickname());
        
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
        log.info("Request received: Update member ID: {} with request: {}", 
                requestMemberId, toJson(memberUpdateRequest));

        Member updatedMember = memberService.update(requestMemberId, memberUpdateRequest);
        log.info("Response sent: Member ID: {} updated successfully with nickname: {}", 
                updatedMember.getId(), updatedMember.getNickname());
        
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
        log.info("Request received: Disable member ID: {}", requestMemberId);

        Member disabledMember = memberService.disable(requestMemberId);
        log.info("Response sent: Member ID: {} disabled successfully", disabledMember.getId());
        
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(MemberDisableResponse.from(disabledMember));
    }
}
