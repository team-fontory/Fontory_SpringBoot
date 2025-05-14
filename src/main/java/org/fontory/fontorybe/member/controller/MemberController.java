package org.fontory.fontorybe.member.controller;

import org.fontory.fontorybe.authentication.adapter.inbound.annotation.Login;
import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.fontory.fontorybe.file.application.port.CloudStorageService;
import org.fontory.fontorybe.member.controller.dto.ProfileResponse;
import org.fontory.fontorybe.member.controller.port.MemberLookupService;
import org.fontory.fontorybe.member.domain.Member;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@Tag(name = "사용자 - 정보조회", description = "다른 회원 정보 조회")
@Builder
@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
    private final CloudStorageService cloudStorageService;
    private final MemberLookupService memberLookupService;

    @GetMapping("/{id}")
    public ResponseEntity<ProfileResponse> getInfoMember(
            @Login UserPrincipal me,
            @PathVariable Long id
    ) {
        Long requestMemberId = me.getId();
        log.info("Request received: Get member info ID: {} by member ID: {}", id, requestMemberId);

        Member targetMember = memberLookupService.getOrThrowById(id);
        String fileUrl = cloudStorageService.getProfileImageUrl(targetMember.getProfileImageKey());
        log.info("ProfileImageUrl generated : {}", fileUrl);

        ProfileResponse profileResponse = ProfileResponse.from(targetMember, fileUrl);
        log.info("Response sent: ProfileResponse : {}", profileResponse);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(profileResponse);
    }
}
