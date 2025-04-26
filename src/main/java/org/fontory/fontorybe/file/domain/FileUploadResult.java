package org.fontory.fontorybe.file.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FileUploadResult {
    private final String fileName;
    private final String fileUrl;
    private final LocalDateTime fileUploadTime;
    private final long size;

    public static FileUploadResult from(FileMetadata fileMetadata, String fileUrl) {
        return FileUploadResult.builder()
                .fileName(fileMetadata.getFileName())
                .fileUrl(fileUrl)
                .fileUploadTime(fileMetadata.getUploadedAt())
                .size(fileMetadata.getSize())
                .build();
    }
}
