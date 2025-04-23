package org.fontory.fontorybe.file.adapter.inbound;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.fontory.fontorybe.authentication.adapter.inbound.annotation.Login;
import org.fontory.fontorybe.authentication.adapter.inbound.annotation.OAuth2;
import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.fontory.fontorybe.file.adapter.inbound.dto.FileUploadResponse;
import org.fontory.fontorybe.file.application.FileService;
import org.fontory.fontorybe.file.domain.FileCreate;
import org.fontory.fontorybe.file.domain.FileDetails;
import org.fontory.fontorybe.provide.domain.Provide;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
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


    @Operation(summary = "프로필 이미지 업로드")
    @PostMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadMemberProfileImage(
            @OAuth2 Provide provide,
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
        MultipartFile file = extractSingleMultipartFile(files);

        log.info("Request received: Upload new profile image for oauth provider: {}, provideId: {}",
                provide.getProvider(), provide.getId());
        logFileDetails(file, "New profile image upload");

        FileCreate fileCreate = fileRequestMapper.toProfileImageFileCreate(file, provide);
        FileDetails fileDetails = fileService.uploadProfileImage(fileCreate);
        
        log.info("Response sent: Profile image uploaded successfully, url: {}, fileName: {}, size: {} bytes", 
                fileDetails.getFileUrl(), fileDetails.getFileName(), fileDetails.getSize());

        return ResponseEntity.ok()
                .body(FileUploadResponse.from(fileDetails));
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
        Long memberId = userPrincipal.getId();
        MultipartFile file = extractSingleMultipartFile(files);

        log.info("Request received: Update profile image for member ID: {}", memberId);
        logFileDetails(file, "Profile image update");
        
        FileCreate fileCreate = fileRequestMapper.toProfileImageFileCreate(file, memberId);
        FileDetails fileDetails = fileService.uploadProfileImage(fileCreate);
        
        log.info("Response sent: Profile image updated successfully for member ID: {}, url: {}, fileName: {}, size: {} bytes", 
                memberId, fileDetails.getFileUrl(), fileDetails.getFileName(), fileDetails.getSize());

        return ResponseEntity.ok()
                .body(FileUploadResponse.from(fileDetails));
    }
}
