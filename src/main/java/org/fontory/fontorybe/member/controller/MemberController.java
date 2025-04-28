package org.fontory.fontorybe.member.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.fontory.fontorybe.authentication.adapter.inbound.annotation.Login;
import org.fontory.fontorybe.authentication.application.AuthService;
import org.fontory.fontorybe.authentication.application.port.JwtTokenProvider;
import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.fontory.fontorybe.file.application.port.FileService;
import org.fontory.fontorybe.file.domain.FileUploadResult;
import org.fontory.fontorybe.member.controller.dto.MemberCreateRequest;
import org.fontory.fontorybe.member.controller.dto.MemberCreateResponse;
import org.fontory.fontorybe.member.controller.dto.MemberDisableResponse;
import org.fontory.fontorybe.member.controller.dto.MemberUpdateRequest;
import org.fontory.fontorybe.member.controller.dto.MemberUpdateResponse;
import org.fontory.fontorybe.member.controller.port.MemberOnboardService;
import org.fontory.fontorybe.member.controller.port.MemberService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.provide.controller.port.ProvideService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.fontory.fontorybe.file.adapter.inbound.validator.MultipartFileValidator.extractSingleMultipartFile;


@Slf4j
@Tag(name = "회원관리", description = "사용자 API")
@Builder
@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberOnboardService memberOnboardService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ProvideService provideService;
    private final MemberService memberService;
    private final ObjectMapper objectMapper;
    private final AuthService authService;
    private final FileService fileService;

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
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MemberCreateResponse> addMember(
            @Login UserPrincipal user,
            @RequestPart MemberCreateRequest memberCreateRequest,
            @Parameter(
                    description = "업로드할 파일. 정확히 1개의 파일만 제공되어야 합니다.",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            array = @ArraySchema(
                                    schema = @Schema(type = "string", format = "binary"),
                                    maxItems = 1
                            )
                    )
            )
            @RequestPart("file") List<MultipartFile> files
    ) {
        Long requestMemberId = user.getId();
        MultipartFile file = extractSingleMultipartFile(files);

        log.info("Request received: Create member ID: {} with request: {}",
                requestMemberId, toJson(memberCreateRequest));
        logFileDetails(file, "Member profile image upload");

        FileUploadResult fileUploadResult = fileService.uploadProfileImage(file, requestMemberId);
        Member updatedMember = memberOnboardService.initNewMemberInfo(requestMemberId, memberCreateRequest);

        log.info("Response sent: Member ID: {} Created successfully with nickname: {}",
                updatedMember.getId(), updatedMember.getNickname());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(MemberCreateResponse.from(updatedMember, fileUploadResult.getFileUrl()));
    }

    private void logFileDetails(MultipartFile file, String context) {
        log.debug("{} - File details: name='{}', original name='{}', size={} bytes, contentType='{}'",
                context,
                file.getName(),
                file.getOriginalFilename(),
                file.getSize(),
                file.getContentType());
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
