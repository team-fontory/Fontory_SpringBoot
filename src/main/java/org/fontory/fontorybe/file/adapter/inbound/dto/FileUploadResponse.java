package org.fontory.fontorybe.file.adapter.inbound.dto;

import lombok.Builder;
import lombok.Getter;
import org.fontory.fontorybe.file.domain.FileUploadResult;

import java.time.LocalDateTime;

@Getter
@Builder
public class FileUploadResponse {
    private final String url;
    private final LocalDateTime uploadTime;

    public static FileUploadResponse from(FileUploadResult fileDetails) {
        return FileUploadResponse.builder()
                .url(fileDetails.getFileUrl())
                .uploadTime(fileDetails.getFileUploadTime())
                .build();
    }
}
