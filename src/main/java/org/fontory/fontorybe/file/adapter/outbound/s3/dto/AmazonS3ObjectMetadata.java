package org.fontory.fontorybe.file.adapter.outbound.s3.dto;

import lombok.Builder;
import lombok.Getter;
import org.fontory.fontorybe.file.domain.FileMetadata;
import org.fontory.fontorybe.file.domain.FileType;

import java.time.LocalDateTime;

@Getter
@Builder
public class AmazonS3ObjectMetadata {
    private final String fileName;
    private final FileType fileType;
    private final String extension;
    private final Long uploaderId;
    private final String key;
    private final LocalDateTime requestTime;
    private final long size;

    public static AmazonS3ObjectMetadata from(AmazonS3PutRequest request) {
        return AmazonS3ObjectMetadata.builder()
                .fileName(request.getFileName())
                .fileType(request.getFileType())
                .extension(request.getExtension())
                .uploaderId(request.getUploaderId())
                .key(request.getKey())
                .requestTime(request.getRequestTime())
                .size(request.getSize())
                .build();
    }

    public FileMetadata toModel() {
        return FileMetadata.builder()
                .fileName(this.fileName)
                .fileType(this.fileType)
                .extension(this.extension)
                .key(this.key)
                .uploaderId(this.uploaderId)
                .size(this.size)
                .uploadedAt(requestTime)
                .build();
    }
}
