package org.fontory.fontorybe.file.domain;

import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
public class FileCreate {
    private final String fileName;
    private final FileType fileType;
    private final MultipartFile file;
    private final String extension;
    private final Long uploaderId;
}
