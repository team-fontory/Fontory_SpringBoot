package org.fontory.fontorybe.file.adapter.inbound;

import org.fontory.fontorybe.authentication.adapter.inbound.Login;
import org.fontory.fontorybe.authentication.adapter.inbound.OAuth2;
import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.fontory.fontorybe.file.adapter.inbound.dto.FileUploadResponse;
import org.fontory.fontorybe.file.application.FileService;
import org.fontory.fontorybe.file.domain.FileCreate;
import org.fontory.fontorybe.file.domain.FileDetails;
import org.fontory.fontorybe.provide.domain.Provide;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

    @PostMapping("/profile-image")
    public ResponseEntity<?> uploadMemberProfileImage(
            @OAuth2 Provide provide,
            @RequestPart MultipartFile file
    ) {
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

    @PutMapping("/profile-image")
    public ResponseEntity<?> uploadMemberProfileImage(
            @Login UserPrincipal userPrincipal,
            @RequestPart MultipartFile file
    ) {
        Long memberId = userPrincipal.getId();
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
