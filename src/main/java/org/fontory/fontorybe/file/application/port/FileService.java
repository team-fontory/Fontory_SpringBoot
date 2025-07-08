package org.fontory.fontorybe.file.application.port;

import org.fontory.fontorybe.file.domain.FileUploadResult;
import org.fontory.fontorybe.file.domain.FileMetadata;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    FileMetadata getOrThrowById(Long id);
    FileUploadResult uploadFontTemplateImage(MultipartFile file, Long memberId);
}
