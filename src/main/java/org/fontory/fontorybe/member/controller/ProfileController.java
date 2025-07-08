package org.fontory.fontorybe.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fontory.fontorybe.authentication.adapter.inbound.annotation.Login;
import org.fontory.fontorybe.authentication.application.AuthService;
import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.fontory.fontorybe.file.application.port.CloudStorageService;
import org.fontory.fontorybe.file.application.port.FileService;
import org.fontory.fontorybe.file.domain.FileUploadResult;
import org.fontory.fontorybe.file.application.annotation.SingleFileUpload;
import org.fontory.fontorybe.member.controller.dto.MemberDisableResponse;
import org.fontory.fontorybe.member.controller.dto.MemberUpdateRequest;
import org.fontory.fontorybe.member.controller.dto.MyProfileResponse;
import org.fontory.fontorybe.member.controller.port.MemberLookupService;
import org.fontory.fontorybe.member.controller.port.MemberUpdateService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.domain.exception.MemberNotFoundException;
import org.fontory.fontorybe.member.infrastructure.entity.MemberStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.fontory.fontorybe.file.adapter.inbound.validator.MultipartFileValidator.extractSingleMultipartFile;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@Slf4j
@Builder
@RestController
@RequiredArgsConstructor
@RequestMapping("/member/me")
@Tag(name = "사용자 - 내정보", description = " 내 프로필 조회,수정,탈퇴")
public class ProfileController {
    private final AuthService authService;
    private final FileService fileService;
    private final CloudStorageService cloudStorageService;
    private final MemberUpdateService memberUpdateService;
    private final MemberLookupService memberLookupService;

    @Operation(
            summary = "내정보",
            description = "쿠키를 바탕으로 내정보를 조회"
    )
    @GetMapping
    public ResponseEntity<MyProfileResponse> getMyProfile(
            @Login(required = false) UserPrincipal me) {
        if (me == null) {
            log.info("Request received: getMyInfo - no login");
            throw new MemberNotFoundException();
        }
        Long requestMemberId = me.getId();
        log.info("Request received: getMyInfo member ID: {}", requestMemberId);

        Member lookupMember = memberLookupService.getOrThrowById(requestMemberId);
        if (lookupMember.getStatus().equals(MemberStatus.ONBOARDING)) {
            log.info("Request received: getMyInfo - member is onboarding");
            throw new MemberNotFoundException();
        }

        MyProfileResponse myProfileResponse = MyProfileResponse.from(lookupMember);
        log.info("Response sent: MyProfileDto : {}", myProfileResponse);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(myProfileResponse);
    }


    @Operation(
            summary = "내정보 수정"
    )
    @PatchMapping
    public ResponseEntity<MyProfileResponse> updateMember(
            @Login UserPrincipal userPrincipal,
            @RequestBody @Valid MemberUpdateRequest req
    ) {
        Long requestMemberId = userPrincipal.getId();
        log.info("Request received: update member ID: {} with request: {}",
                requestMemberId, req);

        Member updatedMember = memberUpdateService.update(requestMemberId, req);
        log.info("Updated : Member ID: {} Updated successfully with nickname: {}", updatedMember.getId(), updatedMember.getNickname());

        MyProfileResponse myProfileResponse = MyProfileResponse.from(updatedMember);
        log.info("Response sent: MyProfileDto : {}", myProfileResponse);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(myProfileResponse);
    }

    @Operation(
            summary = "회원탈퇴"
    )
    @DeleteMapping
    public ResponseEntity<MemberDisableResponse> disableMember(
            HttpServletResponse res,
            @Login UserPrincipal me
    ) {
        Long requestMemberId = me.getId();
        log.info("Request received: Disable member ID: {}", requestMemberId);

        Member disabledMember = memberUpdateService.disable(requestMemberId);
        log.info("Member {} disabled", disabledMember.getId());

        authService.clearAuthCookies(res, disabledMember.getId());
        log.info("Clear JWT Cookies");

        MemberDisableResponse memberDisableResponse = MemberDisableResponse.from(disabledMember);
        log.info("Response sent MemberDisableResponse : {}", memberDisableResponse);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(memberDisableResponse);
    }

    private void logFileDetails(MultipartFile file, String context) {
        log.debug("{} - File details: name='{}', original name='{}', size={} bytes, contentType='{}'",
                context,
                file.getName(),
                file.getOriginalFilename(),
                file.getSize(),
                file.getContentType());
    }
}
