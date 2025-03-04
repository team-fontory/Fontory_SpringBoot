package org.fontory.fontorybe.file.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FileDetails {
    private final String fileName;
    private final String fileUrl;
    private final LocalDateTime fileUploadTime;
    private final long size;

    public static FileDetails from(FileMetadata fileMetadata) {
        return FileDetails.builder()
                .fileName(fileMetadata.getFileName())
                .fileUrl(fileMetadata.getUrl())
                .fileUploadTime(fileMetadata.getRequestTime())
                .size(fileMetadata.getSize())
                .build();
    }
}
