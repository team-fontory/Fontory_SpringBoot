package org.fontory.fontorybe.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fontory.fontorybe.authentication.adapter.inbound.annotation.Login;
import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.fontory.fontorybe.file.application.port.CloudStorageService;
import org.fontory.fontorybe.file.application.port.FileService;
import org.fontory.fontorybe.file.domain.FileUploadResult;
import org.fontory.fontorybe.file.application.annotation.SingleFileUpload;
import org.fontory.fontorybe.member.controller.dto.InitMemberInfoRequest;
import org.fontory.fontorybe.member.controller.dto.MemberCreateResponse;
import org.fontory.fontorybe.member.controller.port.MemberLookupService;
import org.fontory.fontorybe.member.controller.port.MemberOnboardService;
import org.fontory.fontorybe.member.domain.Member;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.fontory.fontorybe.file.adapter.inbound.validator.MultipartFileValidator.extractSingleMultipartFile;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@Slf4j
@Builder
@RestController
@RequiredArgsConstructor
@RequestMapping("/register")
@Tag(name = "사용자 - 신규", description = "회원가입/온보딩")
public class RegistrationController {
    private final CloudStorageService cloudStorageService;
    private final MemberLookupService memberLookupService;
    private final MemberOnboardService memberOnboardService;
    private final FileService fileService;

    @Operation(
            summary = "닉네임 중복 확인",
            description = "주어진 닉네임이 이미 사용 중인지 확인합니다. true면 중복, false면 사용 가능합니다."
    )
    @GetMapping("/check-duplicate")
    public ResponseEntity<Boolean> checkDuplicate(
            @RequestParam @Parameter(description = "중복 확인할 닉네임") String nickname) {
        log.info("Request received: Check if nickname is duplicate: {}", nickname);

        boolean duplicateNameExists = memberLookupService.existsByNickname(nickname);
        log.info("Response sent: Nickname {} is {}", nickname, duplicateNameExists ? "duplicate" : "available");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(duplicateNameExists);
    }

    @Operation(
            summary = "회원가입 완료",
            description = "온보딩 상태의 회원을 새로운 회원으로 등록하고 초기 정보를 설정합니다."
    )
    @PostMapping
    public ResponseEntity<MemberCreateResponse> register(
            @Login UserPrincipal user,
            @RequestBody @Valid @Parameter(description = "초기 회원 정보") InitMemberInfoRequest req
    ) {
        Long requestMemberId = user.getId();
        log.info("Request received: Create member ID: {} with request: {}",
                requestMemberId, req);

        Member updatedMember = memberOnboardService.initNewMemberInfo(requestMemberId, req);

        log.info("Response sent: Member ID: {} Created successfully with nickname: {}",
                updatedMember.getId(), updatedMember.getNickname());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(MemberCreateResponse.from(updatedMember));
    }
}
