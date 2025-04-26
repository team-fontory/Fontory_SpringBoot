package org.fontory.fontorybe.file.adapter.inbound;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.fontory.fontorybe.authentication.adapter.inbound.annotation.Login;
import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.fontory.fontorybe.file.adapter.inbound.dto.FileUploadResponse;
import org.fontory.fontorybe.file.application.port.FileService;
import org.fontory.fontorybe.file.domain.FileCreate;
import org.fontory.fontorybe.file.domain.FileUploadResult;
import org.fontory.fontorybe.member.controller.port.MemberService;
import org.fontory.fontorybe.member.domain.Member;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static org.fontory.fontorybe.file.validator.MultipartFileValidator.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
public class S3UploadController {

    private final FileService fileService;
    private final MemberService memberService;
    private final FileRequestMapper fileRequestMapper;

    /**
     * Log detailed file information for debugging
     */
    private void logFileDetails(MultipartFile file, String context) {
        log.debug("{} - File details: name='{}', original name='{}', size={} bytes, contentType='{}'", 
                context,
                file.getName(),
                file.getOriginalFilename(),
                file.getSize(), 
                file.getContentType());
    }

    @Operation(summary = "프로필 이미지 업데이트")
    @PutMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadMemberProfileImage(
            @Login UserPrincipal userPrincipal,
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
        Member requestMember = memberService.getOrThrowById(userPrincipal.getId());
        MultipartFile file = extractSingleMultipartFile(files);

        log.info("Request received: Update profile image for member ID: {}", requestMember.getId());
        logFileDetails(file, "Profile image update");
        
        FileCreate fileCreate = fileRequestMapper.toProfileImageFileCreate(file, requestMember);
        FileUploadResult fileDetails = fileService.uploadProfileImage(fileCreate, requestMember);
        
        log.info("Response sent: Profile image updated successfully for member ID: {}, url: {}, fileName: {}, size: {} bytes", 
                requestMember.getId(), fileDetails.getFileUrl(), fileDetails.getFileName(), fileDetails.getSize());

        return ResponseEntity.ok()
                .body(FileUploadResponse.from(fileDetails));
    }
}
