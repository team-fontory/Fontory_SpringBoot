package org.fontory.fontorybe.file.adapter.inbound;

import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.authentication.adapter.inbound.OAuth2;
import org.fontory.fontorybe.file.adapter.inbound.dto.FileUploadResponse;
import org.fontory.fontorybe.file.domain.FileCreate;
import org.fontory.fontorybe.file.domain.FileDetails;
import org.fontory.fontorybe.file.application.FileService;
import org.fontory.fontorybe.provide.domain.Provide;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
public class S3UploadController {

    private final FileService fileService;
    private final FileRequestMapper fileRequestMapper;

    @PostMapping("/profile-image")
    public ResponseEntity<?> uploadMemberProfileImage(
            @OAuth2 Provide provide,
            @RequestPart MultipartFile file
    ) {
        FileCreate fileCreate = fileRequestMapper.toProfileImageFileCreate(file, provide);
        FileDetails fileDetails = fileService.uploadProfileImage(fileCreate);

        return ResponseEntity.ok()
                .body(FileUploadResponse.from(fileDetails));
    }
}
